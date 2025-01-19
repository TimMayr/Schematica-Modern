package com.github.lunatrius.schematica.world.chunk;

import com.github.lunatrius.schematica.world.FakeLevel;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
@MethodsReturnNonnullByDefault
public class FakeChunkSource extends ChunkSource {
	private final FakeLevel fakeLevel;

	public FakeChunkSource(FakeLevel fakeLevel) {
		this.fakeLevel = fakeLevel;
	}

	@Override
	public FakeLevel getLevel() {
		return fakeLevel;
	}

	@Override
	public @Nullable ChunkAccess getChunk(int x, int z, @NotNull ChunkStatus chunkStatus, boolean requireChunk) {
		return fakeLevel.getChunk(x, z, chunkStatus, requireChunk);
	}

	@Override
	public void tick(@Nullable BooleanSupplier ignored_1, boolean ignored_2) {
		// noop
	}

	@Override
	public String gatherStats() {
		return fakeLevel.gatherChunkSourceStats();
	}

	@Override
	public int getLoadedChunksCount() {
		final int xCount = (fakeLevel.getLevelSource().getSizeX() + 15) / 16, zCount =
				(fakeLevel.getLevelSource().getSizeZ() + 15) / 16;
		return xCount * zCount;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return fakeLevel.getLightEngine();
	}
}