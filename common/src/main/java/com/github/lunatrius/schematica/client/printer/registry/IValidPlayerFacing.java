package com.github.lunatrius.schematica.client.printer.registry;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IValidPlayerFacing {
	boolean isValid(BlockState blockState, Player player, BlockPos pos, Level world);
}