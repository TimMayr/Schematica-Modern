package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.schematica.reference.Reference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
@MethodsReturnNonnullByDefault
public class BlockStateToItemStack {
	public static ItemStack getItemStack(BlockState blockState, Level world,
	                                     BlockPos pos) {
		try {
			ItemStack itemStack = blockState.getCloneItemStack(world, pos, false);
			if (!itemStack.isEmpty()) {
				return itemStack;
			}
		} catch (Exception e) {
			Reference.logger.debug("Could not get the pick block for: {}", blockState, e);
		}

		return ItemStack.EMPTY;
	}
}