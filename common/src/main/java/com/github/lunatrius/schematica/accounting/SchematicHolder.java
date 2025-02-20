package com.github.lunatrius.schematica.accounting;

import com.github.lunatrius.schematica.accounting.loaders.ClientSchematicLoader;
import com.github.lunatrius.schematica.api.ISchematic;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public record SchematicHolder(String name, long filesize, SchematicLocation locationType, UUID owner,
                              Set<UUID> additionalReadPlayers, Set<UUID> additionalRemovePlayers) {
	static @NotNull Map<String, SchematicHolder> qualify(@NotNull Collection<SchematicHolder> holders) {
		Map<String, Integer> nameCount = new HashMap<>();
		Map<String, SchematicHolder> result = new TreeMap<>();

		for (SchematicHolder holder : holders) {
			String name = holder.name();
			nameCount.put(name, nameCount.getOrDefault(name, 0) + 1);
		}

		for (SchematicHolder holder : holders) {
			String name = holder.name;
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
		return String.format("%s (by %s) (%s)", this.name, this.owner,
				this.locationType.toString().toLowerCase(Locale.ROOT));
	}

	public @NotNull CompletableFuture<ISchematic> getSchematic() {
		if (Platform.getEnv() == EnvType.CLIENT) {
			return ClientSchematicLoader.get(this);
		} else {
			return null;
		}
	}
}
