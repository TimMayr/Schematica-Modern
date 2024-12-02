package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlacementData {
	private final IValidPlayerFacing validPlayerFacing;
	private final IValidBlockFacing validBlockFacing;
	private IOffset offsetX;
	private IOffset offsetY;
	private IOffset offsetZ;
	private IExtraClick extraClick;

	public PlacementData() {
		this(null, null);
	}

	public PlacementData(final IValidPlayerFacing validPlayerFacing, final IValidBlockFacing validBlockFacing) {
		this.validPlayerFacing = validPlayerFacing;
		this.validBlockFacing = validBlockFacing;
		this.offsetX = null;
		this.offsetY = null;
		this.offsetZ = null;
	}

	public PlacementData(final IValidPlayerFacing validPlayerFacing) {
		this(validPlayerFacing, null);
	}

	public PlacementData(final IValidBlockFacing validBlockFacing) {
		this(null, validBlockFacing);
	}

	public PlacementData setOffsetX(final IOffset offset) {
		this.offsetX = offset;
		return this;
	}

	public PlacementData setOffsetY(final IOffset offset) {
		this.offsetY = offset;
		return this;
	}

	public PlacementData setOffsetZ(final IOffset offset) {
		this.offsetZ = offset;
		return this;
	}

	public PlacementData setExtraClick(final IExtraClick extraClick) {
		this.extraClick = extraClick;
		return this;
	}

	public float getOffsetX(final BlockState blockState) {
		if (this.offsetX != null) {
			return this.offsetX.getOffset(blockState);
		}

		return 0.5f;
	}

	public float getOffsetY(final BlockState blockState) {
		if (this.offsetY != null) {
			return this.offsetY.getOffset(blockState);
		}

		return 0.5f;
	}

	public float getOffsetZ(final BlockState blockState) {
		if (this.offsetZ != null) {
			return this.offsetZ.getOffset(blockState);
		}

		return 0.5f;
	}

	public int getExtraClicks(final BlockState blockState) {
		if (this.extraClick != null) {
			return this.extraClick.getExtraClicks(blockState);
		}

		return 0;
	}

	public boolean isValidPlayerFacing(final BlockState blockState,
	                                   final PlayerEntity player,
	                                   final BlockPos pos,
	                                   final World world) {
		return this.validPlayerFacing == null || this.validPlayerFacing.isValid(blockState, player, pos, world);
	}

	public List<Direction> getValidBlockFacings(final List<Direction> solidSides, final BlockState blockState) {
		final List<Direction> list = this.validBlockFacing != null
		                             ? this.validBlockFacing.getValidBlockFacings(solidSides, blockState)
		                             : new ArrayList<>(solidSides);

		for (final Iterator<Direction> iterator = list.iterator(); iterator.hasNext(); ) {
			final Direction facing = iterator.next();
			if (this.offsetY != null) {
				final float offset = this.offsetY.getOffset(blockState);
				if (offset < 0.5 && facing == Direction.UP) {
					iterator.remove();
				} else if (offset > 0.5 && facing == Direction.DOWN) {
					iterator.remove();
				}
			}
		}

		return list;
	}
}
