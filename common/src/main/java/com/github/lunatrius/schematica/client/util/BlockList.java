package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.core.entity.EntityHelper;
import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BlockList {
	public List<WrappedItemStack> getList(Player player, FakeLevel world, Level mcWorld) {
		List<WrappedItemStack> blockList = new ArrayList<>();

		if (world == null) {
			return blockList;
		}

		MBlockPos mcPos = new MBlockPos();

		for (MBlockPos pos : BlockPosHelper.getAllInBox(BlockPos.ZERO,
				new BlockPos(world.getLevelSource().getMaxX() - 1,
						world.getHeight() - 1,
						world.getLevelSource().getMaxZ() - 1))) {
			if (!world.shouldUseLayer(pos.getY())) {
				continue;
			}

			BlockState blockState = world.getBlockState(pos);

			Block block = blockState.getBlock();

			if (blockState.isAir()) {
				continue;
			}

			mcPos.set(world.getWorldPos().offset(pos));

			BlockState mcBlockState = mcWorld.getBlockState(mcPos);
			boolean isPlaced = BlockStateHelper.areBlockStatesEqual(blockState, mcBlockState);

			ItemStack stack = ItemStack.EMPTY;

			try {
				stack = blockState.getCloneItemStack(world, pos, false);
			} catch (Exception e) {
				Reference.logger.warn("Could not get the pick block for: {}", blockState, e);
			}


			if (block instanceof LiquidBlock) {
				stack = new ItemStack(((LiquidBlock) block).arch$getFluid().getBucket());
			}

			if (stack.isEmpty()) {
				Reference.logger.warn("Could not find the item for: {}", blockState);
				continue;
			}

			int count = 1;

			// TODO: this has to be generalized for all blocks; just a temporary "fix"
			if (block instanceof SlabBlock) {
				if (blockState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE) {
					count = 2;
				}
			}

			if (block instanceof CandleBlock) {
				count = blockState.getValue(BlockStateProperties.CANDLES);
			}

			if (block instanceof SeaPickleBlock) {
				count = blockState.getValue(BlockStateProperties.PICKLES);
			}

			if (block instanceof TurtleEggBlock) {
				count = blockState.getValue(BlockStateProperties.EGGS);
			}

			if (block instanceof PinkPetalsBlock) {
				count = blockState.getValue(BlockStateProperties.FLOWER_AMOUNT);
			}

			WrappedItemStack wrappedItemStack = findOrCreateWrappedItemStackFor(blockList, stack);
			if (isPlaced) {
				wrappedItemStack.placed += count;
			}
			wrappedItemStack.itemStack.setCount(wrappedItemStack.itemStack.getCount() + count);
		}

		for (WrappedItemStack wrappedItemStack : blockList) {
			if (player.isCreative()) {
				wrappedItemStack.inventory = -1;
			} else {
				wrappedItemStack.inventory = EntityHelper.getItemCountInInventory(player.getInventory(),
						wrappedItemStack.itemStack.getItem(),
						wrappedItemStack.itemStack.getDamageValue());
			}
		}

		return blockList;
	}

	private @NotNull WrappedItemStack findOrCreateWrappedItemStackFor(@NotNull List<WrappedItemStack> blockList,
	                                                                  ItemStack itemStack) {
		for (WrappedItemStack wrappedItemStack : blockList) {
			if (wrappedItemStack.itemStack.is(itemStack.getItem())) {
				return wrappedItemStack;
			}
		}

		WrappedItemStack wrappedItemStack = new WrappedItemStack(itemStack.copy());
		blockList.add(wrappedItemStack);
		return wrappedItemStack;
	}

	public static class WrappedItemStack {
		public final ItemStack itemStack;
		public int placed;
		public int inventory;

		public WrappedItemStack(ItemStack itemStack) {
			this(itemStack, 0, itemStack.getCount());
		}

		public WrappedItemStack(@NotNull ItemStack itemStack, int placed, int total) {
			this.itemStack = itemStack;
			this.placed = placed;
			itemStack.setCount(total);
		}

		public Component getItemStackDisplayName() {
			return this.itemStack.getItem().getName(this.itemStack);
		}

		public String getFormattedAmount() {
			char color = this.placed < this.itemStack.getCount() ? 'c' : 'a';
			return String.format("§%c%s§r/%s", color, getFormattedStackAmount(this.itemStack, this.placed),
					getFormattedStackAmount(itemStack, this.itemStack.getCount()));
		}

		private static @NotNull String getFormattedStackAmount(@NotNull ItemStack itemStack, int amount) {
			int stackSize = itemStack.getMaxStackSize();
			if (amount < stackSize || stackSize == 1) {
				return String.format("%d", amount);
			} else {
				int amountStack = amount / stackSize;
				int amountRemainder = amount % stackSize;
				return String.format("%d (%d:%d)", amount, amountStack, amountRemainder);
			}
		}

		public String getFormattedAmountMissing(String strAvailable, String strMissing) {
			int need = this.itemStack.getCount() - (this.inventory + this.placed);
			if (this.inventory != -1 && need > 0) {
				return String.format("§c%s: %s", strMissing, getFormattedStackAmount(this.itemStack, need));
			} else {
				return String.format("§a%s", strAvailable);
			}
		}

		public ItemStack getItemStack() {
			return itemStack;
		}
	}
}