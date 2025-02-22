package com.github.lunatrius.schematica.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class CommonCodecs {
	public static final StreamCodec<FriendlyByteBuf, UUID> UUID =
			StreamCodec.of(
					(buf, UUID) -> buf.writeUUID(UUID),
					(buf) -> buf.readUUID());

	@Contract(value = "_ -> new", pure = true)
	public static <T, B extends FriendlyByteBuf> @NotNull StreamCodec<B, List<T>> LIST(StreamCodec<B, T> codec) {
		return StreamCodec.of((buf, list) -> {
					buf.writeInt(list.size());
					for (T t : list) {
						codec.encode(buf, t);
					}
				},
				(buf) -> {
					int size = buf.readInt();
					List<T> result = new ArrayList<>(size);
					for (int i = 0; i < size; i++) {
						result.add(codec.decode(buf));
					}
					return result;
				});
	}

	@Contract(value = "_, _-> new", pure = true)
	public static <K, V, B extends FriendlyByteBuf> @NotNull StreamCodec<B, Map<K, V>> MAP(StreamCodec<B, K> keyCodec,
	                                                                                       StreamCodec<B, V> valueCodec) {
		return StreamCodec.of((buf, map) -> {
					buf.writeInt(map.size());
					for (Map.Entry<K, V> entry : map.entrySet()) {
						keyCodec.encode(buf, entry.getKey());
						valueCodec.encode(buf, entry.getValue());
					}
				},
				(buf) -> {
					int size = buf.readInt();
					Map<K, V> result = new HashMap<>(size);
					for (int i = 0; i < size; i++) {
						result.put(keyCodec.decode(buf), valueCodec.decode(buf));
					}

					return result;
				});
	}

	@Contract(value = "_ -> new", pure = true)
	public static <T, B extends FriendlyByteBuf> @NotNull StreamCodec<B, Set<T>> SET(StreamCodec<B, T> codec) {
		return StreamCodec.of((buf, set) -> {
					buf.writeInt(set.size());
					for (T t : set) {
						codec.encode(buf, t);
					}
				},
				(buf) -> {
					int size = buf.readInt();
					Set<T> result = new HashSet<>();
					for (int i = 0; i < size; i++) {
						result.add(codec.decode(buf));
					}

					return result;
				});
	}

	@Contract(value = "_ -> new", pure = true)
	public static <T extends Enum<T>> @NotNull StreamCodec<FriendlyByteBuf, T> ENUM(Class<T> enumClass) {
		return StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(enumClass)
		);
	}
}
