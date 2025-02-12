package com.github.lunatrius.schematica.client.printer;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.printer.nbtsync.NBTSync;
import com.github.lunatrius.schematica.client.printer.nbtsync.SyncRegistry;
import com.github.lunatrius.schematica.client.printer.registry.PlacementData;
import com.github.lunatrius.schematica.client.printer.registry.PlacementRegistry;
import com.github.lunatrius.schematica.client.util.BlockStateToItemStack;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE;

@Environment(EnvType.CLIENT)
public class SchematicPrinter {
	public static final SchematicPrinter INSTANCE = new SchematicPrinter();

	private final Minecraft minecraft = Minecraft.getInstance();
	private final HashMap<BlockPos, Integer> syncBlacklist = new HashMap<>();
	private boolean isEnabled = true;
	private boolean isPrinting = false;
	private FakeLevel schematic = null;
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

	public FakeLevel getSchematic() {
		return this.schematic;
	}

	public void setSchematic(FakeLevel schematic) {
		this.isPrinting = false;
		this.schematic = schematic;
		refresh();
	}

	public void refresh() {
		if (this.schematic != null) {
			this.timeout = new byte[this.schematic.getLevelSource()
			                                      .getMaxX()][this.schematic.getHeight()][this.schematic.getLevelSource()
			                                                                                            .getMaxZ()];
		} else {
			this.timeout = null;
		}
		this.syncBlacklist.clear();
	}

	public void print(ClientLevel level, LocalPlayer player) {
		double dX = ClientProxy.playerPosition.x - this.schematic.getWorldPos().x;
		double dY = ClientProxy.playerPosition.y - this.schematic.getWorldPos().y;
		double dZ = ClientProxy.playerPosition.z - this.schematic.getWorldPos().z;
		int x = (int) Math.floor(dX);
		int y = (int) Math.floor(dY);
		int z = (int) Math.floor(dZ);
		int range = SchematicaClientConfig.CLIENT.placeDistance.get();

		int minX = Math.max(0, x - range);
		int maxX = Math.min(this.schematic.getLevelSource().getMaxX() - 1, x + range);
		int minY = Math.max(0, y - range);
		int maxY = Math.min(this.schematic.getHeight() - 1, y + range);
		int minZ = Math.max(0, z - range);
		int maxZ = Math.min(this.schematic.getLevelSource().getMaxZ() - 1, z + range);

		if (minX > maxX || minY > maxY || minZ > maxZ) {
			return;
		}

		int slot = player.getInventory().selected;
		boolean isSneaking = player.isCrouching();

		switch (schematic.layerMode) {
			case ALL:
				break;
			case SINGLE_LAYER:
				if (schematic.renderLayer > maxY) {
					return;
				}
			case ALL_BELOW:
				if (schematic.renderLayer < minY) {
					return;
				}
				maxY = schematic.renderLayer;
				break;
		}

		syncSneaking(player, true);

		double blockReachDistance = this.minecraft.player.getAttributeValue(ENTITY_INTERACTION_RANGE);
		double blockReachDistanceSq = blockReachDistance * blockReachDistance;
		for (MBlockPos pos : BlockPosHelper.getAllInBoxXZY(minX, minY, minZ, maxX, maxY, maxZ)) {
			if (pos.distSqr(new Vec3i((int) Math.floor(dX), (int) Math.floor(dY), (int) Math.floor(dZ)))
					> blockReachDistanceSq) {
				continue;
			}

			try {
				if (placeBlock(level, player, pos)) {
					syncSlotAndSneaking(player, slot, isSneaking, true);
					return;
				}
			} catch (Exception e) {
				Reference.logger.error("Could not place block!", e);
				syncSlotAndSneaking(player, slot, isSneaking, false);
				return;
			}
		}

		syncSlotAndSneaking(player, slot, isSneaking, true);
	}

	private void syncSlotAndSneaking(@NotNull LocalPlayer player, int slot, boolean isSneaking, boolean success) {
		player.getInventory().selected = slot;
		syncSneaking(player, isSneaking);
	}

	private boolean placeBlock(ClientLevel level, LocalPlayer player, @NotNull BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (this.timeout[x][y][z] > 0) {
			this.timeout[x][y][z]--;
			return false;
		}

		int wx = this.schematic.getWorldPos().x + x;
		int wy = this.schematic.getWorldPos().y + y;
		int wz = this.schematic.getWorldPos().z + z;
		BlockPos realPos = new BlockPos(wx, wy, wz);

		BlockState blockState = this.schematic.getBlockState(pos);
		BlockState realBlockState = level.getBlockState(realPos);
		Block realBlock = realBlockState.getBlock();

		if (BlockStateHelper.areBlockStatesEqual(blockState, realBlockState)) {
			NBTSync handler = SyncRegistry.INSTANCE.getHandler(realBlock);
			if (handler != null) {
				this.timeout[x][y][z] = SchematicaClientConfig.CLIENT.timeout.get().byteValue();

				Integer tries = this.syncBlacklist.get(realPos);
				if (tries == null) {
					tries = 0;
				} else if (tries >= 10) {
					return false;
				}

				Reference.logger.trace("Trying to sync block at {} {}", realPos, tries);
				boolean success = handler.execute(player, this.schematic, pos, level, realPos);
				if (success) {
					this.syncBlacklist.put(realPos, tries + 1);
				}

				return success;
			}

			return false;
		}

		if (SchematicaClientConfig.CLIENT.destroyBlocks.get()
				&& !level.getBlockState(realPos).isAir()
				&& player.isCreative()) {
			//TODO: Probably also discord
			this.minecraft.gameMode.startDestroyBlock(realPos, Direction.DOWN);

			this.timeout[x][y][z] = SchematicaClientConfig.CLIENT.timeout.get().byteValue();

			return !SchematicaClientConfig.CLIENT.destroyInstantly.get();
		}

		if (this.schematic.getLevelSource().getBlockState(pos).isAir()) {
			return false;
		}

		if (!realBlockState.canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND,
		                                                                         new BlockHitResult(Vec3.ZERO,
		                                                                                            Direction.UP,
		                                                                                            realPos,
		                                                                                            false))))) {
			return false;
		}

		ItemStack itemStack = BlockStateToItemStack.getItemStack(blockState, this.schematic, pos);
		if (itemStack.isEmpty()) {
			Reference.logger.debug("{} is missing a mapping!", blockState);
			return false;
		}

		if (placeBlock(level, player, realPos, blockState, itemStack)) {
			this.timeout[x][y][z] = SchematicaClientConfig.CLIENT.timeout.get().byteValue();

			return !SchematicaClientConfig.CLIENT.placeInstantly.get();
		}

		return false;
	}

	private @NotNull List<Direction> getSolidSides(Level level, BlockPos pos, Player player) {
		if (!SchematicaClientConfig.CLIENT.placeAdjacent.get()) {
			return Arrays.asList(Direction.values());
		}

		List<Direction> list = new ArrayList<>();

		for (Direction side : Direction.values()) {
			if (isSolid(level, pos, side, player)) {
				list.add(side);
			}
		}

		return list;
	}

	private boolean isSolid(@NotNull Level level, @NotNull BlockPos pos, @NotNull Direction side, Player player) {
		BlockPos offset = pos.offset(side.getUnitVec3i());

		BlockState blockState = level.getBlockState(offset);
		Block block = blockState.getBlock();

		if (blockState.isAir()) {
			return false;
		}

		if (block instanceof LiquidBlock) {
			return false;
		}

		return !blockState.canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND,
		                                                                        new BlockHitResult(Vec3.ZERO,
		                                                                                           Direction.UP,
		                                                                                           offset,
		                                                                                           false))));
	}

	private boolean placeBlock(ClientLevel level, LocalPlayer player, BlockPos pos, BlockState blockState,
	                           @NotNull ItemStack itemStack) {
		if (itemStack.getItem() instanceof BucketItem) {
			return false;
		}

		PlacementData data = PlacementRegistry.INSTANCE.getPlacementData(blockState, itemStack);
		if (data != null && !data.isValidPlayerFacing(blockState, player, pos, level)) {
			return false;
		}

		List<Direction> solidSides = getSolidSides(level, pos, player);

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

			direction = validDirections.getFirst();
			offsetX = data.getOffsetX(blockState);
			offsetY = data.getOffsetY(blockState);
			offsetZ = data.getOffsetZ(blockState);
			extraClicks = data.getExtraClicks(blockState);
		} else {
			direction = solidSides.getFirst();
			offsetX = 0.5f;
			offsetY = 0.5f;
			offsetZ = 0.5f;
			extraClicks = 0;
		}

		if (!swapToItem(player.getInventory(), itemStack)) {
			return false;
		}

		return placeBlock(level, player, pos, direction, offsetX, offsetY, offsetZ, extraClicks);
	}

	private boolean placeBlock(ClientLevel level, LocalPlayer player, BlockPos pos, Direction direction, float offsetX,
	                           float offsetY, float offsetZ, int extraClicks) {
		InteractionHand hand = InteractionHand.MAIN_HAND;
		ItemStack itemStack = player.getItemInHand(hand);
		boolean success;

		if (!this.minecraft.player.isCreative() && !itemStack.isEmpty() && itemStack.getCount() <= extraClicks) {
			return false;
		}

		BlockPos offset = pos.offset(direction.getUnitVec3i());
		Direction side = direction.getOpposite();
		Vec3 hitVec = new Vec3(offset.getX() + offsetX, offset.getY() + offsetY, offset.getZ() + offsetZ);

		success = placeBlock(level, player, itemStack, offset, side, hitVec, hand);
		for (int i = 0; success && i < extraClicks; i++) {
			success = placeBlock(level, player, itemStack, offset, side, hitVec, hand);
		}

		if (itemStack.getCount() == 0 && success) {
			player.getInventory().items.set(player.getInventory().selected, ItemStack.EMPTY);
		}

		return success;
	}

	private boolean placeBlock(ClientLevel level, Player player, ItemStack itemStack, BlockPos pos, Direction side,
	                           Vec3 hitVec, InteractionHand hand) {
		if (itemStack.getItem() instanceof BlockItem blockItem) {
			BlockHitResult hitResult = new BlockHitResult(hitVec, side, pos, false);

			// Create a BlockItemUseContext
			BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));

			// Use the BlockItem's place method to try to place the block
			InteractionResult result = blockItem.place(context);

			if (result != InteractionResult.SUCCESS) {
				return false;
			}

			// Perform a swing animation to match the player action (optional)
			player.swing(hand);
			return true;
		}

		return false;
	}

	private void syncSneaking(@NotNull LocalPlayer player, boolean isSneaking) {
		player.setShiftKeyDown(isSneaking);
		player.connection.send(
				new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, isSneaking, false)));
	}

	private boolean swapToItem(Inventory inventory, ItemStack itemStack) {
		return swapToItem(inventory, itemStack, true);
	}

	private boolean swapToItem(Inventory inventory, ItemStack itemStack, boolean swapSlots) {
		int slot = getInventorySlotWithItem(inventory, itemStack);

		if (this.minecraft.player.isCreative()
				&& (slot < Constants.Inventory.InventoryOffset.HOTBAR
						    || slot
				>= Constants.Inventory.InventoryOffset.HOTBAR + Constants.Inventory.Size.HOTBAR)
				&& !SchematicaClientConfig.swapSlotsQueue.isEmpty()) {
			inventory.selected = getNextSlot();
			inventory.setItem(inventory.selected, itemStack.copy());
			this.minecraft.player.connection.send(
					new ServerboundSetCreativeModeSlotPacket(Constants.Inventory.SlotOffset.HOTBAR + inventory.selected,
					                                         inventory.getItem(inventory.selected)));
			return true;
		}

		if (slot >= Constants.Inventory.InventoryOffset.HOTBAR
				&& slot < Constants.Inventory.InventoryOffset.HOTBAR + Constants.Inventory.Size.HOTBAR) {
			inventory.selected = slot;
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

	private int getInventorySlotWithItem(@NotNull Inventory inventory, ItemStack itemStack) {
		for (int i = 0; i < inventory.items.size(); i++) {
			if (inventory.items.get(i).is(itemStack.getItem())) {
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

	private int getNextSlot() {
		int slot = SchematicaClientConfig.swapSlotsQueue.poll() % Constants.Inventory.Size.HOTBAR;
		SchematicaClientConfig.swapSlotsQueue.offer(slot);
		return slot;
	}

	private void swapSlots(int from, int to) {
		AbstractContainerMenu container = this.minecraft.player.containerMenu;
		container.clicked(from, to, ClickType.SWAP, this.minecraft.player);
	}
}