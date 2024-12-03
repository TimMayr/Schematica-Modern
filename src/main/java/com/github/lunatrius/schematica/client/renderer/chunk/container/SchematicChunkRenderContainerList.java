package com.github.lunatrius.schematica.client.renderer.chunk.container;

import com.github.lunatrius.schematica.client.renderer.chunk.overlay.RenderOverlay;
import com.github.lunatrius.schematica.client.renderer.chunk.overlay.RenderOverlayList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.opengl.GL11;

public class SchematicChunkRenderContainerList extends SchematicChunkRenderContainer {
	@Override
	public void renderChunkLayer(BlockRenderLayer layer) {
		if (this.initialized) {
			for (RenderChunk renderChunk : this.renderChunks) {
				ListedRenderChunk listedRenderChunk = (ListedRenderChunk) renderChunk;
				GlStateManager.pushMatrix();
				preRenderChunk(renderChunk);
				GL11.glCallList(listedRenderChunk.getDisplayList(layer, listedRenderChunk.getCompiledChunk()));
				GlStateManager.popMatrix();
			}

			GlStateManager.resetColor();
			this.renderChunks.clear();
		}
	}

	@Override
	public void renderOverlay() {
		if (this.initialized) {
			for (RenderOverlay renderOverlay : this.renderOverlays) {
				RenderOverlayList renderOverlayList = (RenderOverlayList) renderOverlay;
				GlStateManager.pushMatrix();
				preRenderChunk(renderOverlay);
				GL11.glCallList(renderOverlayList.getDisplayList(BlockRenderLayer.TRANSLUCENT,
				                                                 renderOverlayList.getCompiledChunk()));
				GlStateManager.popMatrix();
			}
		}

		GlStateManager.resetColor();
		this.renderOverlays.clear();
	}
}
