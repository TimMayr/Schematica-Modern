package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;

public class SchematicRenderCache extends ChunkRenderCache {
	private final Minecraft minecraft = Minecraft.getInstance();

	public SchematicRenderCache(World worldIn, int chunkStartXIn, int chunkStartZIn, Chunk[][] chunksIn,
	                            BlockPos startPos, BlockPos endPos) {
		super(worldIn, chunkStartXIn, chunkStartZIn, chunksIn, startPos, endPos);
	}

	@Override
	@Nonnull
	public BlockState getBlockState(@Nonnull BlockPos pos) {
		BlockPos schPos = ClientProxy.schematic.position;
		if (schPos == null) {
			return Blocks.AIR.getDefaultState();
		}

		BlockPos realPos = pos.add(schPos);
		World world = this.minecraft.world;

		if (world == null || !world.isAirBlock(realPos) && !SchematicaClientConfig.isExtraAirBlock(
				world.getBlockState(realPos).getBlock())) {
			return Blocks.AIR.getDefaultState();
		}

		return super.getBlockState(pos);
	}
}
