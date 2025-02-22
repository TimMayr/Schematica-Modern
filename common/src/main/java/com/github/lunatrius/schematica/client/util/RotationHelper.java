package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RotationHelper {
	public static final RotationHelper INSTANCE = new RotationHelper();
	private static final Direction[][] FACINGS = new Direction[Direction.values().length][];
	private static final Direction.Axis[][] AXISES = new Direction.Axis[Direction.Axis.values().length][];

	static {
		FACINGS[Direction.DOWN.ordinal()] = new Direction[]{Direction.DOWN,
				Direction.UP,
				Direction.WEST,
				Direction.EAST,
				Direction.SOUTH,
				Direction.NORTH};
		FACINGS[Direction.UP.ordinal()] = new Direction[]{Direction.DOWN,
				Direction.UP,
				Direction.EAST,
				Direction.WEST,
				Direction.NORTH,
				Direction.SOUTH};
		FACINGS[Direction.NORTH.ordinal()] = new Direction[]{Direction.EAST,
				Direction.WEST,
				Direction.NORTH,
				Direction.SOUTH,
				Direction.DOWN,
				Direction.UP};
		FACINGS[Direction.SOUTH.ordinal()] = new Direction[]{Direction.WEST,
				Direction.EAST,
				Direction.NORTH,
				Direction.SOUTH,
				Direction.UP,
				Direction.DOWN};
		FACINGS[Direction.WEST.ordinal()] = new Direction[]{Direction.NORTH,
				Direction.SOUTH,
				Direction.UP,
				Direction.DOWN,
				Direction.WEST,
				Direction.EAST};
		FACINGS[Direction.EAST.ordinal()] = new Direction[]{Direction.SOUTH,
				Direction.NORTH,
				Direction.DOWN,
				Direction.UP,
				Direction.WEST,
				Direction.EAST};

		AXISES[Direction.Axis.X.ordinal()] =
				new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z, Direction.Axis.Y};
		AXISES[Direction.Axis.Y.ordinal()] =
				new Direction.Axis[]{Direction.Axis.Z, Direction.Axis.Y, Direction.Axis.X};
		AXISES[Direction.Axis.Z.ordinal()] =
				new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.X, Direction.Axis.Z};
	}

	public boolean rotate(FakeLevel world, Direction axis, boolean forced) {
		if (world == null) {
			return false;
		}

		try {
			ISchematic schematic = world.getLevelSource();
			Schematic schematicRotated = rotate(schematic, axis, forced);

			updatePosition(world, axis);

			world.setLevelSource(schematicRotated);

			return true;
		} catch (RotationException re) {
			Reference.logger.error(re.getMessage());
		} catch (Exception e) {
			Reference.logger.fatal("Something went wrong!", e);
		}

		return false;
	}

	public Schematic rotate(@NotNull ISchematic schematic, Direction axis, boolean forced) throws RotationException {
		Vec3i dimensionsRotated =
				rotateDimensions(axis, schematic.getWidth(), schematic.getHeight(), schematic.getLength());
		SchematicMetadata metadata = schematic.getMetadata()
				.withDimensions(schematic.getMetadata().dimensions()
						.withWidth(dimensionsRotated.getX())
						.withHeight(dimensionsRotated.getY())
						.withLength(dimensionsRotated.getZ()));
		Schematic schematicRotated = new Schematic(metadata);
		MBlockPos tmp = new MBlockPos();

		for (MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, schematic.getWidth() - 1, schematic.getHeight() - 1,
				schematic.getLength() - 1)) {
			BlockState blockState = schematic.getBlockState(pos);
			BlockState blockStateRotated = rotateBlock(blockState, axis, forced);
			schematicRotated.setBlockState(rotatePos(pos, axis, dimensionsRotated, tmp), blockStateRotated);
		}

		List<BlockEntity> blockEntities = schematic.getBlockEntities();
		for (BlockEntity blockEntity : blockEntities) {
			BlockPos pos = blockEntity.getBlockPos();
			schematicRotated.setBlockEntity(new BlockPos(rotatePos(pos, axis, dimensionsRotated, tmp)), blockEntity);
		}

		return schematicRotated;
	}

	private void updatePosition(FakeLevel world, @NotNull Direction axis) {
		switch (axis) {
			case DOWN:
			case UP: {
				int offset = (world.getLevelSource().getMaxX() - world.getLevelSource().getMaxZ()) / 2;
				world.getWorldPos().offset(offset, 0, offset);
				break;
			}

			case NORTH:
			case SOUTH: {
				int offset = (world.getLevelSource().getMaxX() - world.getHeight()) / 2;
				world.getWorldPos().offset(offset, offset, 0);
				break;
			}

			case WEST:
			case EAST: {
				int offset = (world.getHeight() - world.getLevelSource().getMaxZ()) / 2;
				world.getWorldPos().offset(0, offset, offset);
				break;
			}
		}
	}

	@SuppressWarnings("SuspiciousNameCombination")
	private @NotNull Vec3i rotateDimensions(@NotNull Direction axis, int width, int height, int length) {
		return switch (axis) {
			case DOWN, UP -> new Vec3i(length, height, width);
			case NORTH, SOUTH -> new Vec3i(height, width, length);
			case WEST, EAST -> new Vec3i(width, length, height);
		};

	}

	@SuppressWarnings({"rawtypes"})
	private @NotNull BlockState rotateBlock(BlockState blockState, Direction axisRotation, boolean forced)
			throws RotationException {
		Property<?> propertyFacingPotentially = BlockStateHelper.getProperty(blockState, "facing");
		if (propertyFacingPotentially.getPossibleValues().stream().allMatch(Direction.class::isInstance)) {
			EnumProperty<Direction> propertyFacing = BlockStateProperties.FACING;
			Comparable value = blockState.getValue(propertyFacing);
			if (value instanceof Direction) {
				Direction facing = getRotatedFacing(axisRotation, (Direction) value);
				if (propertyFacing.getPossibleValues().contains(facing)) {
					return blockState.setValue(propertyFacing, facing);
				}
			}
		} else {
			Reference.logger.error("'{}': found 'facing' property with unknown type {}",
					BuiltInRegistries.BLOCK.getKey(blockState.getBlock()),
					propertyFacingPotentially.getClass().getSimpleName());
		}

		Property<?> property = BlockStateHelper.getProperty(blockState, "axis");
		if (property.getPossibleValues().stream().allMatch(Direction.Axis.class::isInstance)) {
			EnumProperty<Direction.Axis> propertyAxis = BlockStateProperties.AXIS;
			if (Direction.Axis.class.isAssignableFrom(propertyAxis.getValueClass())) {
				Direction.Axis axis = blockState.getValue(propertyAxis);
				Direction.Axis axisRotated = getRotatedAxis(axisRotation, axis);
				return blockState.setValue(propertyAxis, axisRotated);
			}
		} else {
			Reference.logger.error("'{}': found 'axis' property with unknown type {}",
					BuiltInRegistries.BLOCK.getKey(blockState.getBlock()),
					property.getClass().getSimpleName());
		}

		if (!forced) {
			throw new RotationException("'%s' cannot be rotated around '%s'",
					BuiltInRegistries.BLOCK.getKey(blockState.getBlock()), axisRotation);
		}

		return blockState;
	}

	private @NotNull BlockPos rotatePos(BlockPos pos, @NotNull Direction axis, Vec3i dimensions, MBlockPos rotated) {
		return switch (axis) {
			case DOWN -> rotated.set(pos.getZ(), pos.getY(), dimensions.getZ() - 1 - pos.getX());
			case UP -> rotated.set(dimensions.getX() - 1 - pos.getZ(), pos.getY(), pos.getX());
			case NORTH -> rotated.set(dimensions.getX() - 1 - pos.getY(), pos.getX(), pos.getZ());
			case SOUTH -> rotated.set(pos.getY(), dimensions.getY() - 1 - pos.getX(), pos.getZ());
			case WEST -> rotated.set(pos.getX(), dimensions.getY() - 1 - pos.getZ(), pos.getY());
			case EAST -> rotated.set(pos.getX(), pos.getZ(), dimensions.getZ() - 1 - pos.getY());
		};

	}

	private static Direction getRotatedFacing(@NotNull Direction source, @NotNull Direction side) {
		return FACINGS[source.ordinal()][side.ordinal()];
	}

	private static Direction.Axis getRotatedAxis(@NotNull Direction source, Direction.@NotNull Axis axis) {
		return AXISES[source.getAxis().ordinal()][axis.ordinal()];
	}

	public static class RotationException extends Exception {
		public RotationException(String message, Object... args) {
			super(String.format(message, args));
		}
	}
}