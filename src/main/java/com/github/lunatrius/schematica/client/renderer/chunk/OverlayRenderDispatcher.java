package com.github.lunatrius.schematica.client.renderer.chunk;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OverlayRenderDispatcher extends ChunkRenderDispatcher {
	public OverlayRenderDispatcher(World worldIn, WorldRenderer worldRendererIn, Executor executorIn,
	                               boolean java64bit,
	                               RegionRenderCacheBuilder fixedBuilderIn) {
		super(worldIn, worldRendererIn, executorIn, java64bit, fixedBuilderIn, -1);
	}

	public OverlayRenderDispatcher(World worldIn, WorldRenderer worldRendererIn, Executor executorIn,
	                               boolean java64bit,
	                               RegionRenderCacheBuilder fixedBuilderIn, int countRenderBuilders) {
		super(worldIn, worldRendererIn, executorIn, java64bit, fixedBuilderIn, countRenderBuilders);
	}

	@Override
	public void rebuildChunk(ChunkRenderDispatcher.ChunkRender chunkRenderIn) {
		CompiledOverlay compiledOverlay = new CompiledOverlay();
		BlockPos from = chunkRenderIn.getPosition();
		BlockPos to = from.add(15, 15, 15);
		BlockPos fromEx = from.add(-1, -1, -1);
		BlockPos toEx = to.add(1, 1, 1);
		ChunkRenderCache chunkCache;
		SchematicWorld schematic = (SchematicWorld) this.world;

		if (from.getX() < 0
				|| from.getZ() < 0
				|| from.getX() >= schematic.getWidth()
				|| from.getZ() >= schematic.getLength()) {
			chunkRenderIn.compiledChunk.set(CompiledChunk.DUMMY);
			return;
		}

		chunkCache = ChunkRenderCache.generateCache(this.world, fromEx, toEx, 1);
		chunkRenderIn.compiledChunk.set(compiledOverlay);

		VisGraph visgraph = new VisGraph();
		if (chunkCache != null) {
			World mcWorld = Minecraft.getInstance().world;

			RenderType layer = RenderType.getTranslucent();
			BufferBuilder buffer = new BufferBuilder(128);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			GeometryTessellator.setStaticDelta(SchematicaConfig.CLIENT.blockDelta.get());
			BlockType[][][] types = new BlockType[18][18][18];

			BlockPos.Mutable mcPos = new BlockPos.Mutable();
			for (BlockPos pos : BlockPos.getAllInBoxMutable(fromEx, toEx)) {
				if (!schematic.isInside(pos) || !schematic.layerMode.shouldUseLayer(schematic, pos.getY())) {
					continue;
				}

				int secX = pos.getX() - fromEx.getX();
				int secY = pos.getY() - fromEx.getY();
				int secZ = pos.getZ() - fromEx.getZ();

				BlockState schBlockState = schematic.getBlockState(pos);
				Block schBlock = schBlockState.getBlock();

				if (schBlockState.isSolidSide(mcWorld, pos, Direction.UP)) {
					visgraph.setOpaqueCube(pos);
				}

				mcPos.setPos(pos.getX() + schematic.position.getX(), pos.getY() + schematic.position.getY(),
				             pos.getZ() + schematic.position.getZ());
				BlockState mcBlockState = mcWorld.getBlockState(mcPos);
				Block mcBlock = mcBlockState.getBlock();

				boolean isSchAirBlock = schematic.isAirBlock(pos);
				boolean isMcAirBlock = mcWorld.isAirBlock(mcPos) || SchematicaClientConfig.isExtraAirBlock(mcBlock);

				if (SchematicaConfig.CLIENT.highlightAir.get() && !isMcAirBlock && isSchAirBlock) {
					types[secX][secY][secZ] = BlockType.EXTRA_BLOCK;
				} else if (SchematicaConfig.CLIENT.highlight.get()) {
					if (!isMcAirBlock) {
						if (schBlock != mcBlock) {
							types[secX][secY][secZ] = BlockType.WRONG_BLOCK;
						} else if (schBlockState != mcBlockState) {
							types[secX][secY][secZ] = BlockType.WRONG_META;
						}
					} else if (!isSchAirBlock) {
						types[secX][secY][secZ] = BlockType.MISSING_BLOCK;
					}
				}
			}

			for (BlockPos pos : BlockPos.getAllInBoxMutable(from, to)) {
				int secX = pos.getX() - fromEx.getX();
				int secY = pos.getY() - fromEx.getY();
				int secZ = pos.getZ() - fromEx.getZ();

				BlockType type = types[secX][secY][secZ];

				if (type != null) {
					if (!compiledOverlay.isLayerStarted(layer)) {
						compiledOverlay.setLayerStarted(layer);
						preRenderBlocks(buffer, from);
					}

					int sides = getSides(types, secX, secY, secZ);
					GeometryTessellator.drawCuboid(buffer, pos, sides, 0x3F000000 | type.color);
					compiledOverlay.setLayerUsed(layer);
				}
			}
		}

		compiledOverlay.setVisibility = visgraph.computeVisibility();
	}

	@Override
	public CompletableFuture<Void> uploadChunkLayer(BufferBuilder bufferBuilderIn, VertexBuffer vertexBufferIn) {
		return super.uploadChunkLayer(bufferBuilderIn, vertexBufferIn);
	}

	private int getSides(BlockType[][][] types, int x, int y, int z) {
		// The padding cannot be rendered (it lacks neighbors)
		if (!(x > 0 && x < 17)) {
			throw new IndexOutOfBoundsException("x cannot be in padding: " + x);
		}
		if (!(y > 0 && y < 17)) {
			throw new IndexOutOfBoundsException("y cannot be in padding: " + y);
		}
		if (!(z > 0 && z < 17)) {
			throw new IndexOutOfBoundsException("z cannot be in padding: " + z);
		}

		int sides = 0;

		BlockType type = types[x][y][z];

		if (types[x][y - 1][z] != type) {
			sides |= GeometryMasks.Quad.DOWN;
		}

		if (types[x][y + 1][z] != type) {
			sides |= GeometryMasks.Quad.UP;
		}

		if (types[x][y][z - 1] != type) {
			sides |= GeometryMasks.Quad.NORTH;
		}

		if (types[x][y][z + 1] != type) {
			sides |= GeometryMasks.Quad.SOUTH;
		}

		if (types[x - 1][y][z] != type) {
			sides |= GeometryMasks.Quad.WEST;
		}

		if (types[x + 1][y][z] != type) {
			sides |= GeometryMasks.Quad.EAST;
		}

		return sides;
	}

	public void preRenderBlocks(BufferBuilder buffer, BlockPos pos) {
		buffer.begin(RenderType.getSolid().getDrawMode(), DefaultVertexFormats.POSITION_COLOR);
	}

	private enum BlockType {
		/**
		 * Purple - a block that is present in the world but not the schematic
		 */
		EXTRA_BLOCK(0xBF00BF),
		/**
		 * Red - a mismatch between the block in the world and the schematic
		 */
		WRONG_BLOCK(0xFF0000),
		/**
		 * Orange - a mismatch between the metadata for the block in the world and the schematic
		 */
		WRONG_META(0xBF5F00),
		/**
		 * Blue - a block that is present in the schematic but not in the world
		 */
		MISSING_BLOCK(0x00BFFF);

		public final int color;

		BlockType(int color) {
			this.color = color;
		}
	}
}
