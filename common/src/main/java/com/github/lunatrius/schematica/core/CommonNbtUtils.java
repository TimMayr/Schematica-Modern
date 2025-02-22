package com.github.lunatrius.schematica.core;

import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
		if (!tag.contains("data", Tag.TAG_BYTE_ARRAY)) {
			return new HashMap<>(); // Return empty map if missing
		}

		byte[] data = tag.getByteArray("data"); // Retrieve stored byte array
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

		return CommonCodecs.MAP(keyCodec, valueCodec).decode(buf);
	}

	public static ItemStack deserializeItemStack(CompoundTag tagCompound, String location) {
		ItemStack icon = Constants.Schematic.DEFAULT_ICON.copy();

		if (tagCompound != null && tagCompound.contains(location)) {
			icon = ItemStack.parseOptional(Reference.proxy.getRegistryAccess(),
					tagCompound.getCompound(location));

			if (icon.isEmpty()) {
				icon = Constants.Schematic.DEFAULT_ICON.copy();
			}
		}

		return icon;
	}
}
