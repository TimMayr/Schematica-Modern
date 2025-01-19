package com.github.lunatrius.schematica.client.world.chunk;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChunkSchematic extends Chunk {
	private final Level world;

	public ChunkSchematic(Level world, int x, int z, BiomeContainer biomeContainer) {
		super(world, new ChunkPos(x, z), biomeContainer);
		this.world = world;
	}

	@Override
	@NotNull
	public BlockState getBlockState(@Nullable BlockPos pos) {
		if (pos != null) {
			return this.world.getBlockState(pos);
		}

		return Blocks.AIR.defaultBlockState();
	}

	public BlockEntity getBlockEntity(BlockPos pos, CreateEntityType createEntityType) {
		return this.world.getBlockEntity(pos);
	}

	@Override
	public boolean isEmptyBetween(int startY, int endY) {
		return false;
	}
}