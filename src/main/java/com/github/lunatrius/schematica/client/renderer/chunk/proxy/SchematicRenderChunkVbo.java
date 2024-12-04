package com.github.lunatrius.schematica.client.renderer.chunk.proxy;

import com.github.lunatrius.schematica.client.renderer.SchematicRenderCache;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SchematicRenderChunkVbo extends ChunkRenderDispatcher.ChunkRender {
	public SchematicRenderChunkVbo(World world, WorldRenderer renderGlobal, int index) {
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
	protected ChunkRenderCache createRegionRenderCache(World world, BlockPos from, BlockPos to, int subtract) {
		return Objects.requireNonNull(SchematicRenderCache.generateCache(world, from, to, subtract));
	}
}
