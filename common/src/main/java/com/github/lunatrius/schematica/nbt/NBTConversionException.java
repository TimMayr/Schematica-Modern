package com.github.lunatrius.schematica.nbt;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NBTConversionException extends Exception {
	public NBTConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public NBTConversionException(BlockEntity blockEntity, Throwable cause) {
		super(String.valueOf(blockEntity), cause);
	}

	public NBTConversionException(Entity entity, Throwable cause) {
		super(String.valueOf(entity), cause);
	}
}