package com.github.lunatrius.schematica.client.printer.registry;


import net.minecraft.world.level.block.state.BlockState;

public interface IExtraClick {
	int getExtraClicks(BlockState blockState);
}