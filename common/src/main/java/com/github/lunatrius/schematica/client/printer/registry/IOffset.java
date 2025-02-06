package com.github.lunatrius.schematica.client.printer.registry;


import net.minecraft.world.level.block.state.BlockState;

public interface IOffset {
	float getOffset(BlockState blockState);
}