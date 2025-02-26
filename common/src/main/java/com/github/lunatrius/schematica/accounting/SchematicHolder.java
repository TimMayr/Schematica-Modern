package com.github.lunatrius.schematica.accounting;

import com.github.lunatrius.schematica.accounting.loader.ClientSchematicLoader;
import com.github.lunatrius.schematica.accounting.loader.ServerSchematicLoader;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.core.PlatformUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public record SchematicHolder(SchematicMetadata metadata, SchematicLocation location) {
	public static final StreamCodec<RegistryFriendlyByteBuf, SchematicHolder> STREAM_CODEC =
			StreamCodec.composite(
					SchematicMetadata.STREAM_CODEC, SchematicHolder::metadata,
					CommonCodecs.ENUM(SchematicLocation.class), SchematicHolder::location,
					SchematicHolder::new);

	public static @NotNull Map<String, SchematicHolder> qualify(@NotNull Collection<SchematicHolder> holders) {
		Map<String, Integer> nameCount = new HashMap<>();
		Map<String, SchematicHolder> result = new TreeMap<>();

		for (SchematicHolder holder : holders) {
			String name = holder.metadata.name();
			nameCount.put(name, nameCount.getOrDefault(name, 0) + 1);
		}

		for (SchematicHolder holder : holders) {
			String name = holder.metadata.name();
			if (nameCount.get(name) > 1) {
				result.put(holder.toString(), holder);
			} else {
				result.put(name, holder);
			}
		}

		return result;
	}

	@Override
	public @NotNull String toString() {
		return String.format("%s (by %s)", this.metadata.name(), this.metadata.owner());
	}

	public boolean checkPermission(UUID player, FilePermission permission) {
		if (this.metadata().owner().equals(player)) {
			return true;
		}

		if (permission == FilePermission.READ) {
			if (!this.metadata().isPrivate()) {
				return true;
			}
		}

		return this.metadata.permissions().getOrDefault(player, List.of()).contains(permission);
	}

	public @NotNull CompletableFuture<ISchematic> getSchematic() {
		if (PlatformUtils.isPlatformClient()) {
			return ClientSchematicLoader.get(this);
		} else {
			return ServerSchematicLoader.get(this);
		}
	}
}