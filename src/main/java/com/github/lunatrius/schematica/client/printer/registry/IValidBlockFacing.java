package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

import java.util.List;

public interface IValidBlockFacing {
	List<Direction> getValidBlockFacings(List<Direction> solidSides, BlockState blockState);
}
