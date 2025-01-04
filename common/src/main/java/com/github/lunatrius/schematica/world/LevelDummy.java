package com.github.lunatrius.schematica.world;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
@MethodsReturnNonnullByDefault
public class LevelDummy extends Level {
	private static LevelDummy instance;
	protected final Scoreboard scoreboard;
	protected final boolean overrideBeLevel;
	protected final FakeChunkSource chunkSource;
	protected final FakeLevelLightEngine lightEngine;
	protected BlockGetter levelSource;
	protected Level realLevel;
	protected FakeLevelEntityGetterAdapter levelEntityGetter = FakeLevelEntityGetterAdapter.EMPTY;
	// TODO: this is currently manually filled by class user - ideally if not filled yet this should get constructed
	//  from levelSource
	// manually
	protected Map<BlockPos, BlockEntity> blockEntities = Collections.emptyMap();

	/**
	 * Current rendering worldPos so we can use client level real info
	 */
	protected BlockPos worldPos = BlockPos.ZERO;


	private LevelDummy(ClientPacketListener connection, ClientLevelData levelData, ResourceKey<Level> dimension,
	                   Holder<DimensionType> dimensionTypeRegistration, int viewDistance, int serverSimulationDistance,
	                   LevelRenderer levelRenderer, boolean isDebug, long biomeZoomSeed, int seaLevel) {
		super(Minecraft.getInstance().getConnection(), new ClientLevelData(Difficulty.EASY, false, true),
		      Level.OVERWORLD,
				, viewDistance, serverSimulationDistance,
              levelRenderer,
              isDebug,
              biomeZoomSeed,
              seaLevel);
	}

	protected LevelDummy(worldInfo, DimensionType dimensionType, IProfiler profiler) {
		super(Minecraft.getInstance().getConnection(), new WorldSettings(worldInfo), dimensionType, 8, profiler,
		      Minecraft.getInstance().worldRenderer);
	}


	public static LevelDummy instance() {
		if (instance == null) {
			WorldSettings worldSettings = new WorldSettings(0, GameType.CREATIVE, false, false, WorldType.FLAT);
			WorldInfo worldInfo = new WorldInfo(worldSettings, "FakeWorld");
			instance = new LevelDummy(worldInfo, DimensionType.OVERWORLD, Minecraft.getInstance().getProfiler());
		}

		return instance;
	}

	@Nullable
	@Override
	public Entity getEntityByID(int id) {
		return null;
	}

	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn,
	                      SoundCategory category, float volume, float pitch) {

	}

	@Override
	public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn,
	                            SoundCategory categoryIn, float volume, float pitch) {

	}

	@Override
	public RecipeManager getRecipeManager() {
		return new RecipeManager();
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return new EmptyTickList<>();
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return new EmptyTickList<>();
	}

	@Nullable
	@Override
	public MapData getMapData(String mapName) {
		return null;
	}

	@Override
	public void registerMapData(MapData mapDataIn) {

	}

	@Override
	public Scoreboard getScoreboard() {
		return new Scoreboard();
	}

	@Override
	public NetworkTagManager getTags() {
		return new NetworkTagManager();
	}

	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}

	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {

	}

	@Override
	public List<AbstractClientPlayerEntity> getPlayers() {
		return Collections.emptyList();
	}

	@Override
	public Biome getNoiseBiomeRaw(int x, int y, int z) {
		return Biomes.JUNGLE;
	}
}