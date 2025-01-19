package com.github.lunatrius.schematica.client.world;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.block.state.pattern.BlockStateReplacer;
import com.github.lunatrius.schematica.client.world.chunk.SchematicChunkProvider;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SchematicWorld extends ClientWorld {
	private static final WorldSettings WORLD_SETTINGS =
			new WorldSettings(0, GameType.CREATIVE, false, false, WorldType.FLAT);
	public final MBlockPos position = new MBlockPos();
	public boolean isRendering = false;
	public LayerMode layerMode = LayerMode.ALL;
	public int renderingLayer = 0;
	protected AbstractChunkProvider chunkProvider = new SchematicChunkProvider(this);
	private ISchematic schematic;

	@SuppressWarnings("DataFlowIssue")
	public SchematicWorld(ISchematic schematic) {
		super(Minecraft.getInstance().getConnection(), WORLD_SETTINGS, DimensionType.OVERWORLD, 8,
		      Minecraft.getInstance().getProfiler(), Minecraft.getInstance().worldRenderer);
		this.schematic = schematic;

		for (BlockEntity blockEntity : schematic.getBlockEntities()) {
			initializeBlockEntity(blockEntity);
		}
	}

	public void initializeBlockEntity(BlockEntity blockEntity) {
		blockEntity.setWorldAndPos(this, blockEntity.getPos());
		blockEntity.getBlockState().getBlock();
		try {
			blockEntity.remove();
			blockEntity.validate();
		} catch (Exception e) {
			Reference.logger.error("BlockEntity validation for {} failed!", blockEntity.getClass(), e);
		}
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
		return this.schematic.setBlockState(pos, state);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (!this.layerMode.shouldUseLayer(this, pos.getY())) {
			return Blocks.AIR.getDefaultState();
		}

		return this.schematic.getBlockState(pos);
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (!this.layerMode.shouldUseLayer(this, pos.getY())) {
			return null;
		}

		return this.schematic.getBlockEntity(pos);
	}

	public void setBlockEntity(BlockPos pos, @Nullable BlockEntity blockEntity) {
		if (blockEntity != null) {
			this.schematic.setBlockEntity(pos, blockEntity);
			initializeBlockEntity(blockEntity);
		}
	}

	public void removeBlockEntity(BlockPos pos) {
		this.schematic.removeBlockEntity(pos);
	}

	@Override
	public void calculateInitialSkylight() {}

	@Override
	protected void calculateInitialWeather() {}

	@Override
	public void setSpawnPoint(BlockPos pos) {}

	public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
		Chunk chunk = getChunkAt(pos);
		return getBlockState(pos).isNormalCube(getWorld().getBlockReader(chunk.getPos().x, chunk.getPos().z), pos);
	}

	@Override
	public int getLightValue(BlockPos p_217298_1_) {
		return 15;
	}

	@Override
	public int getHeight() {
		return this.schematic.getHeight();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getLightFor(LightType lightType, BlockPos pos) {
		return 15;
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return Biomes.JUNGLE;
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		IForgeBlockState blockState = getBlockState(pos);
		return blockState.getBlockState().getBlock().isAir(blockState.getBlockState(), this, pos);
	}

	@Override
	@Nullable
	public Entity getEntityByID(int id) {
		return null;
	}

	public boolean isSideSolid(BlockPos pos, Direction side) {
		return isSideSolid(pos, side, false);
	}

	public boolean isSideSolid(BlockPos pos, Direction side, boolean _default) {
		return getBlockState(pos).isSolidSide(this, pos, side);
	}

	public ISchematic getSchematic() {
		return this.schematic;
	}

	public void setSchematic(ISchematic schematic) {
		this.schematic = schematic;
	}

	public ItemStack getIcon() {
		return this.schematic.getIcon();
	}

	public void setIcon(ItemStack icon) {
		this.schematic.setIcon(icon);
	}

	public List<BlockEntity> getBlockEntities() {
		return this.schematic.getBlockEntities();
	}

	public boolean toggleRendering() {
		this.isRendering = !this.isRendering;
		return this.isRendering;
	}

	public String getDebugDimensions() {
		return "WHL: " + getWidth() + " / " + getHeight() + " / " + getLength();
	}

	public int getWidth() {
		return this.schematic.getSizeX();
	}

	public int getLength() {
		return this.schematic.getSizeZ();
	}

	@SuppressWarnings({"rawtypes"})
	public int replaceBlock(BlockStateMatchTest matcher, BlockStateReplacer replacer) {
		int count = 0;

		for (MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, getWidth(), getHeight(), getLength())) {
			BlockState blockState = this.schematic.getBlockState(pos);

			// TODO: add support for tile entities?
			if (blockState.getBlock().hasTileEntity(blockState)) {
				continue;
			}

			if (matcher.test(blockState)) {
				Map<IProperty, Comparable> properties = BlockStateHelper.getProperties(blockState);
				BlockState replacement = replacer.getReplacement(properties);

				// TODO: add support for tile entities?
				if (replacement.getBlock().hasTileEntity(replacement)) {
					continue;
				}

				if (this.schematic.setBlockState(pos, replacement)) {
					notifyBlockUpdate(pos.add(this.position), blockState, replacement, 3);
					count++;
				}
			}
		}

		return count;
	}

	public boolean isInside(BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		return !(x < 0 || y < 0 || z < 0 || x >= getWidth() || y >= getHeight() || z >= getLength());
	}

	public enum LayerMode {
		ALL(Names.Gui.Control.MODE_ALL) {
			@Override
			public boolean shouldUseLayer(SchematicWorld world, int layer) {
				return true;
			}
		},
		SINGLE_LAYER(Names.Gui.Control.MODE_LAYERS) {
			@Override
			public boolean shouldUseLayer(SchematicWorld world, int layer) {
				return layer == world.renderingLayer;
			}
		},
		ALL_BELOW(Names.Gui.Control.MODE_BELOW) {
			@Override
			public boolean shouldUseLayer(SchematicWorld world, int layer) {
				return layer <= world.renderingLayer;
			}
		};

		public final String name;

		LayerMode(String name) {
			this.name = name;
		}

		public static LayerMode next(LayerMode mode) {
			LayerMode[] values = values();
			return values[(mode.ordinal() + 1) % values.length];
		}

		public abstract boolean shouldUseLayer(SchematicWorld world, int layer);
	}
}