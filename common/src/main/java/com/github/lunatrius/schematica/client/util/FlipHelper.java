package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
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
public class FlipHelper {
	public static final FlipHelper INSTANCE = new FlipHelper();

	public boolean flip(FakeLevel world, Direction.Axis axis, boolean forced) {
		if (world == null) {
			return false;
		}

		try {
			ISchematic schematic = world.getLevelSource();
			Schematic schematicFlipped = flip(schematic, axis, forced);

			world.setLevelSource(schematicFlipped);
			return true;
		} catch (FlipException fe) {
			Reference.logger.error(fe.getMessage());
		} catch (Exception e) {
			Reference.logger.fatal("Something went wrong!", e);
		}

		return false;
	}

	public Schematic flip(@NotNull ISchematic schematic, Direction.Axis axis, boolean forced) throws FlipException {
		Vec3i dimensionsFlipped = new Vec3i(schematic.getSizeX(), schematic.getHeight(), schematic.getSizeZ());
		Schematic schematicFlipped =
				new Schematic(schematic.getIcon(), schematic.getName(), dimensionsFlipped.getX(),
						dimensionsFlipped.getY(), dimensionsFlipped.getZ(), schematic.getAuthor());
		MBlockPos tmp = new MBlockPos();

		for (MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, schematic.getSizeX() - 1, schematic.getHeight() - 1,
				schematic.getSizeZ() - 1)) {
			BlockState blockState = schematic.getBlockState(pos);
			BlockState blockStateFlipped = flipBlock(blockState, axis, forced);
			schematicFlipped.setBlockState(flipPos(pos, axis, dimensionsFlipped, tmp), blockStateFlipped);
		}

		List<BlockEntity> blockEntities = schematic.getBlockEntities();
		for (BlockEntity blockEntity : blockEntities) {
			BlockPos pos = blockEntity.getBlockPos();
			schematicFlipped.setBlockEntity(new BlockPos(flipPos(pos, axis, dimensionsFlipped, tmp)), blockEntity);
		}

		return schematicFlipped;
	}

	@SuppressWarnings({"rawtypes"})
	private @NotNull BlockState flipBlock(BlockState blockState, Direction.Axis axis, boolean forced) throws FlipException {
		Property<?> property = BlockStateHelper.getProperty(blockState, "facing");
		if (property.getPossibleValues().stream().allMatch(Direction.class::isInstance)) {
			EnumProperty<Direction> propertyFacing = BlockStateProperties.FACING;
			Comparable value = blockState.getValue(propertyFacing);
			if (value instanceof Direction) {
				Direction facing = getFlippedFacing(axis, (Direction) value);
				if (propertyFacing.getPossibleValues().contains(facing)) {
					return blockState.setValue(propertyFacing, facing);
				}
			}
		} else {
			Reference.logger.error("'{}': found 'facing' property with unknown type {}",
					BuiltInRegistries.BLOCK.getKey(blockState.getBlock()),
					property.getClass().getSimpleName());
		}

		if (!forced) {
			throw new FlipException("'%s' cannot be flipped across '%s'",
					BuiltInRegistries.BLOCK.getKey(blockState.getBlock()), axis);
		}

		return blockState;
	}

	private @NotNull BlockPos flipPos(BlockPos pos, @NotNull Direction.Axis axis, Vec3i dimensions,
	                                  MBlockPos flipped) {
		return switch (axis) {
			case Z -> flipped.set(pos.getX(), dimensions.getY() - 1 - pos.getY(), pos.getZ());
			case X -> flipped.set(pos.getX(), pos.getY(), dimensions.getZ() - 1 - pos.getZ());
			case Y -> flipped.set(dimensions.getX() - 1 - pos.getX(), pos.getY(), pos.getZ());
		};

	}

	private static @NotNull Direction getFlippedFacing(Direction.Axis axis, @NotNull Direction side) {
		if (axis == side.getAxis()) {
			return side.getOpposite();
		}

		return side;
	}

	public static class FlipException extends Exception {
		public FlipException(String message, Object... args) {
			super(String.format(message, args));
		}
	}
}