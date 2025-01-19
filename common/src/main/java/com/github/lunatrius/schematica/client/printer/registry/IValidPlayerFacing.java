package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.Player;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Level;

public interface IValidPlayerFacing {
	boolean isValid(BlockState blockState, Player player, BlockPos pos, Level world);
}