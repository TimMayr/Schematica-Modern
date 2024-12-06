package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.schematica.client.renderer.chunk.overlay.ISchematicRenderChunkFactory;
import com.github.lunatrius.schematica.client.renderer.chunk.overlay.RenderOverlay;
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
	public RenderOverlay[] renderOverlays;

	public ViewFrustumOverlay(ChunkRenderDispatcher dispatcher, World world, int countChunks,
	                          WorldRenderer worldRenderer) {
		super(dispatcher, world, countChunks, worldRenderer);
	}

	protected void createRenderOverlays(ISchematicRenderChunkFactory renderChunkFactory) {
		int amount = this.countChunksX * this.countChunksY * this.countChunksZ;
		this.renderOverlays = new RenderOverlay[amount];
		int count = 0;

		for (int x = 0; x < this.countChunksX; x++) {
			for (int y = 0; y < this.countChunksY; y++) {
				for (int z = 0; z < this.countChunksZ; z++) {
					int index = (z * this.countChunksY + y) * this.countChunksX + x;
					this.renderOverlays[index] =
							renderChunkFactory.makeRenderOverlay(this.world, this.renderGlobal, count++);
				}
			}
		}
	}

	@Override
	public void deleteGlResources() {
		super.deleteGlResources();

		for (RenderOverlay renderOverlay : this.renderOverlays) {
			renderOverlay.deleteGlResources();
		}
	}

	@Override
	public void updateChunkPositions(double viewEntityX, double viewEntityZ) {
		super.updateChunkPositions(viewEntityX, viewEntityZ);

		int xx = MathHelper.floor(viewEntityX) - 8;
		int zz = MathHelper.floor(viewEntityZ) - 8;
		int yy = this.countChunksX * 16;

		for (int chunkX = 0; chunkX < this.countChunksX; chunkX++) {
			int x = getPosition(xx, yy, chunkX);

			for (int chunkZ = 0; chunkZ < this.countChunksZ; chunkZ++) {
				int z = getPosition(zz, yy, chunkZ);

				for (int chunkY = 0; chunkY < this.countChunksY; chunkY++) {
					int y = chunkY * 16;
					RenderOverlay renderOverlay =
							this.renderOverlays[(chunkZ * this.countChunksY + chunkY) * this.countChunksX + chunkX];
					BlockPos blockpos = new BlockPos(x, y, z);

					if (!blockpos.equals(renderOverlay.getPosition())) {
						renderOverlay.setPosition(x, y, z);
					}
				}
			}
		}
	}

	private int getPosition(int xz, int y, int chunk) {
		int chunks = chunk * 16;
		int i = chunks - xz + y / 2;

		if (i < 0) {
			i -= y - 1;
		}

		return chunks - i / y * y;
	}

	@Override
	public void markForRerender(int sectionX, int sectionY, int sectionZ, boolean rerenderOnMainThread) {
		super.markForRerender(sectionX, sectionY, sectionZ, rerenderOnMainThread);

		int x = MathHelper.intFloorDiv(sectionX, 16) % this.countChunksX;
		if (x < 0) {
			x += this.countChunksX;
		}

		int y = MathHelper.intFloorDiv(sectionY, 16) % this.countChunksY;
		if (y < 0) {
			y += this.countChunksY;
		}

		int z = MathHelper.intFloorDiv(sectionZ, 16) % this.countChunksZ;
		if (z < 0) {
			z += this.countChunksZ;
		}

		int index = (z * this.countChunksY + y) * this.countChunksX + x;
		RenderOverlay renderOverlay = this.renderOverlays[index];
		renderOverlay.setNeedsUpdate(rerenderOnMainThread);
	}

	public RenderOverlay getRenderOverlay(BlockPos pos) {
		int x = MathHelper.intFloorDiv(pos.getX(), 16);
		int y = MathHelper.intFloorDiv(pos.getY(), 16);
		int z = MathHelper.intFloorDiv(pos.getZ(), 16);

		if (y >= 0 && y < this.countChunksY) {
			x %= this.countChunksX;

			if (x < 0) {
				x += this.countChunksX;
			}

			z %= this.countChunksZ;

			if (z < 0) {
				z += this.countChunksZ;
			}

			int index = (z * this.countChunksY + y) * this.countChunksX + x;
			return this.renderOverlays[index];
		} else {
			return null;
		}
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
