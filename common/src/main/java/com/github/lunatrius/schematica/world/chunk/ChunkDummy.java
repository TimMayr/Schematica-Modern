package com.github.lunatrius.schematica.world.chunk;

import com.github.lunatrius.schematica.world.LevelDummy;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

@MethodsReturnNonnullByDefault
public class ChunkDummy extends LevelChunk {
	private final LevelDummy levelDummy;

	public ChunkDummy(LevelDummy level, int x, int z) {
		super(level, new ChunkPos(x, z));
		this.levelDummy = level;
	}

	@Override
	public BlockState getBlockState(@NotNull BlockPos pos) {
		return levelDummy.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(@NotNull BlockPos pos) {
		return levelDummy.getFluidState(pos);
	}

	@Override
	public FluidState getFluidState(int bx, int by, int bz) {
		return getFluidState(new BlockPos(bx, by, bz));
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(@NotNull BlockPos pos, @NotNull EntityCreationType creationMode) {
		return levelDummy.getBlockEntity(pos);
	}

	@Override
	public Map<BlockPos, BlockEntity> getBlockEntities() {
		// TODO: this should ideally return only BEs in this chunk
		return levelDummy.blockEntities;
	}

	@Override
	public Set<BlockPos> getBlockEntitiesPos() {
		return getBlockEntities().keySet();
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z) {
		return levelDummy.getNoiseBiome(x, y, z);
	}
}