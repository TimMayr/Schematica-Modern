package com.github.lunatrius.schematica.client.printer;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.Config.SchematicaClientConfig;
import com.github.lunatrius.schematica.Config.SchematicaConfig;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.printer.nbtsync.NBTSync;
import com.github.lunatrius.schematica.client.printer.nbtsync.SyncRegistry;
import com.github.lunatrius.schematica.client.printer.registry.PlacementData;
import com.github.lunatrius.schematica.client.printer.registry.PlacementRegistry;
import com.github.lunatrius.schematica.client.util.BlockStateToItemStack;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.*;

public class SchematicPrinter {
	public static final SchematicPrinter INSTANCE = new SchematicPrinter();

	private final Minecraft minecraft = Minecraft.getInstance();
	private final HashMap<BlockPos, Integer> syncBlacklist = new HashMap<>();
	private boolean isEnabled = true;
	private boolean isPrinting = false;
	private SchematicWorld schematic = null;
	private byte[][][] timeout = null;

	public boolean isEnabled() {
		return this.isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean togglePrinting() {
		this.isPrinting = !this.isPrinting && this.schematic != null;
		return this.isPrinting;
	}

	public boolean isPrinting() {
		return this.isPrinting;
	}

	public void setPrinting(boolean isPrinting) {
		this.isPrinting = isPrinting;
	}

	public SchematicWorld getSchematic() {
		return this.schematic;
	}

	public void setSchematic(SchematicWorld schematic) {
		this.isPrinting = false;
		this.schematic = schematic;
		refresh();
	}

	public void refresh() {
		if (this.schematic != null) {
			this.timeout = new byte[this.schematic.getWidth()][this.schematic.getHeight()][this.schematic.getLength()];
		} else {
			this.timeout = null;
		}
		this.syncBlacklist.clear();
	}

	public boolean print(ClientWorld world, ClientPlayerEntity player) {
		double dX = ClientProxy.playerPosition.x - this.schematic.position.x;
		double dY = ClientProxy.playerPosition.y - this.schematic.position.y;
		double dZ = ClientProxy.playerPosition.z - this.schematic.position.z;
		int x = (int) Math.floor(dX);
		int y = (int) Math.floor(dY);
		int z = (int) Math.floor(dZ);
		int range = SchematicaConfig.SERVER.placeDistance.get();

		int minX = Math.max(0, x - range);
		int maxX = Math.min(this.schematic.getWidth() - 1, x + range);
		int minY = Math.max(0, y - range);
		int maxY = Math.min(this.schematic.getHeight() - 1, y + range);
		int minZ = Math.max(0, z - range);
		int maxZ = Math.min(this.schematic.getLength() - 1, z + range);

		if (minX > maxX || minY > maxY || minZ > maxZ) {
			return false;
		}

		int slot = player.inventory.currentItem;
		boolean isSneaking = player.isSneaking();

		switch (schematic.layerMode) {
			case ALL:
				break;
			case SINGLE_LAYER:
				if (schematic.renderingLayer > maxY) {
					return false;
				}
				maxY = schematic.renderingLayer;
				//$FALL-THROUGH$
			case ALL_BELOW:
				if (schematic.renderingLayer < minY) {
					return false;
				}
				maxY = schematic.renderingLayer;
				break;
		}

		syncSneaking(player, true);

		double blockReachDistance =
				Objects.requireNonNull(this.minecraft.playerController).getBlockReachDistance() - 0.1;
		double blockReachDistanceSq = blockReachDistance * blockReachDistance;
		for (MBlockPos pos : BlockPosHelper.getAllInBoxXZY(minX, minY, minZ, maxX, maxY, maxZ)) {
			if (pos.distanceSq(dX, dY, dZ, true) > blockReachDistanceSq) {
				continue;
			}

			try {
				if (placeBlock(world, player, pos)) {
					return syncSlotAndSneaking(player, slot, isSneaking, true);
				}
			} catch (Exception e) {
				Reference.logger.error("Could not place block!", e);
				return syncSlotAndSneaking(player, slot, isSneaking, false);
			}
		}

		return syncSlotAndSneaking(player, slot, isSneaking, true);
	}

	private boolean syncSlotAndSneaking(ClientPlayerEntity player, int slot, boolean isSneaking, boolean success) {
		player.inventory.currentItem = slot;
		syncSneaking(player, isSneaking);
		return success;
	}

	private boolean placeBlock(ClientWorld world, ClientPlayerEntity player, BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (this.timeout[x][y][z] > 0) {
			this.timeout[x][y][z]--;
			return false;
		}

		int wx = this.schematic.position.x + x;
		int wy = this.schematic.position.y + y;
		int wz = this.schematic.position.z + z;
		BlockPos realPos = new BlockPos(wx, wy, wz);

		BlockState blockState = this.schematic.getBlockState(pos);
		BlockState realBlockState = world.getBlockState(realPos);
		Block realBlock = realBlockState.getBlock();

		if (BlockStateHelper.areBlockStatesEqual(blockState, realBlockState)) {
			// TODO: clean up this mess
			NBTSync handler = SyncRegistry.INSTANCE.getHandler(realBlock);
			if (handler != null) {
				this.timeout[x][y][z] = SchematicaConfig.SERVER.timeout.get().byteValue();

				Integer tries = this.syncBlacklist.get(realPos);
				if (tries == null) {
					tries = 0;
				} else if (tries >= 10) {
					return false;
				}

				Reference.logger.trace("Trying to sync block at {} {}", realPos, tries);
				boolean success = handler.execute(player, this.schematic, pos, world, realPos);
				if (success) {
					this.syncBlacklist.put(realPos, tries + 1);
				}

				return success;
			}

			return false;
		}

		if (SchematicaConfig.SERVER.destroyBlocks.get() && !world.isAirBlock(realPos) && Objects.requireNonNull(
				this.minecraft.playerController).isInCreativeMode()) {
			this.minecraft.playerController.clickBlock(realPos, Direction.DOWN);

			this.timeout[x][y][z] = SchematicaConfig.SERVER.timeout.get().byteValue();

			return !SchematicaConfig.SERVER.destroyInstantly.get();
		}

		if (this.schematic.isAirBlock(pos)) {
			return false;
		}

		if (!realBlockState.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, player.getActiveHand(),
		                                                                             new BlockRayTraceResult(Vec3d.ZERO,
		                                                                                                     Direction.UP,
		                                                                                                     realPos,
		                                                                                                     false))))) {
			return false;
		}

		ItemStack itemStack =
				BlockStateToItemStack.getItemStack(blockState, new EntityRayTraceResult(player), this.schematic, pos,
				                                   player);
		if (itemStack.isEmpty()) {
			Reference.logger.debug("{} is missing a mapping!", blockState);
			return false;
		}

		if (placeBlock(world, player, realPos, blockState, itemStack)) {
			this.timeout[x][y][z] = SchematicaConfig.SERVER.timeout.get().byteValue();

			return !SchematicaConfig.SERVER.placeInstantly.get();
		}

		return false;
	}

	private boolean isSolid(World world, BlockPos pos, Direction side, PlayerEntity player) {
		BlockPos offset = pos.offset(side);

		BlockState blockState = world.getBlockState(offset);
		Block block = blockState.getBlock();

		if (block.isAir(blockState, world, offset)) {
			return false;
		}

		if (block instanceof IFluidBlock) {
			return false;
		}

		return !blockState.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, player.getActiveHand(),
		                                                                            new BlockRayTraceResult(Vec3d.ZERO,
		                                                                                                    Direction.UP,
		                                                                                                    offset,
		                                                                                                    false))));
	}

	private List<Direction> getSolidSides(World world, BlockPos pos, PlayerEntity player) {
		if (!SchematicaConfig.SERVER.placeAdjacent.get()) {
			return Arrays.asList(Direction.values());
		}

		List<Direction> list = new ArrayList<Direction>();

		for (Direction side : Direction.values()) {
			if (isSolid(world, pos, side, player)) {
				list.add(side);
			}
		}

		return list;
	}

	private boolean placeBlock(ClientWorld world, ClientPlayerEntity player, BlockPos pos, BlockState blockState,
	                           ItemStack itemStack) {
		if (itemStack.getItem() instanceof BucketItem) {
			return false;
		}

		PlacementData data = PlacementRegistry.INSTANCE.getPlacementData(blockState, itemStack);
		if (data != null && !data.isValidPlayerFacing(blockState, player, pos, world)) {
			return false;
		}

		List<Direction> solidSides = getSolidSides(world, pos, player);

		if (solidSides.isEmpty()) {
			return false;
		}

		Direction direction;
		float offsetX;
		float offsetY;
		float offsetZ;
		int extraClicks;

		if (data != null) {
			List<Direction> validDirections = data.getValidBlockFacings(solidSides, blockState);
			if (validDirections.isEmpty()) {
				return false;
			}

			direction = validDirections.get(0);
			offsetX = data.getOffsetX(blockState);
			offsetY = data.getOffsetY(blockState);
			offsetZ = data.getOffsetZ(blockState);
			extraClicks = data.getExtraClicks(blockState);
		} else {
			direction = solidSides.get(0);
			offsetX = 0.5f;
			offsetY = 0.5f;
			offsetZ = 0.5f;
			extraClicks = 0;
		}

		if (!swapToItem(player.inventory, itemStack)) {
			return false;
		}

		return placeBlock(world, player, pos, direction, offsetX, offsetY, offsetZ, extraClicks);
	}

	private boolean placeBlock(ClientWorld world, ClientPlayerEntity player, BlockPos pos, Direction direction,
	                           float offsetX, float offsetY, float offsetZ, int extraClicks) {
		Hand hand = Hand.MAIN_HAND;
		ItemStack itemStack = player.getHeldItem(hand);
		boolean success;

		if (!Objects.requireNonNull(this.minecraft.playerController).isInCreativeMode()
				&& !itemStack.isEmpty()
				&& itemStack.getCount() <= extraClicks) {
			return false;
		}

		BlockPos offset = pos.offset(direction);
		Direction side = direction.getOpposite();
		Vec3d hitVec = new Vec3d(offset.getX() + offsetX, offset.getY() + offsetY, offset.getZ() + offsetZ);

		success = placeBlock(world, player, itemStack, offset, side, hitVec, hand);
		for (int i = 0; success && i < extraClicks; i++) {
			success = placeBlock(world, player, itemStack, offset, side, hitVec, hand);
		}

		if (itemStack.getCount() == 0 && success) {
			player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
		}

		return success;
	}

	private boolean placeBlock(ClientWorld world, PlayerEntity player, ItemStack itemStack, BlockPos pos,
	                           Direction side, Vec3d hitVec, Hand hand) {
		// FIXME: where did this event go?
        /*
        if (ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_BLOCK, world, pos, side, hitVec).isCanceled
        ()) {
            return false;
        }
        */

		// FIXME: when an adjacent block is not required the blocks should be placed 1 block away from the actual
		// position (because air is replaceable)
		ActionResultType result = Objects.requireNonNull(this.minecraft.playerController)
		                                 .processRightClick(player, world, hand);
		if ((result != ActionResultType.SUCCESS)) {
			return false;
		}

		player.swingArm(hand);
		return true;
	}

	private void syncSneaking(ClientPlayerEntity player, boolean isSneaking) {
		player.setSneaking(isSneaking);
		player.connection.sendPacket(new CEntityActionPacket(player, isSneaking
		                                                             ? CEntityActionPacket.Action.PRESS_SHIFT_KEY
		                                                             : CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
	}

	private boolean swapToItem(PlayerInventory inventory, ItemStack itemStack) {
		return swapToItem(inventory, itemStack, true);
	}

	private boolean swapToItem(PlayerInventory inventory, ItemStack itemStack, boolean swapSlots) {
		int slot = getInventorySlotWithItem(inventory, itemStack);

		if (this.minecraft.playerController != null
				&& this.minecraft.playerController.isInCreativeMode()
				&& (slot
						    < Constants.Inventory.InventoryOffset.HOTBAR
						    || slot
				>= Constants.Inventory.InventoryOffset.HOTBAR
				+ Constants.Inventory.Size.HOTBAR)
				&& !SchematicaClientConfig.swapSlotsQueue.isEmpty()) {
			inventory.currentItem = getNextSlot();
			inventory.setInventorySlotContents(inventory.currentItem, itemStack.copy());
			this.minecraft.playerController.sendSlotPacket(inventory.getStackInSlot(inventory.currentItem),
			                                               Constants.Inventory.SlotOffset.HOTBAR
					                                               + inventory.currentItem);
			return true;
		}

		if (slot >= Constants.Inventory.InventoryOffset.HOTBAR
				&& slot < Constants.Inventory.InventoryOffset.HOTBAR + Constants.Inventory.Size.HOTBAR) {
			inventory.currentItem = slot;
			return true;
		} else if (swapSlots
				&& slot >= Constants.Inventory.InventoryOffset.INVENTORY
				&& slot < Constants.Inventory.InventoryOffset.INVENTORY + Constants.Inventory.Size.INVENTORY) {
			if (swapSlots(slot)) {
				return swapToItem(inventory, itemStack, false);
			}
		}

		return false;
	}

	private int getInventorySlotWithItem(PlayerInventory inventory, ItemStack itemStack) {
		for (int i = 0; i < inventory.mainInventory.size(); i++) {
			if (inventory.mainInventory.get(i).isItemEqual(itemStack)) {
				return i;
			}
		}
		return -1;
	}

	private boolean swapSlots(int from) {
		if (!SchematicaClientConfig.swapSlotsQueue.isEmpty()) {
			int slot = getNextSlot();

			swapSlots(from, slot);
			return true;
		}

		return false;
	}

	@SuppressWarnings("DataFlowIssue")
	private int getNextSlot() {
		int slot = SchematicaClientConfig.swapSlotsQueue.poll() % Constants.Inventory.Size.HOTBAR;
		SchematicaClientConfig.swapSlotsQueue.offer(slot);
		return slot;
	}

	private boolean swapSlots(int from, int to) {
		return Objects.requireNonNull(this.minecraft.playerController)
		              .windowClick(Objects.requireNonNull(this.minecraft.player).container.windowId, from, to,
		                           ClickType.SWAP, this.minecraft.player) == ItemStack.EMPTY;
	}
}
