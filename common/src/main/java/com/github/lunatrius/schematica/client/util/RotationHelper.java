package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class RotationHelper {
	public static final RotationHelper INSTANCE = new RotationHelper();
	private static final Direction[][] FACINGS = new Direction[Direction.values().length][];
	private static final Direction.Axis[][] AXISES = new Direction.Axis[Direction.Axis.values().length][];

	static {
		FACINGS[Direction.DOWN.ordinal()] = new Direction[] {Direction.DOWN,
		                                                     Direction.UP,
		                                                     Direction.WEST,
		                                                     Direction.EAST,
		                                                     Direction.SOUTH,
		                                                     Direction.NORTH};
		FACINGS[Direction.UP.ordinal()] = new Direction[] {Direction.DOWN,
		                                                   Direction.UP,
		                                                   Direction.EAST,
		                                                   Direction.WEST,
		                                                   Direction.NORTH,
		                                                   Direction.SOUTH};
		FACINGS[Direction.NORTH.ordinal()] = new Direction[] {Direction.EAST,
		                                                      Direction.WEST,
		                                                      Direction.NORTH,
		                                                      Direction.SOUTH,
		                                                      Direction.DOWN,
		                                                      Direction.UP};
		FACINGS[Direction.SOUTH.ordinal()] = new Direction[] {Direction.WEST,
		                                                      Direction.EAST,
		                                                      Direction.NORTH,
		                                                      Direction.SOUTH,
		                                                      Direction.UP,
		                                                      Direction.DOWN};
		FACINGS[Direction.WEST.ordinal()] = new Direction[] {Direction.NORTH,
		                                                     Direction.SOUTH,
		                                                     Direction.UP,
		                                                     Direction.DOWN,
		                                                     Direction.WEST,
		                                                     Direction.EAST};
		FACINGS[Direction.EAST.ordinal()] = new Direction[] {Direction.SOUTH,
		                                                     Direction.NORTH,
		                                                     Direction.DOWN,
		                                                     Direction.UP,
		                                                     Direction.WEST,
		                                                     Direction.EAST};

		AXISES[Direction.Axis.X.ordinal()] =
				new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z, Direction.Axis.Y};
		AXISES[Direction.Axis.Y.ordinal()] =
				new Direction.Axis[] {Direction.Axis.Z, Direction.Axis.Y, Direction.Axis.X};
		AXISES[Direction.Axis.Z.ordinal()] =
				new Direction.Axis[] {Direction.Axis.Y, Direction.Axis.X, Direction.Axis.Z};
	}

	public boolean rotate(SchematicWorld world, Direction axis, boolean forced) {
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

	private void updatePosition(SchematicWorld world, Direction axis) {
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

	public Schematic rotate(ISchematic schematic, Direction axis, boolean forced) throws RotationException {
		Vec3i dimensionsRotated =
				rotateDimensions(axis, schematic.getWidth(), schematic.getHeight(), schematic.getLength());
		Schematic schematicRotated =
				new Schematic(schematic.getIcon(), dimensionsRotated.getX(), dimensionsRotated.getY(),
				              dimensionsRotated.getZ(), schematic.getAuthor());
		MBlockPos tmp = new MBlockPos();

		for (MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, schematic.getWidth() - 1, schematic.getHeight() - 1,
		                                                schematic.getLength() - 1)) {
			BlockState blockState = schematic.getBlockState(pos);
			BlockState blockStateRotated = rotateBlock(blockState, axis, forced);
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

	private Vec3i rotateDimensions(Direction axis, int width, int height, int length) throws RotationException {
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

	private BlockPos rotatePos(BlockPos pos, Direction axis, Vec3i dimensions, MBlockPos rotated)
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
	private BlockState rotateBlock(BlockState blockState, Direction axisRotation, boolean forced)
			throws RotationException {
		IProperty propertyFacing = BlockStateHelper.getProperty(blockState, "facing");
		if (propertyFacing instanceof DirectionProperty) {
			Comparable value = blockState.get(propertyFacing);
			if (value instanceof Direction) {
				Direction facing = getRotatedFacing(axisRotation, (Direction) value);
				if (propertyFacing.getAllowedValues().contains(facing)) {
					return blockState.with(propertyFacing, facing);
				}
			}
		} else if (propertyFacing != null) {
			Reference.logger.error("'{}': found 'facing' property with unknown type {}",
			                       ForgeRegistries.BLOCKS.getKey(blockState.getBlock()),
			                       propertyFacing.getClass().getSimpleName());
		}

		IProperty propertyAxis = BlockStateHelper.getProperty(blockState, "axis");
		if (propertyAxis instanceof EnumProperty) {
			if (Direction.Axis.class.isAssignableFrom(propertyAxis.getValueClass())) {
				Direction.Axis axis = (Direction.Axis) blockState.get(propertyAxis);
				Direction.Axis axisRotated = getRotatedAxis(axisRotation, axis);
				return blockState.with(propertyAxis, axisRotated);
			}
		} else if (propertyAxis != null) {
			Reference.logger.error("'{}': found 'axis' property with unknown type {}",
			                       ForgeRegistries.BLOCKS.getKey(blockState.getBlock()),
			                       propertyAxis.getClass().getSimpleName());
		}

		if (!forced && (propertyFacing != null || propertyAxis != null)) {
			throw new RotationException("'%s' cannot be rotated around '%s'",
			                            ForgeRegistries.BLOCKS.getKey(blockState.getBlock()), axisRotation);
		}

		return blockState;
	}

	private static Direction getRotatedFacing(Direction source, Direction side) {
		return FACINGS[source.ordinal()][side.ordinal()];
	}

	private static Direction.Axis getRotatedAxis(Direction source, Direction.Axis axis) {
		return AXISES[source.getAxis().ordinal()][axis.ordinal()];
	}

	public static class RotationException extends Exception {
		public RotationException(String message, Object... args) {
			super(String.format(message, args));
		}
	}
}
