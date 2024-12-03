package com.github.lunatrius.schematica.client.renderer.chunk.proxy;

import com.github.lunatrius.schematica.client.renderer.SchematicRenderCache;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SchematicRenderChunkList extends ListedRenderChunk {
	public SchematicRenderChunkList(World world, RenderGlobal renderGlobal, BlockPos pos, int index) {
		super(world, renderGlobal, index);
	}

	@Override
	public void rebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator) {
		generator.getLock().lock();

		try {
			if (generator.getStatus() == ChunkCompileTaskGenerator.Status.COMPILING) {
				BlockPos from = getPosition();
				SchematicWorld schematic = (SchematicWorld) this.world;

				if (from.getX() < 0
						|| from.getZ() < 0
						|| from.getX() >= schematic.getWidth()
						|| from.getZ() >= schematic.getLength()) {
					SetVisibility visibility = new SetVisibility();
					visibility.setAllVisible(true);

					CompiledChunk dummy = new CompiledChunk();
					dummy.setVisibility(visibility);

					generator.setCompiledChunk(dummy);
					return;
				}
			}
		} ly {
			generator.getLock().unlock();
		}

		super.rebuildChunk(x, y, z, generator);
	}

	@Override
	protected ChunkCache createRegionRenderCache(World world, BlockPos from, BlockPos to, int subtract) {
		return new SchematicRenderCache(world, from, to, subtract);
	}
}
