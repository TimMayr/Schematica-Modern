package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IValidPlayerFacing {
	boolean isValid(BlockState blockState, PlayerEntity player, BlockPos pos, World world);
}
