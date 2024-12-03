package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.schematica.handler.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public class SchematicRenderCache extends ChunkCache {
	private final Minecraft minecraft = Minecraft.getMinecraft();

	public SchematicRenderCache(World world, BlockPos from, BlockPos to, int subtract) {
		super(world, from, to, subtract);
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
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
