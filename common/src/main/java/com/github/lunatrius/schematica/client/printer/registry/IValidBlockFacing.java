package com.github.lunatrius.schematica.client.printer.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface IValidBlockFacing {
	List<Direction> getValidBlockFacings(List<Direction> solidSides, BlockState blockState);
}