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

	public PlacementData(IValidPlayerFacing validPlayerFacing, IValidBlockFacing validBlockFacing) {
		this.validPlayerFacing = validPlayerFacing;
		this.validBlockFacing = validBlockFacing;
		this.offsetX = null;
		this.offsetY = null;
		this.offsetZ = null;
	}

	public PlacementData(IValidPlayerFacing validPlayerFacing) {
		this(validPlayerFacing, null);
	}

	public PlacementData(IValidBlockFacing validBlockFacing) {
		this(null, validBlockFacing);
	}

	public PlacementData setOffsetX(IOffset offset) {
		this.offsetX = offset;
		return this;
	}

	public PlacementData setOffsetY(IOffset offset) {
		this.offsetY = offset;
		return this;
	}

	public PlacementData setOffsetZ(IOffset offset) {
		this.offsetZ = offset;
		return this;
	}

	public PlacementData setExtraClick(IExtraClick extraClick) {
		this.extraClick = extraClick;
		return this;
	}

	public float getOffsetX(BlockState blockState) {
		if (this.offsetX != null) {
			return this.offsetX.getOffset(blockState);
		}

		return 0.5f;
	}

	public float getOffsetY(BlockState blockState) {
		if (this.offsetY != null) {
			return this.offsetY.getOffset(blockState);
		}

		return 0.5f;
	}

	public float getOffsetZ(BlockState blockState) {
		if (this.offsetZ != null) {
			return this.offsetZ.getOffset(blockState);
		}

		return 0.5f;
	}

	public int getExtraClicks(BlockState blockState) {
		if (this.extraClick != null) {
			return this.extraClick.getExtraClicks(blockState);
		}

		return 0;
	}

	public boolean isValidPlayerFacing(BlockState blockState, PlayerEntity player, BlockPos pos, World world) {
		return this.validPlayerFacing == null || this.validPlayerFacing.isValid(blockState, player, pos, world);
	}

	public List<Direction> getValidBlockFacings(List<Direction> solidSides, BlockState blockState) {
		List<Direction> list = this.validBlockFacing != null
		                       ? this.validBlockFacing.getValidBlockFacings(solidSides, blockState)
		                       : new ArrayList<>(solidSides);

		for (Iterator<Direction> iterator = list.iterator(); iterator.hasNext(); ) {
			Direction facing = iterator.next();
			if (this.offsetY != null) {
				float offset = this.offsetY.getOffset(blockState);
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
