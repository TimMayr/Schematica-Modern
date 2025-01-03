package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.BlockState;

public interface IOffset {
	float getOffset(BlockState blockState);
}
