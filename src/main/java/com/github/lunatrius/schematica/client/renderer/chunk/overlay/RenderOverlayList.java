package com.github.lunatrius.schematica.client.renderer.chunk.overlay;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RenderOverlayList extends RenderOverlay {
	private final int displayList = GLAllocation.generateDisplayLists(1);

	public RenderOverlayList(World world, RenderGlobal renderGlobal, BlockPos pos, int index) {
		super(world, renderGlobal, index);
	}

	public int getDisplayList(BlockRenderLayer layer, CompiledChunk compiledChunk) {
		return !compiledChunk.isLayerEmpty(layer) ? this.displayList : -1;
	}

	@Override
	public void deleteGlResources() {
		super.deleteGlResources();
		GLAllocation.deleteDisplayLists(this.displayList);
	}
}
