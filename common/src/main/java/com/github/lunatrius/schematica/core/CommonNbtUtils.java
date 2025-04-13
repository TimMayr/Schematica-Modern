package com.github.lunatrius.schematica.core;

import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class CommonNbtUtils {
	public static <K, V> @NotNull CompoundTag serializeMap(Map<K, V> map, StreamCodec<FriendlyByteBuf, K> keyCodec,
	                                                       StreamCodec<FriendlyByteBuf, V> valueCodec) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		CommonCodecs.MAP(keyCodec, valueCodec).encode(buf, map);

		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);

		CompoundTag compound = new CompoundTag();
		compound.putByteArray("data", data);
		return compound;
	}

	public static <K, V> @NotNull Map<K, V> deserializeMap(@NotNull CompoundTag tag,
	                                                       StreamCodec<FriendlyByteBuf, K> keyCodec,
	                                                       StreamCodec<FriendlyByteBuf, V> valueCodec) {
		if (!tag.contains("data")) {
			return new HashMap<>(); // Return empty map if missing
		}

		byte[] data = tag.getByteArray("data").orElse(new byte[]{}); // Retrieve stored byte array
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

		return CommonCodecs.MAP(keyCodec, valueCodec).decode(buf);
	}

	public static ItemStack deserializeItemStack(CompoundTag tagCompound, String location) {
		ItemStack icon = Constants.Schematic.DEFAULT_ICON.copy();

		if (tagCompound != null && tagCompound.contains(location)) {
			icon = ItemStack.parse(Reference.proxy.getRegistryAccess(),
					tagCompound.getCompound(location).orElse(new CompoundTag())).orElse(ItemStack.EMPTY);

			if (icon.isEmpty()) {
				icon = Constants.Schematic.DEFAULT_ICON.copy();
			}
		}

		return icon;
	}

	public static @NotNull CompoundTag serializeInstant(Instant instant) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeInstant(instant);

		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);

		CompoundTag compound = new CompoundTag();
		compound.putByteArray("timestamp", data);
		return compound;
	}

	public static @NotNull Instant deserializeInstant(@NotNull CompoundTag tag) {
		if (!tag.contains("timestamp")) {
			return Instant.ofEpochMilli(Long.MIN_VALUE);
		}

		byte[] data = tag.getByteArray("timestamp").orElse(new byte[]{}); // Retrieve stored byte array
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

		return buf.readInstant();
	}
}
