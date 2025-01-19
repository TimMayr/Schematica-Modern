package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@MethodsReturnNonnullByDefault
public class BlockStateToItemStack {
	public static ItemStack getItemStack(BlockState blockState, BlockHitResult rayTraceResult, SchematicWorld world,
	                                     BlockPos pos, Player player) {
		Block block = blockState.getBlock();

		try {
			ItemStack itemStack = block.getPickBlock(blockState, rayTraceResult, world, pos, player);
			if (!itemStack.isEmpty()) {
				return itemStack;
			}
		} catch (Exception e) {
			Reference.logger.debug("Could not get the pick block for: {}", blockState, e);
		}

		return ItemStack.EMPTY;
	}
}