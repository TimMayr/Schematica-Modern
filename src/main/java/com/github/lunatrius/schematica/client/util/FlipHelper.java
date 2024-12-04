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
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class FlipHelper {
	public static final FlipHelper INSTANCE = new FlipHelper();

	public boolean flip(SchematicWorld world, Direction axis, boolean forced) {
		if (world == null) {
			return false;
		}

		try {
			ISchematic schematic = world.getSchematic();
			Schematic schematicFlipped = flip(schematic, axis, forced);

			world.setSchematic(schematicFlipped);

			for (TileEntity tileEntity : world.getTileEntities()) {
				world.initializeTileEntity(tileEntity);
			}

			return true;
		} catch (FlipException fe) {
			Reference.logger.error(fe.getMessage());
		} catch (Exception e) {
			Reference.logger.fatal("Something went wrong!", e);
		}

		return false;
	}

	public Schematic flip(ISchematic schematic, Direction axis, boolean forced) throws FlipException {
		Vec3i dimensionsFlipped = new Vec3i(schematic.getWidth(), schematic.getHeight(), schematic.getLength());
		Schematic schematicFlipped =
				new Schematic(schematic.getIcon(), dimensionsFlipped.getX(), dimensionsFlipped.getY(),
				              dimensionsFlipped.getZ(), schematic.getAuthor());
		MBlockPos tmp = new MBlockPos();

		for (MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, schematic.getWidth() - 1, schematic.getHeight() - 1,
		                                                schematic.getLength() - 1)) {
			BlockState blockState = schematic.getBlockState(pos);
			BlockState blockStateFlipped = flipBlock(blockState, axis, forced);
			schematicFlipped.setBlockState(flipPos(pos, axis, dimensionsFlipped, tmp), blockStateFlipped);
		}

		List<TileEntity> tileEntities = schematic.getTileEntities();
		for (TileEntity tileEntity : tileEntities) {
			BlockPos pos = tileEntity.getPos();
			tileEntity.setPos(new BlockPos(flipPos(pos, axis, dimensionsFlipped, tmp)));
			schematicFlipped.setTileEntity(tileEntity.getPos(), tileEntity);
		}

		return schematicFlipped;
	}

	private BlockPos flipPos(BlockPos pos, Direction axis, Vec3i dimensions, MBlockPos flipped) throws FlipException {
		switch (axis) {
			case DOWN:
			case UP:
				return flipped.set(pos.getX(), dimensions.getY() - 1 - pos.getY(), pos.getZ());

			case NORTH:
			case SOUTH:
				return flipped.set(pos.getX(), pos.getY(), dimensions.getZ() - 1 - pos.getZ());

			case WEST:
			case EAST:
				return flipped.set(dimensions.getX() - 1 - pos.getX(), pos.getY(), pos.getZ());
		}

		throw new FlipException("'%s' is not a valid axis!", axis.getName());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private BlockState flipBlock(BlockState blockState, Direction axis, boolean forced) throws FlipException {
		IProperty propertyFacing = BlockStateHelper.getProperty(blockState, "facing");
		if (propertyFacing instanceof DirectionProperty) {
			Comparable value = blockState.get(propertyFacing);
			if (value instanceof Direction) {
				Direction facing = getFlippedFacing(axis, (Direction) value);
				if (propertyFacing.getAllowedValues().contains(facing)) {
					return blockState.with(propertyFacing, facing);
				}
			}
		} else if (propertyFacing != null) {
			Reference.logger.error("'{}': found 'facing' property with unknown type {}",
			                       ForgeRegistries.BLOCKS.getKey(blockState.getBlock()),
			                       propertyFacing.getClass().getSimpleName());
		}

		if (!forced && propertyFacing != null) {
			throw new FlipException("'%s' cannot be flipped across '%s'",
			                        ForgeRegistries.BLOCKS.getKey(blockState.getBlock()), axis);
		}

		return blockState;
	}

	private static Direction getFlippedFacing(Direction axis, Direction side) {
		if (axis.getAxis() == side.getAxis()) {
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
