package com.github.lunatrius.schematica.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record SchematicDimensions(int width, int height, int length) {
	public static final StreamCodec<RegistryFriendlyByteBuf, SchematicDimensions> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.INT, SchematicDimensions::width,
					ByteBufCodecs.INT, SchematicDimensions::height,
					ByteBufCodecs.INT, SchematicDimensions::length,
					SchematicDimensions::new
			);

	@Contract("_ -> new")
	public @NotNull SchematicDimensions withWidth(int newWidth) {
		return new SchematicDimensions(newWidth, this.height, this.length);
	}

	@Contract("_ -> new")
	public @NotNull SchematicDimensions withHeight(int newHeight) {
		return new SchematicDimensions(this.width, newHeight, this.length);
	}

	@Contract("_ -> new")
	public @NotNull SchematicDimensions withLength(int newLength) {
		return new SchematicDimensions(this.width, this.height, newLength);
	}
}
