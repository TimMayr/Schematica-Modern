package com.github.lunatrius.schematica.client.renderer.chunk;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

import javax.annotation.Nonnull;


public class CompiledOverlay extends ChunkRenderDispatcher.CompiledChunk {
	public void setLayerStarted(RenderType layer) {
		if (layer == RenderType.getTranslucent()) {
			this.layersStarted.add(layer);
		}
	}

	public void setLayerUsed(RenderType layer) {
		if (layer == RenderType.getTranslucent()) {
			this.layersUsed.add(layer);
		}
	}

	public boolean isLayerStarted(RenderType layer) {
		return layer == RenderType.getTranslucent() && this.layersStarted.contains(layer);
	}

	@Override
	public boolean isLayerEmpty(@Nonnull RenderType layer) {
		return layer == RenderType.getTranslucent() && super.isLayerEmpty(layer);
	}
}
