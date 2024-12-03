package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class RotationHelper {
	private static final EnumFacing[][] FACINGS = new EnumFacing[EnumFacing.VALUES.length][];
	private static final EnumFacing.Axis[][] AXISES = new EnumFacing.Axis[EnumFacing.Axis.values().length][];
	private static final BlockLog.EnumAxis[][] AXISES_LOG = new BlockLog.EnumAxis[EnumFacing.Axis.values().length][];
	private static final BlockQuartz.EnumType[][] AXISES_QUARTZ =
			new BlockQuartz.EnumType[EnumFacing.Axis.values().length][];
	public static final RotationHelper INSTANCE = new RotationHelper();

	static {
		FACINGS[EnumFacing.DOWN.ordinal()] = new EnumFacing[] {EnumFacing.DOWN,
		                                                       EnumFacing.UP,
		                                                       EnumFacing.WEST,
		                                                       EnumFacing.EAST,
		                                                       EnumFacing.SOUTH,
		                                                       EnumFacing.NORTH};
		FACINGS[EnumFacing.UP.ordinal()] = new EnumFacing[] {EnumFacing.DOWN,
		                                                     EnumFacing.UP,
		                                                     EnumFacing.EAST,
		                                                     EnumFacing.WEST,
		                                                     EnumFacing.NORTH,
		                                                     EnumFacing.SOUTH};
		FACINGS[EnumFacing.NORTH.ordinal()] = new EnumFacing[] {EnumFacing.EAST,
		                                                        EnumFacing.WEST,
		                                                        EnumFacing.NORTH,
		                                                        EnumFacing.SOUTH,
		                                                        EnumFacing.DOWN,
		                                                        EnumFacing.UP};
		FACINGS[EnumFacing.SOUTH.ordinal()] = new EnumFacing[] {EnumFacing.WEST,
		                                                        EnumFacing.EAST,
		                                                        EnumFacing.NORTH,
		                                                        EnumFacing.SOUTH,
		                                                        EnumFacing.UP,
		                                                        EnumFacing.DOWN};
		FACINGS[EnumFacing.WEST.ordinal()] = new EnumFacing[] {EnumFacing.NORTH,
		                                                       EnumFacing.SOUTH,
		                                                       EnumFacing.UP,
		                                                       EnumFacing.DOWN,
		                                                       EnumFacing.WEST,
		                                                       EnumFacing.EAST};
		FACINGS[EnumFacing.EAST.ordinal()] = new EnumFacing[] {EnumFacing.SOUTH,
		                                                       EnumFacing.NORTH,
		                                                       EnumFacing.DOWN,
		                                                       EnumFacing.UP,
		                                                       EnumFacing.WEST,
		                                                       EnumFacing.EAST};

		AXISES[EnumFacing.Axis.X.ordinal()] =
				new EnumFacing.Axis[] {EnumFacing.Axis.X, EnumFacing.Axis.Z, EnumFacing.Axis.Y};
		AXISES[EnumFacing.Axis.Y.ordinal()] =
				new EnumFacing.Axis[] {EnumFacing.Axis.Z, EnumFacing.Axis.Y, EnumFacing.Axis.X};
		AXISES[EnumFacing.Axis.Z.ordinal()] =
				new EnumFacing.Axis[] {EnumFacing.Axis.Y, EnumFacing.Axis.X, EnumFacing.Axis.Z};

		AXISES_LOG[EnumFacing.Axis.X.ordinal()] = new BlockLog.EnumAxis[] {BlockLog.EnumAxis.X,
		                                                                   BlockLog.EnumAxis.Z,
		                                                                   BlockLog.EnumAxis.Y,
		                                                                   BlockLog.EnumAxis.NONE};
		AXISES_LOG[EnumFacing.Axis.Y.ordinal()] = new BlockLog.EnumAxis[] {BlockLog.EnumAxis.Z,
		                                                                   BlockLog.EnumAxis.Y,
		                                                                   BlockLog.EnumAxis.X,
		                                                                   BlockLog.EnumAxis.NONE};
		AXISES_LOG[EnumFacing.Axis.Z.ordinal()] = new BlockLog.EnumAxis[] {BlockLog.EnumAxis.Y,
		                                                                   BlockLog.EnumAxis.X,
		                                                                   BlockLog.EnumAxis.Z,
		                                                                   BlockLog.EnumAxis.NONE};

		AXISES_QUARTZ[EnumFacing.Axis.X.ordinal()] = new BlockQuartz.EnumType[] {BlockQuartz.EnumType.DEFAULT,
		                                                                         BlockQuartz.EnumType.CHISELED,
		                                                                         BlockQuartz.EnumType.LINES_Z,
		                                                                         BlockQuartz.EnumType.LINES_X,
		                                                                         BlockQuartz.EnumType.LINES_Y};
		AXISES_QUARTZ[EnumFacing.Axis.Y.ordinal()] = new BlockQuartz.EnumType[] {BlockQuartz.EnumType.DEFAULT,
		                                                                         BlockQuartz.EnumType.CHISELED,
		                                                                         BlockQuartz.EnumType.LINES_Y,
		                                                                         BlockQuartz.EnumType.LINES_Z,
		                                                                         BlockQuartz.EnumType.LINES_X};
		AXISES_QUARTZ[EnumFacing.Axis.Z.ordinal()] = new BlockQuartz.EnumType[] {BlockQuartz.EnumType.DEFAULT,
		                                                                         BlockQuartz.EnumType.CHISELED,
		                                                                         BlockQuartz.EnumType.LINES_X,
		                                                                         BlockQuartz.EnumType.LINES_Y,
		                                                                         BlockQuartz.EnumType.LINES_Z};
	}

	public boolean rotate(SchematicWorld world, EnumFacing axis, boolean forced) {
		if (world == null) {
			return false;
		}

		try {
			ISchematic schematic = world.getSchematic();
			Schematic schematicRotated = rotate(schematic, axis, forced);

			updatePosition(world, axis);

			world.setSchematic(schematicRotated);

			for (TileEntity tileEntity : world.getTileEntities()) {
				world.initializeTileEntity(tileEntity);
			}

			return true;
		} catch (RotationException re) {
			Reference.logger.error(re.getMessage());
		} catch (Exception e) {
			Reference.logger.fatal("Something went wrong!", e);
		}

		return false;
	}

	private void updatePosition(SchematicWorld world, EnumFacing axis) {
		switch (axis) {
			case DOWN:
			case UP: {
				int offset = (world.getWidth() - world.getLength()) / 2;
				world.position.x += offset;
				world.position.z -= offset;
				break;
			}

			case NORTH:
			case SOUTH: {
				int offset = (world.getWidth() - world.getHeight()) / 2;
				world.position.x += offset;
				world.position.y -= offset;
				break;
			}

			case WEST:
			case EAST: {
				int offset = (world.getHeight() - world.getLength()) / 2;
				world.position.y += offset;
				world.position.z -= offset;
				break;
			}
		}
	}

	public Schematic rotate(ISchematic schematic, EnumFacing axis, boolean forced) throws RotationException {
		Vec3i dimensionsRotated =
				rotateDimensions(axis, schematic.getWidth(), schematic.getHeight(), schematic.getLength());
		Schematic schematicRotated =
				new Schematic(schematic.getIcon(), dimensionsRotated.getX(), dimensionsRotated.getY(),
				              dimensionsRotated.getZ(), schematic.getAuthor());
		MBlockPos tmp = new MBlockPos();

		for (MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, schematic.getWidth() - 1, schematic.getHeight() - 1,
		                                                schematic.getLength() - 1)) {
			IBlockState blockState = schematic.getBlockState(pos);
			IBlockState blockStateRotated = rotateBlock(blockState, axis, forced);
			schematicRotated.setBlockState(rotatePos(pos, axis, dimensionsRotated, tmp), blockStateRotated);
		}

		List<TileEntity> tileEntities = schematic.getTileEntities();
		for (TileEntity tileEntity : tileEntities) {
			BlockPos pos = tileEntity.getPos();
			tileEntity.setPos(new BlockPos(rotatePos(pos, axis, dimensionsRotated, tmp)));
			schematicRotated.setTileEntity(tileEntity.getPos(), tileEntity);
		}

		return schematicRotated;
	}

	private Vec3i rotateDimensions(EnumFacing axis, int width, int height, int length) throws RotationException {
		switch (axis) {
			case DOWN:
			case UP:
				return new Vec3i(length, height, width);

			case NORTH:
			case SOUTH:
				return new Vec3i(height, width, length);

			case WEST:
			case EAST:
				return new Vec3i(width, length, height);
		}

		throw new RotationException("'%s' is not a valid axis!", axis.getName());
	}

	private BlockPos rotatePos(BlockPos pos, EnumFacing axis, Vec3i dimensions, MBlockPos rotated)
			throws RotationException {
		switch (axis) {
			case DOWN:
				return rotated.set(pos.getZ(), pos.getY(), dimensions.getZ() - 1 - pos.getX());

			case UP:
				return rotated.set(dimensions.getX() - 1 - pos.getZ(), pos.getY(), pos.getX());

			case NORTH:
				return rotated.set(dimensions.getX() - 1 - pos.getY(), pos.getX(), pos.getZ());

			case SOUTH:
				return rotated.set(pos.getY(), dimensions.getY() - 1 - pos.getX(), pos.getZ());

			case WEST:
				return rotated.set(pos.getX(), dimensions.getY() - 1 - pos.getZ(), pos.getY());

			case EAST:
				return rotated.set(pos.getX(), pos.getZ(), dimensions.getZ() - 1 - pos.getY());
		}

		throw new RotationException("'%s' is not a valid axis!", axis.getName());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private IBlockState rotateBlock(IBlockState blockState, EnumFacing axisRotation, boolean forced)
			throws RotationException {
		IProperty propertyFacing = BlockStateHelper.getProperty(blockState, "facing");
		if (propertyFacing instanceof PropertyDirection) {
			Comparable value = blockState.getValue(propertyFacing);
			if (value instanceof EnumFacing) {
				EnumFacing facing = getRotatedFacing(axisRotation, (EnumFacing) value);
				if (propertyFacing.getAllowedValues().contains(facing)) {
					return blockState.withProperty(propertyFacing, facing);
				}
			}
		} else if (propertyFacing instanceof PropertyEnum) {
			if (BlockLever.EnumOrientation.class.isAssignableFrom(propertyFacing.getValueClass())) {
				BlockLever.EnumOrientation orientation =
						(BlockLever.EnumOrientation) blockState.getValue(propertyFacing);
				BlockLever.EnumOrientation orientationRotated = getRotatedLeverFacing(axisRotation, orientation);
				if (propertyFacing.getAllowedValues().contains(orientationRotated)) {
					return blockState.withProperty(propertyFacing, orientationRotated);
				}
			}
		} else if (propertyFacing != null) {
			Reference.logger.error("'{}': found 'facing' property with unknown type {}",
			                       Block.REGISTRY.getNameForObject(blockState.getBlock()),
			                       propertyFacing.getClass().getSimpleName());
		}

		IProperty propertyAxis = BlockStateHelper.getProperty(blockState, "axis");
		if (propertyAxis instanceof PropertyEnum) {
			if (EnumFacing.Axis.class.isAssignableFrom(propertyAxis.getValueClass())) {
				EnumFacing.Axis axis = (EnumFacing.Axis) blockState.getValue(propertyAxis);
				EnumFacing.Axis axisRotated = getRotatedAxis(axisRotation, axis);
				return blockState.withProperty(propertyAxis, axisRotated);
			}

			if (BlockLog.EnumAxis.class.isAssignableFrom(propertyAxis.getValueClass())) {
				BlockLog.EnumAxis axis = (BlockLog.EnumAxis) blockState.getValue(propertyAxis);
				BlockLog.EnumAxis axisRotated = getRotatedLogAxis(axisRotation, axis);
				return blockState.withProperty(propertyAxis, axisRotated);
			}
		} else if (propertyAxis != null) {
			Reference.logger.error("'{}': found 'axis' property with unknown type {}",
			                       Block.REGISTRY.getNameForObject(blockState.getBlock()),
			                       propertyAxis.getClass().getSimpleName());
		}

		IProperty propertyVariant = BlockStateHelper.getProperty(blockState, "variant");
		if (propertyVariant instanceof PropertyEnum) {
			if (BlockQuartz.EnumType.class.isAssignableFrom(propertyVariant.getValueClass())) {
				BlockQuartz.EnumType type = (BlockQuartz.EnumType) blockState.getValue(propertyVariant);
				BlockQuartz.EnumType typeRotated = getRotatedQuartzType(axisRotation, type);
				return blockState.withProperty(propertyVariant, typeRotated);
			}
		}

		if (!forced && (propertyFacing != null || propertyAxis != null)) {
			throw new RotationException("'%s' cannot be rotated around '%s'",
			                            Block.REGISTRY.getNameForObject(blockState.getBlock()), axisRotation);
		}

		return blockState;
	}

	private static EnumFacing getRotatedFacing(EnumFacing source, EnumFacing side) {
		return FACINGS[source.ordinal()][side.ordinal()];
	}

	private static EnumFacing.Axis getRotatedAxis(EnumFacing source, EnumFacing.Axis axis) {
		return AXISES[source.getAxis().ordinal()][axis.ordinal()];
	}

	private static BlockLog.EnumAxis getRotatedLogAxis(EnumFacing source, BlockLog.EnumAxis axis) {
		return AXISES_LOG[source.getAxis().ordinal()][axis.ordinal()];
	}

	private static BlockQuartz.EnumType getRotatedQuartzType(EnumFacing source, BlockQuartz.EnumType type) {
		return AXISES_QUARTZ[source.getAxis().ordinal()][type.ordinal()];
	}

	private static BlockLever.EnumOrientation getRotatedLeverFacing(EnumFacing source,
	                                                                BlockLever.EnumOrientation side) {
		EnumFacing facing;
		if (source.getAxis().isVertical() && side.getFacing().getAxis().isVertical()) {
			facing = side == BlockLever.EnumOrientation.UP_X || side == BlockLever.EnumOrientation.DOWN_X
			         ? EnumFacing.NORTH
			         : EnumFacing.WEST;
		} else {
			facing = side.getFacing();
		}

		EnumFacing facingRotated = getRotatedFacing(source, side.getFacing());
		return BlockLever.EnumOrientation.forFacings(facingRotated, facing);
	}

	public static class RotationException extends Exception {
		public RotationException(String message, Object... args) {
			super(String.format(message, args));
		}
	}
}
