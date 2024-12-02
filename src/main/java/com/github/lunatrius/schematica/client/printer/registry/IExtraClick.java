package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.BlockState;

public interface IExtraClick {
	int getExtraClicks(BlockState blockState);
}
