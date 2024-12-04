package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.core.entity.EntityHelper;
import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class BlockList {
	public List<WrappedItemStack> getList(PlayerEntity player, SchematicWorld world, World mcWorld) {
		List<WrappedItemStack> blockList = new ArrayList<>();

		if (world == null) {
			return blockList;
		}

		RayTraceResult rtr = new EntityRayTraceResult(player);
		MBlockPos mcPos = new MBlockPos();

		for (MBlockPos pos : BlockPosHelper.getAllInBox(BlockPos.ZERO,
		                                                new BlockPos(world.getWidth() - 1, world.getHeight() - 1,
		                                                             world.getLength() - 1))) {
			if (!world.layerMode.shouldUseLayer(world, pos.getY())) {
				continue;
			}

			BlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();

			if (block == Blocks.AIR || world.isAirBlock(pos)) {
				continue;
			}

			mcPos.set(world.position.add(pos));

			BlockState mcBlockState = mcWorld.getBlockState(mcPos);
			boolean isPlaced = BlockStateHelper.areBlockStatesEqual(blockState, mcBlockState);

			ItemStack stack = ItemStack.EMPTY;

			try {
				stack = block.getPickBlock(blockState, rtr, world, pos, player);
			} catch (Exception e) {
				Reference.logger.warn("Could not get the pick block for: {}", blockState, e);
			}

			if (block instanceof IFluidBlock || block instanceof FlowingFluidBlock) {
				IFluidHandler fluidHandler = FluidUtil.getFluidHandler(world, pos, null)
				                                      .orElseThrow(() -> new NullPointerException(
						                                      "Error getting FluidHandler"));
				FluidActionResult fluidActionResult =
						FluidUtil.tryFillContainer(new ItemStack(Items.BUCKET), fluidHandler, 1000, null, false);
				if (fluidActionResult.isSuccess()) {
					ItemStack result = fluidActionResult.getResult();
					if (!result.isEmpty()) {
						stack = result;
					}
				}
			}

			if (stack == null) {
				Reference.logger.error("Could not find the item for: {} (getPickBlock() returned null, this is a bug)",
				                       blockState);
				continue;
			}

			if (stack.isEmpty()) {
				Reference.logger.warn("Could not find the item for: {}", blockState);
				continue;
			}

			int count = 1;

			// TODO: this has to be generalized for all blocks; just a temporary "fix"
			if (block instanceof SlabBlock) {
				if (blockState.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE) {
					count = 2;
				}
			}

			WrappedItemStack wrappedItemStack = findOrCreateWrappedItemStackFor(blockList, stack);
			if (isPlaced) {
				wrappedItemStack.placed += count;
			}
			wrappedItemStack.total += count;
		}

		for (WrappedItemStack wrappedItemStack : blockList) {
			if (player.isCreative()) {
				wrappedItemStack.inventory = -1;
			} else {
				wrappedItemStack.inventory =
						EntityHelper.getItemCountInInventory(player.inventory, wrappedItemStack.itemStack.getItem(),
						                                     wrappedItemStack.itemStack.getDamage());
			}
		}

		return blockList;
	}

	private WrappedItemStack findOrCreateWrappedItemStackFor(List<WrappedItemStack> blockList, ItemStack itemStack) {
		for (WrappedItemStack wrappedItemStack : blockList) {
			if (wrappedItemStack.itemStack.isItemEqual(itemStack)) {
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
		public int total;
		public int inventory;

		public WrappedItemStack(ItemStack itemStack) {
			this(itemStack, 0, 0);
		}

		public WrappedItemStack(ItemStack itemStack, int placed, int total) {
			this.itemStack = itemStack;
			this.placed = placed;
			this.total = total;
		}

		public ITextComponent getItemStackDisplayName() {
			return this.itemStack.getItem().getDisplayName(this.itemStack);
		}

		public String getFormattedAmount() {
			char color = this.placed < this.total ? 'c' : 'a';
			return String.format("§%c%s§r/%s", color, getFormattedStackAmount(this.itemStack, this.placed),
			                     getFormattedStackAmount(itemStack, this.total));
		}

		private static String getFormattedStackAmount(ItemStack itemStack, int amount) {
			int stackSize = itemStack.getMaxStackSize();
			if (amount < stackSize) {
				return String.format("%d", amount);
			} else {
				int amountStack = amount / stackSize;
				int amountRemainder = amount % stackSize;
				return String.format("%d(%d:%d)", amount, amountStack, amountRemainder);
			}
		}

		public String getFormattedAmountMissing(String strAvailable, String strMissing) {
			int need = this.total - (this.inventory + this.placed);
			if (this.inventory != -1 && need > 0) {
				return String.format("§c%s: %s", strMissing, getFormattedStackAmount(this.itemStack, need));
			} else {
				return String.format("§a%s", strAvailable);
			}
		}
	}
}
