package com.github.lunatrius.schematica.client.renderer;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ViewFrustumOverlay extends ViewFrustum {

	public ViewFrustumOverlay(ChunkRenderDispatcher dispatcher, World world, int countChunks,
	                          WorldRenderer worldRenderer) {
		super(dispatcher, world, countChunks, worldRenderer);
	}

	@Override
	public void updateChunkPositions(double viewEntityX, double viewEntityZ) {
		super.updateChunkPositions(viewEntityX, viewEntityZ);
	}

	@Override
	public void markForRerender(int sectionX, int sectionY, int sectionZ, boolean rerenderOnMainThread) {
		super.markForRerender(sectionX, sectionY, sectionZ, rerenderOnMainThread);
	}

	public ChunkRenderDispatcher.ChunkRender getChunkRender(BlockPos pos) {
		int i = MathHelper.intFloorDiv(pos.getX(), 16);
		int j = MathHelper.intFloorDiv(pos.getY(), 16);
		int k = MathHelper.intFloorDiv(pos.getZ(), 16);
		if (j >= 0 && j < this.countChunksY) {
			i = MathHelper.normalizeAngle(i, this.countChunksX);
			k = MathHelper.normalizeAngle(k, this.countChunksZ);
			return this.renderChunks[this.getIndexPublic(i, j, k)];
		} else {
			return null;
		}
	}

	public int getIndexPublic(int x, int y, int z) {
		return (z * this.countChunksY + y) * this.countChunksX + x;
	}
}
