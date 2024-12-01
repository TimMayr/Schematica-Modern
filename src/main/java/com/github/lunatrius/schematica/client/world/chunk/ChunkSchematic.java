package com.github.lunatrius.schematica.client.world.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class ChunkSchematic extends Chunk {
	private final World world;

	public ChunkSchematic(final World world, final int x, final int z, BiomeContainer biomeContainer) {
		super(world, new ChunkPos(x, z), biomeContainer);
		this.world = world;
	}

	@Override
	@Nonnull
	public BlockState getBlockState(final @Nullable BlockPos pos) {
		if (pos != null) {
			return this.world.getBlockState(pos);
		}

		return Blocks.AIR.getDefaultState();
	}

	@Override
	@ParametersAreNonnullByDefault
	public TileEntity getTileEntity(final BlockPos pos, final CreateEntityType createEntityType) {
		return this.world.getTileEntity(pos);
	}

	@Override
	public boolean isEmptyBetween(final int startY, final int endY) {
		return false;
	}
}
