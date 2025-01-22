package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface IValidBlockFacing {
	List<Direction> getValidBlockFacings(List<Direction> solidSides, BlockState blockState);
}