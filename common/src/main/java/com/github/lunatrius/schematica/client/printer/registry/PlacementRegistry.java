package com.github.lunatrius.schematica.client.printer.registry;

import com.github.lunatrius.core.util.math.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.NotNull;

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

		IValidPlayerFacing playerFacingEntity = (BlockState blockState, Player player, BlockPos pos, Level world) -> {
			Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
			return facing == player.getDirection();
		};

		IValidPlayerFacing playerFacingEntityOpposite =
				(BlockState blockState, Player player, BlockPos pos, Level world) -> {
					Direction facing = blockState.getValue(BlockStateProperties.FACING);
					return facing == player.getDirection().getOpposite();
				};

		IValidPlayerFacing playerFacingPiston = (BlockState blockState, Player player, BlockPos pos, Level world) -> {
			Direction facing = blockState.getValue(BlockStateProperties.FACING);
			return facing == Direction.getApproximateNearest((float) player.getX() - pos.getX(),
			                                                 (float) player.getY() - pos.getY(),
			                                                 (float) player.getZ() - pos.getZ());
		};

		IValidPlayerFacing playerFacingObserver =
				(BlockState blockState, Player player, BlockPos pos, Level world) -> {
			Direction facing = blockState.getValue(BlockStateProperties.FACING);
			return facing == Direction.getApproximateNearest((float) player.getX() - pos.getX(),
			                                                 (float) player.getY() - pos.getY(),
			                                                 (float) player.getZ() - pos.getZ()).getOpposite();
		};

		IValidPlayerFacing playerFacingRotateY = (BlockState blockState, Player player, BlockPos pos, Level world) -> {
			Direction facing = blockState.getValue(BlockStateProperties.FACING);
			return facing == player.getDirection();
		};

		IValidPlayerFacing playerFacingLever = (BlockState blockState, Player player, BlockPos pos, Level world) -> {
			AttachFace face = blockState.getValue(BlockStateProperties.ATTACH_FACE);
			Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
			return !facing.getAxis().isVertical() || (face == AttachFace.WALL && facing == player.getDirection()) || (
					face != AttachFace.WALL
							&& facing == player.getDirection().getOpposite());
		};

		IValidPlayerFacing playerFacingStandingSign =
				(BlockState blockState, Player player, BlockPos pos, Level world) -> {
					int value = blockState.getValue(BlockStateProperties.ROTATION_16);
					int facing = MathHelper.floor((float) ((player.yHeadRot + 180.0) * 16.0 / 360.0 + 0.5)) & 15;
					return value == facing;
				};

		IValidPlayerFacing playerFacingIgnore = (BlockState state, Player player, BlockPos pos, Level world) -> false;

		IOffset offsetSlab = (BlockState blockState) -> {
			if (!(blockState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)) {
				SlabType half = blockState.getValue(BlockStateProperties.SLAB_TYPE);
				return half == SlabType.TOP ? 1 : 0;
			}

			return 0;
		};

		IOffset offsetHalfBlock = (BlockState blockState) -> {
			Half half = blockState.getValue(BlockStateProperties.HALF);
			return half == Half.TOP ? 1 : 0;
		};

		IValidBlockFacing blockFacingAxis = (List<Direction> solidSides, BlockState blockState) -> {
			List<Direction> list = new ArrayList<>();

			Direction.Axis axis = blockState.getValue(BlockStateProperties.AXIS);
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

			Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
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

			Direction facing = blockState.getValue(BlockStateProperties.FACING);
			for (Direction side : solidSides) {
				if (facing != side) {
					continue;
				}

				list.add(side);
			}

			return list;
		};

		IExtraClick extraClickDoubleSlab =
				(BlockState blockState) -> (blockState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
				                           ? 1
				                           : 0;


		addPlacementMapping(FaceAttachedHorizontalDirectionalBlock.class, new PlacementData(blockFacingOpposite));
		addPlacementMapping(ChestBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(DispenserBlock.class, new PlacementData(playerFacingPiston));
		addPlacementMapping(DoorBlock.class, new PlacementData(playerFacingEntity));
		addPlacementMapping(EnderChestBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(EndRodBlock.class, new PlacementData(blockFacingOpposite));
		addPlacementMapping(FenceGateBlock.class, new PlacementData(playerFacingEntity));
		addPlacementMapping(FurnaceBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(HopperBlock.class, new PlacementData(blockFacingSame));
		addPlacementMapping(ObserverBlock.class, new PlacementData(playerFacingObserver));
		addPlacementMapping(PistonBaseBlock.class, new PlacementData(playerFacingPiston));
		addPlacementMapping(CarvedPumpkinBlock.class, new PlacementData(playerFacingEntityOpposite));
		addPlacementMapping(RotatedPillarBlock.class, new PlacementData(blockFacingAxis));
		addPlacementMapping(SlabBlock.class,
		                    new PlacementData().setOffsetY(offsetSlab).setExtraClick(extraClickDoubleSlab));
		addPlacementMapping(StairBlock.class, new PlacementData(playerFacingEntity).setOffsetY(offsetHalfBlock));
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
		addPlacementMapping(DiodeBlock.class, new PlacementData(playerFacingEntityOpposite));
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

	private void addPlacementMapping(Class<? extends Block> clazz, PlacementData data) {
		if (clazz == null || data == null) {
			return;
		}

		this.classPlacementMap.put(clazz, data);
	}

	private void addPlacementMapping(Block block, PlacementData data) {
		if (block == null || data == null) {
			return;
		}

		this.blockPlacementMap.put(block, data);
	}

	private PlacementData addPlacementMapping(Item item, PlacementData data) {
		if (item == null || data == null) {
			return null;
		}

		return this.itemPlacementMap.put(item, data);
	}

	public PlacementData getPlacementData(BlockState blockState, @NotNull ItemStack itemStack) {
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