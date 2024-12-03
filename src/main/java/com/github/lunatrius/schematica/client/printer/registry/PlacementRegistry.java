package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

public class PlacementRegistry {
	public static final PlacementRegistry INSTANCE = new PlacementRegistry();

	static {
		INSTANCE.populateMappings();
	}

	private final Map<Class<? extends Block>, PlacementData> classPlacementMap = new LinkedHashMap<>();
	private final Map<Block, PlacementData> blockPlacementMap = new HashMap<>();
	private final Map<Item, PlacementData> itemPlacementMap = new HashMap<>();

	private void populateMappings() {
		this.classPlacementMap.clear();
		this.blockPlacementMap.clear();
		this.itemPlacementMap.clear();

		IValidPlayerFacing playerFacingEntity =
				(BlockState blockState, PlayerEntity player, BlockPos pos, World world) -> {
					Direction facing = blockState.get(BlockStateProperties.FACING);
					return facing == player.getHorizontalFacing();
				};

		IValidPlayerFacing playerFacingEntityOpposite =
				(BlockState blockState, PlayerEntity player, BlockPos pos, World world) -> {
					Direction facing = blockState.get(BlockStateProperties.FACING);
					return facing == player.getHorizontalFacing().getOpposite();
				};

		IValidPlayerFacing playerFacingPiston =
				(BlockState blockState, PlayerEntity player, BlockPos pos, World world) -> {
					Direction facing = blockState.get(BlockStateProperties.FACING);
					return facing == Direction.getFacingFromVector((float) player.getPosX() - pos.getX(),
					                                               (float) player.getPosY() - pos.getY(),
					                                               (float) player.getPosZ() - pos.getZ());
				};

		IValidPlayerFacing playerFacingObserver =
				(BlockState blockState, PlayerEntity player, BlockPos pos, World world) -> {
					Direction facing = blockState.get(BlockStateProperties.FACING);
					return facing == Direction.getFacingFromVector((float) player.getPosX() - pos.getX(),
					                                               (float) player.getPosY() - pos.getY(),
					                                               (float) player.getPosZ() - pos.getZ()).getOpposite();
				};

		IValidPlayerFacing playerFacingRotateY =
				(BlockState blockState, PlayerEntity player, BlockPos pos, World world) -> {
					Direction facing = blockState.get(BlockStateProperties.FACING);
					return facing == player.getHorizontalFacing().rotateY();
				};

		IValidPlayerFacing playerFacingLever =
				(BlockState blockState, PlayerEntity player, BlockPos pos, World world) -> {
					AttachFace face = blockState.get(BlockStateProperties.FACE);
					Direction facing = blockState.get(BlockStateProperties.HORIZONTAL_FACING);
					return !facing.getAxis().isVertical() || (face == AttachFace.WALL
							                                          && facing == player.getHorizontalFacing()) || (
							face != AttachFace.WALL
									&& facing == player.getHorizontalFacing().getOpposite());
				};

		IValidPlayerFacing playerFacingStandingSign =
				(BlockState blockState, PlayerEntity player, BlockPos pos, World world) -> {
					int value = blockState.get(BlockStateProperties.ROTATION_0_15);
					int facing = MathHelper.floor((player.rotationYaw + 180.0) * 16.0 / 360.0 + 0.5) & 15;
					return value == facing;
				};

		IValidPlayerFacing playerFacingIgnore =
				(BlockState state, PlayerEntity player, BlockPos pos, World world) -> false;

		IOffset offsetSlab = (BlockState blockState) -> {
			if (!(blockState.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)) {
				SlabType half = blockState.get(BlockStateProperties.SLAB_TYPE);
				return half == SlabType.TOP ? 1 : 0;
			}

			return 0;
		};

		IOffset offsetHalfBlock = (BlockState blockState) -> {
			Half half = blockState.get(BlockStateProperties.HALF);
			return half == Half.TOP ? 1 : 0;
		};

		IValidBlockFacing blockFacingAxis = (List<Direction> solidSides, BlockState blockState) -> {
			List<Direction> list = new ArrayList<>();

			Direction.Axis axis = blockState.get(BlockStateProperties.AXIS);
			for (Direction side : solidSides) {
				if (axis != side.getAxis()) {
					continue;
				}

				list.add(side);
			}

			return list;
		};

		IValidBlockFacing blockFacingOpposite = (List<Direction> solidSides, BlockState blockState) -> {
			List<Direction> list = new ArrayList<>();

			Direction facing = blockState.get(BlockStateProperties.FACING);
			for (Direction side : solidSides) {
				if (facing.getOpposite() != side) {
					continue;
				}

				list.add(side);
			}

			return list;
		};

		IValidBlockFacing blockFacingSame = (List<Direction> solidSides, BlockState blockState) -> {
			List<Direction> list = new ArrayList<>();

			Direction facing = blockState.get(BlockStateProperties.FACING);
			for (Direction side : solidSides) {
				if (facing != side) {
					continue;
				}

				list.add(side);
			}

			return list;
		};

		IExtraClick extraClickDoubleSlab =
				(BlockState blockState) -> (blockState.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE) ? 1 : 0;

		addPlacementMapping(LogBlock.class, new PlacementData(blockFacingAxis));
		addPlacementMapping(AbstractButtonBlock.class, new PlacementData(blockFacingOpposite));
		addPlacementMapping(ChestBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(DispenserBlock.class, new PlacementData(playerFacingPiston));
		addPlacementMapping(DoorBlock.class, new PlacementData(playerFacingEntity));
		addPlacementMapping(EnderChestBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(EndRodBlock.class, new PlacementData(blockFacingOpposite));
		addPlacementMapping(FenceGateBlock.class, new PlacementData(playerFacingEntity));
		addPlacementMapping(FurnaceBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(HopperBlock.class, new PlacementData(blockFacingSame));
		addPlacementMapping(ObserverBlock.class, new PlacementData(playerFacingObserver));
		addPlacementMapping(PistonBlock.class, new PlacementData(playerFacingPiston));
		addPlacementMapping(PumpkinBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(RotatedPillarBlock.class, new PlacementData(blockFacingAxis));
		addPlacementMapping(SlabBlock.class,
		                    new PlacementData().setOffsetY(offsetSlab).setExtraClick(extraClickDoubleSlab));
		addPlacementMapping(StairsBlock.class, new PlacementData(playerFacingEntity).setOffsetY(offsetHalfBlock));
		addPlacementMapping(TorchBlock.class, new PlacementData(blockFacingOpposite));
		addPlacementMapping(TrapDoorBlock.class, new PlacementData(blockFacingOpposite).setOffsetY(offsetHalfBlock));
		addPlacementMapping(StandingSignBlock.class, new PlacementData(playerFacingStandingSign));
		addPlacementMapping(WallSignBlock.class, new PlacementData(playerFacingStandingSign));
		addPlacementMapping(TripWireHookBlock.class, new PlacementData(blockFacingOpposite));
		addPlacementMapping(AnvilBlock.class, new PlacementData(playerFacingRotateY));
		addPlacementMapping(CocoaBlock.class, new PlacementData(blockFacingSame));
		addPlacementMapping(EndPortalFrameBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(LadderBlock.class, new PlacementData(blockFacingOpposite));
		addPlacementMapping(LeverBlock.class, new PlacementData(playerFacingLever, blockFacingOpposite));
		addPlacementMapping(RedstoneDiodeBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(BedBlock.class, new PlacementData(playerFacingIgnore));
		addPlacementMapping(PistonHeadBlock.class, new PlacementData(playerFacingIgnore));
		addPlacementMapping(EndPortalBlock.class, new PlacementData(playerFacingIgnore));
		addPlacementMapping(NetherPortalBlock.class, new PlacementData(playerFacingIgnore));
		addPlacementMapping(SkullBlock.class, new PlacementData(playerFacingIgnore));
		addPlacementMapping(WallSkullBlock.class, new PlacementData(playerFacingIgnore));
		addPlacementMapping(BannerBlock.class, new PlacementData(playerFacingIgnore));
		addPlacementMapping(WallBannerBlock.class, new PlacementData(playerFacingIgnore));

		addPlacementMapping(Blocks.CHAIN_COMMAND_BLOCK, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(Blocks.REPEATING_COMMAND_BLOCK, new PlacementData(playerFacingEntityOpposite));
	}

	private PlacementData addPlacementMapping(Class<? extends Block> clazz, PlacementData data) {
		if (clazz == null || data == null) {
			return null;
		}

		return this.classPlacementMap.put(clazz, data);
	}

	private PlacementData addPlacementMapping(Block block, PlacementData data) {
		if (block == null || data == null) {
			return null;
		}

		return this.blockPlacementMap.put(block, data);
	}

	private PlacementData addPlacementMapping(Item item, PlacementData data) {
		if (item == null || data == null) {
			return null;
		}

		return this.itemPlacementMap.put(item, data);
	}

	public PlacementData getPlacementData(BlockState blockState, ItemStack itemStack) {
		Item item = itemStack.getItem();

		PlacementData placementDataItem = this.itemPlacementMap.get(item);
		if (placementDataItem != null) {
			return placementDataItem;
		}

		Block block = blockState.getBlock();

		PlacementData placementDataBlock = this.blockPlacementMap.get(block);
		if (placementDataBlock != null) {
			return placementDataBlock;
		}

		for (Class<? extends Block> clazz : this.classPlacementMap.keySet()) {
			if (clazz.isInstance(block)) {
				return this.classPlacementMap.get(clazz);
			}
		}

		return null;
	}
}
