package com.github.lunatrius.schematica.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Codecs {
	public static final StreamCodec<ByteBuf, UUID> UUID = StreamCodec.of((buf, UUID) -> {
		buf.writeLong(UUID.getMostSignificantBits());
		buf.writeLong(UUID.getLeastSignificantBits());
	}, buf -> new UUID(buf.readLong(), buf.readLong()));

	@Contract(value = "_ -> new", pure = true)
	public static <T> @NotNull StreamCodec<ByteBuf, List<T>> LIST(
			StreamCodec<ByteBuf, T> codec) {
		return StreamCodec.of((buf, list) -> {
					for (T t : list) {
						codec.encode(buf, t);
					}
				},
				(buf) -> {
					List<T> result = new ArrayList<>();
					while (buf.isReadable()) {
						result.add(codec.decode(buf));
					}
					return result;
				});
	}

	@Contract(value = "_, _-> new", pure = true)
	public static <T, V> @NotNull StreamCodec<ByteBuf, Map<T, V>> MAP(
			StreamCodec<ByteBuf, T> keyCodec, StreamCodec<ByteBuf, V> valueCodec) {
		return StreamCodec.of((buf, map) -> {
					for (Map.Entry<T, V> entry : map.entrySet()) {
						keyCodec.encode(buf, entry.getKey());
						valueCodec.encode(buf, entry.getValue());
					}
				},
				(buf) -> {
					Map<T, V> result = new HashMap<>();
					while (buf.isReadable()) {
						result.put(keyCodec.decode(buf), valueCodec.decode(buf));
					}
					return result;
				});
	}

	@Contract(value = "_ -> new", pure = true)
	public static <T> @NotNull StreamCodec<ByteBuf, Set<T>> SET(
			StreamCodec<ByteBuf, T> codec) {
		return StreamCodec.of((buf, set) -> {
					for (T t : set) {
						codec.encode(buf, t);
					}
				},
				(buf) -> {
					Set<T> result = new HashSet<>();
					while (buf.isReadable()) {
						result.add(codec.decode(buf));
					}
					return result;
				});
	}

	@Contract(value = "_ -> new", pure = true)
	public static <T extends Enum<T>> @NotNull StreamCodec<RegistryFriendlyByteBuf, T> ENUM(Class<T> enumClass) {
		return StreamCodec.of(
				RegistryFriendlyByteBuf::writeEnum,
				buf -> buf.readEnum(enumClass)
		);
	}
}
