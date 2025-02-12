package com.github.lunatrius.schematica.client.printer.registry;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public interface IExtraClick {
	int getExtraClicks(BlockState blockState);
}