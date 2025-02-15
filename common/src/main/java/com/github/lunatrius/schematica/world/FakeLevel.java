package com.github.lunatrius.schematica.world;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.block.state.pattern.BlockStateReplacer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.FakeChunk;
import com.github.lunatrius.schematica.world.chunk.FakeChunkSource;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer.Continuation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * As much as general fake level. Features:
 * <ul>
 * <li>static access to given data</li>
 * <li>immutability - disables all external changes (but levelSource can be mutable)</li>
 * <li>most of the dimension related things is delegated to current client level (class instances can travel across
 * dimensions)</li>
 * <li>biome info is also delegated from client level</li>
 * <li>light control - manual or delegated from client level</li>
 * <li>primitive chunk and entity management</li>
 * <li>basic heightmap support (not fully working yet)</li>
 * <li><b>Few unsafe NPEs methods :)</b></li>
 * <p>
 * TODO: extend from client level
 */
@MethodsReturnNonnullByDefault
public class FakeLevel extends Level {
	protected final IFakeLevelLightProvider lightProvider;
	protected final Scoreboard scoreboard;
	protected final boolean overrideBeLevel;
	protected final FakeChunkSource chunkSource;
	protected final FakeLevelLightEngine lightEngine;
	public Map<BlockPos, BlockEntity> blockEntities = Collections.emptyMap();
	// chunk cache
	public int lastX;
	public int lastZ;
	public ChunkAccess lastChunk = null;
	public LayerMode layerMode = LayerMode.ALL;
	public int renderLayer;
	protected Level realLevel;
	protected FakeLevelEntityGetterAdapter levelEntityGetter = FakeLevelEntityGetterAdapter.EMPTY;
	/**
	 * Current rendering worldPos so we can use client level real info
	 */
	protected MBlockPos worldPos = new MBlockPos();
	private ISchematic levelSource;
	private boolean isRendering;

	/**
	 * @param levelSource     data source, also try to set block entities/entities collections
	 * @param lightProvider   light source
	 * @param scoreboard      if null client level is used instead
	 * @param overrideBeLevel if true all block entities will have set level to this instance
	 * @see #setBlockEntities(Map) for better block entity handling, if set then levelSource BE getter is not used
	 * @see #setEntities(Collection) only way to add entities into fake level
	 * @see #setRealLevel(Level) if you want to reuse this instance
	 */
	public FakeLevel(final ISchematic levelSource, final IFakeLevelLightProvider lightProvider,
	                 @Nullable final Scoreboard scoreboard, final boolean overrideBeLevel) {
		super(new FakeLevelData(level()::getLevelData, lightProvider), level().dimension(),
				level().registryAccess(), level().dimensionTypeRegistration(),
				level().isClientSide(),
				level().isDebug(), 0, 0);
		this.setLevelSource(levelSource);
		this.lightProvider = lightProvider;
		this.realLevel = level();
		this.scoreboard = scoreboard;
		this.overrideBeLevel = overrideBeLevel;
		this.chunkSource = new FakeChunkSource(this);
		this.lightEngine = new FakeLevelLightEngine(this);

		setRealLevel(level());
		try (Level realLevel = realLevel()) {
			((FakeLevelData) getLevelData()).vanillaLevelData = realLevel::getLevelData;
		} catch (IOException e) {
			throw new RuntimeException("Error creating fake level");
		}
	}

	protected static Level level() {
		return Reference.proxy.getLevel();
	}

	public void setRealLevel(Level realLevel) {
		this.realLevel = realLevel;
	}

	// ========================================
	// ========== FAKE LEVEL METHODS ==========
	// ========================================

	public Level realLevel() {
		return realLevel;
	}

	public static FakeLevel of(ISchematic schematic) {
		return new FakeLevel(schematic, new IFakeLevelLightProvider() {
			@Override
			public boolean forceOwnLightLevel() {
				return false;
			}

			@Override
			public int getSkyDarken() {
				throw new UnsupportedOperationException("Noop light provider");
			}

			@Override
			public int getBlockLight(final BlockPos pos) {
				throw new UnsupportedOperationException("Noop light provider");
			}
		}, new Scoreboard(), true);
	}

	/**
	 * @return anchor in vanilla client level
	 */
	public MBlockPos getWorldPos() {
		return worldPos;
	}

	/**
	 * @param worldPos where is fake level anchor when querying current client level data
	 */
	public void setWorldPos(MBlockPos worldPos) {
		this.worldPos = worldPos;
	}

	/**
	 * For better block entity handling in chunk methods. If set then
	 * {@link ISchematic#getBlockEntity(BlockPos)
	 * levelSource.getBlockEntity(BlockPos)} is not used. Reset with empty collection
	 *
	 * @param blockEntities all block entities, should be data equivalent to levelSource
	 */
	public void setBlockEntities(Map<BlockPos, BlockEntity> blockEntities) {
		this.blockEntities = blockEntities;
	}

	@Override
	public boolean isInWorldBounds(@NotNull BlockPos pos) {
		return getLevelSource().isPosInside(pos);
	}

	/**
	 * @return current data source
	 */
	public ISchematic getLevelSource() {
		return levelSource;
	}

	public void setLevelSource(ISchematic levelSource) {
		this.levelSource = levelSource;
	}

	// ========================================
	// ======= CTOR REAL LEVEL REDIRECTS ======
	// ========================================
	// Note: must have null check because super ctor

	@Nullable
	@Override
	public ChunkAccess getChunk(int x, int z, @NotNull ChunkStatus requiredStatus, boolean nonnull) {
		if (lastX == x && lastZ == z && lastChunk != null) {
			return lastChunk;
		}
		return nonnull || hasChunk(x, z) ? new FakeChunk(this, x, z) : null;
	}

	@Override
	public boolean setBlock(@Nullable BlockPos pos, @Nullable BlockState block, int ignored_1,
	                        int ignored_2) {
		return levelSource.setBlockState(pos, block);
	}

	@Override
	public boolean removeBlock(@Nullable BlockPos ignored_1, boolean ignored_2) {
		return false;
	}

	@Override
	public boolean destroyBlock(@Nullable BlockPos ignored_1, boolean ignored_2, @Nullable Entity ignored_3,
	                            int ignored_4) {
		return false;
	}

	@Override
	public void sendBlockUpdated(@Nullable BlockPos ignored_1, @Nullable BlockState ignored_2,
	                             @Nullable BlockState ignored_3, int ignored_4) {
		// Noop
	}

	@Override
	public void updateNeighborsAt(@Nullable BlockPos ignored_2, @Nullable Block ignored_1) {
		// Noop
	}

	// ========================================
	// ========== REDIRECTED METHODS ==========
	// ========================================

	@Override
	public void neighborShapeChanged(@Nullable Direction ignored_1, @Nullable BlockPos ignored_2,
	                                 @Nullable BlockPos ignored_3, @Nullable BlockState ignored_4, int flags,
	                                 int recursionLeft) {
		// Noop
	}

	@Override
	public int getHeight(@NotNull Types heightmapType, int x, int z) {
		final MutableBlockPos pos = new MutableBlockPos(x, getLevelSource().getMinY(), z);

		if (getLevelSource().isPosInside(pos)) {
			for (int y = getLevelSource().getMaxY() - 1; y >= getLevelSource().getMinY(); y--) {
				pos.setY(y);
				if (heightmapType.isOpaque().test(getLevelSource().getBlockState(pos))) {
					return y;
				}
			}
		}

		return getLevelSource().getMinY();
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return lightEngine;
	}

	@Override
	public BlockState getBlockState(@NotNull BlockPos pos) {
		return getLevelSource().isPosInside(pos) ? getLevelSource().getBlockState(pos) :
				Blocks.AIR.defaultBlockState();
	}

	@Override
	public FluidState getFluidState(@NotNull BlockPos pos) {
		return getLevelSource().getFluidState(pos);
	}

	@Override
	public boolean isDay() {
		return !this.dimensionType().hasFixedTime() && this.getSkyDarken() < 4;
	}

	@Override
	public void playSeededSound(@Nullable Player ignored_1, double ignored_2, double ignored_3, double ignored_4,
	                            @Nullable Holder<SoundEvent> ignored_5, @Nullable SoundSource ignored_6,
	                            float ignored_7, float ignored_8, long ignored_9) {
		// Noop
	}

	@Override
	public void playSeededSound(@Nullable Player ignored_1, @Nullable Entity ignored_2,
	                            @Nullable Holder<SoundEvent> ignored_3, @Nullable SoundSource ignored_4,
	                            float ignored_5, float ignored_6, long ignored_7) {
		// Noop
	}

	@Override
	public void addBlockEntityTicker(@Nullable TickingBlockEntity ignored) {
		// Noop
	}

	@Override
	protected void tickBlockEntities() {
		// Noop
	}

	@Override
	public boolean shouldTickDeath(@Nullable Entity ignored) {
		return false;
	}

	@Override
	public boolean shouldTickBlocksAt(long ignored) {
		return false;
	}

	@Override
	public void explode(@Nullable Entity source, @Nullable DamageSource damageSource,
	                    @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z,
	                    float radius, boolean fire, @Nullable ExplosionInteraction explosionInteraction,
	                    @Nullable ParticleOptions smallExplosionParticles,
	                    @Nullable ParticleOptions largeExplosionParticles,
	                    @Nullable Holder<SoundEvent> explosionSound) {

		throw new UnsupportedOperationException("Schematica fake immutable level - no explosions possible!");
	}

	@Override
	public String gatherChunkSourceStats() {
		return "Fake level for: " + getLevelSource();
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(@NotNull BlockPos pos) {
		final BlockEntity blockEntity =
				blockEntities.isEmpty() ? getLevelSource().getBlockEntity(pos) : blockEntities.get(pos);
		if (blockEntity != null && blockEntity.getLevel() != this && (overrideBeLevel || !blockEntity.hasLevel())) {
			blockEntity.setLevel(this);
		}
		return blockEntity;
	}

	@Override
	public boolean isLoaded(@Nullable BlockPos ignored) {
		return true;
	}

	@Override
	public void updateSkyBrightness() {
		// Noop
	}

	@Override
	public void setSpawnSettings(boolean ignored) {
		// Noop
	}

	@Override
	public void close() {
		// Noop
	}

	@Override
	@Nullable
	public Entity getEntity(int id) {
		return levelEntityGetter.get(id);
	}

	@Override
	public Collection<EnderDragonPart> dragonParts() {
		return List.of();
	}

	@Override
	public boolean mayInteract(@Nullable Player ignored_1, @Nullable BlockPos ignored_2) {
		return false;
	}

	@Override
	public void blockEvent(@Nullable BlockPos ignored_1, @Nullable Block ignored_2, int ignored_3, int ignored_4) {
		// Noop
	}

	@Override
	public TickRateManager tickRateManager() {
		return null;
	}

	@Override
	public float getThunderLevel(float ignored) {
		return 0;
	}

	@Override
	public void setThunderLevel(float ignored) {
		// Noop
	}

	@Override
	public float getRainLevel(float ignored) {
		return 0;
	}

	@Override
	public void setRainLevel(float ignored_1) {
		// Noop
	}

	@Override
	public boolean isRainingAt(@Nullable BlockPos ignored) {
		return isRaining();
	}

	@Override
	public @Nullable MapItemSavedData getMapData(@Nullable MapId mapId) {
		return null;
	}

	// ========================================
	// ======= NOOP UNSAFE NULL METHODS =======
	// ========================================

	@Override
	public void setMapData(@Nullable MapId ignored_1, @Nullable MapItemSavedData ignored_2) {
		// Noop
	}

	// ========================================
	// ========== PERMANENT SETTINGS ==========
	// ========================================

	@Override
	public MapId getFreeMapId() {
		return new MapId(0);
	}

	@Override
	public CrashReportCategory fillReportDetails(@NotNull CrashReport report) {
		CrashReportCategory crashreportcategory = report.addCategory("Schematica fake level");
		getLevelSource().describeSelfInCrashReport(crashreportcategory);
		return crashreportcategory;
	}

	@Override
	public void destroyBlockProgress(int ignored_1, @Nullable BlockPos ignored_2, int ignored_3) {
		// Noop
	}

	@Override
	public Scoreboard getScoreboard() {
		try (Level realLevel = realLevel()) {
			return scoreboard == null ? realLevel.getScoreboard() : scoreboard;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getSkyDarken() {
		try (Level realLevel = realLevel()) {
			return lightProvider.forceOwnLightLevel() ? lightProvider.getSkyDarken() : realLevel.getSkyDarken();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public WorldBorder getWorldBorder() {
		try (Level realLevel = realLevel()) {
			return realLevel.getWorldBorder();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// ========================================
	// ============ NOOP OVERRIDES ============
	// ========================================

	@Override
	public DimensionType dimensionType() {
		try (Level realLevel = realLevel()) {
			return realLevel.dimensionType();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Holder<DimensionType> dimensionTypeRegistration() {
		try (Level realLevel = realLevel()) {
			return realLevel.dimensionTypeRegistration();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResourceKey<Level> dimension() {
		try (Level realLevel = realLevel()) {
			return realLevel.dimension();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public RecipeAccess recipeAccess() {
		try (Level realLevel = realLevel()) {
			return realLevel.recipeAccess();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean noSave() {
		return true;
	}

	@Override
	public BiomeManager getBiomeManager() {
		try (Level realLevel = realLevel()) {
			return realLevel.getBiomeManager();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return levelEntityGetter;
	}

	/**
	 * @param entities all entities, their level should be this fake level instance. Reset with empty collection
	 */
	public void setEntities(@NotNull Collection<? extends Entity> entities) {
		levelEntityGetter = entities.isEmpty()
				? FakeLevelEntityGetterAdapter.EMPTY
				: FakeLevelEntityGetterAdapter.ofEntities(entities);
	}

	@Override
	public RegistryAccess registryAccess() {
		try (Level realLevel = realLevel()) {
			return realLevel.registryAccess();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public DamageSources damageSources() {
		try (Level realLevel = realLevel()) {
			return realLevel.damageSources();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PotionBrewing potionBrewing() {
		return null;
	}

	@Override
	public FuelValues fuelValues() {
		return null;
	}

	@Override
	public List<AbstractClientPlayer> players() {
		final List<AbstractClientPlayer> result = new ArrayList<>();
		levelEntityGetter.get(EntityTypeTest.forClass(AbstractClientPlayer.class), player -> {
			result.add(player);
			return Continuation.CONTINUE;
		});
		return result;
	}

	// ========================================
	// ============= NOOP METHODS =============
	// ========================================

	@Override
	public ChunkSource getChunkSource() {
		return chunkSource;
	}

	@Override
	public boolean hasChunk(int chunkX, int chunkZ) {
		final int posX = SectionPos.sectionToBlockCoord(chunkX);
		final int posZ = SectionPos.sectionToBlockCoord(chunkZ);
		return getLevelSource().getMinX() <= posX
				&& posX < getLevelSource().getMaxX()
				&& getLevelSource().getMinZ() <= posZ
				&& posZ < getLevelSource().getMaxZ();
	}

	@Override
	public void levelEvent(@Nullable Player ignored_1, int ignored_2, @Nullable BlockPos ignored_3, int ignored_4) {
		// Noop
	}

	@Override
	public void gameEvent(@Nullable Holder<GameEvent> ignored_1, @Nullable Vec3 ignored_2,
	                      @Nullable Context ignored_3) {
		// Noop
	}

	@Override
	public float getShade(@NotNull Direction direction, boolean shade) {
		try (Level realLevel = realLevel()) {
			return realLevel.getShade(direction, shade);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getBrightness(@NotNull LightLayer lightType, @NotNull BlockPos pos) {
		try (Level realLevel = realLevel()) {
			return lightProvider.forceOwnLightLevel()
					? lightProvider.getBrightness(lightType, pos)
					: realLevel.getBrightness(lightType, worldPos.offset(pos));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getRawBrightness(@NotNull BlockPos pos, int amount) {
		try (Level realLevel = realLevel()) {
			return lightProvider.forceOwnLightLevel()
					? lightProvider.getRawBrightness(pos, amount)
					: realLevel.getRawBrightness(worldPos.offset(pos), amount);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Holder<Biome> getBiome(@NotNull BlockPos pos) {
		try (Level realLevel = realLevel()) {
			return realLevel.getBiome(worldPos.offset(pos));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z) {
		try (Level realLevel = realLevel()) {
			return realLevel.getNoiseBiome(x, y, z);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
		try (Level realLevel = realLevel()) {
			return realLevel.getUncachedNoiseBiome(x, y, z);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public int getMinY() {
		return getLevelSource().getMinY();
	}

	@Override
	public int getHeight() {
		return getLevelSource().getHeight();
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		try (Level realLevel = realLevel()) {
			return realLevel.enabledFeatures();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return BlackholeTickAccess.emptyLevelList();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyLevelList();
	}

	public boolean isRendering() {
		return isRendering;
	}

	public void setRendering(boolean rendering) {
		isRendering = rendering;
	}

	public boolean shouldUseLayer(int y) {
		return layerMode.shouldUseLayer(this, y);
	}

	public boolean toggleRendering() {
		this.isRendering = !isRendering;
		return this.isRendering;
	}

	@SuppressWarnings("rawtypes")
	public int replaceBlock(BlockStateMatchTest matcher, BlockStateReplacer replacer) {
		int count = 0;

		for (MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, getLevelSource().getMaxX(), getHeight(),
				getLevelSource().getMaxZ())) {
			BlockState blockState = this.getBlockState(pos);

			if (blockState.hasBlockEntity()) {
				continue;
			}

			if (matcher.test(blockState, RandomSource.create())) {
				Map<Property, Comparable> properties = BlockStateHelper.getProperties(blockState);
				BlockState replacement = replacer.getReplacement(properties);

				if (replacement.hasBlockEntity()) {
					continue;
				}

				if (this.setBlockAndUpdate(pos, replacement)) {
					count++;
				}
			}
		}

		return count;
	}

	public enum LayerMode {
		ALL(Names.Gui.Control.MODE_ALL) {
			@Override
			public boolean shouldUseLayer(FakeLevel world, int layer) {
				return true;
			}
		},
		SINGLE_LAYER(Names.Gui.Control.MODE_LAYERS) {
			@Override
			public boolean shouldUseLayer(FakeLevel world, int layer) {
				return layer == world.renderLayer;
			}
		},
		ALL_BELOW(Names.Gui.Control.MODE_BELOW) {
			@Override
			public boolean shouldUseLayer(FakeLevel world, int layer) {
				return layer <= world.renderLayer;
			}
		};

		public final String name;

		LayerMode(String name) {
			this.name = name;
		}

		public static LayerMode next(@NotNull LayerMode mode) {
			LayerMode[] values = values();
			return values[(mode.ordinal() + 1) % values.length];
		}

		public abstract boolean shouldUseLayer(FakeLevel world, int layer);
	}
}