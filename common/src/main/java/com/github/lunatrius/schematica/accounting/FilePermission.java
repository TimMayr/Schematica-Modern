package com.github.lunatrius.schematica.accounting;

import java.util.UUID;
import java.util.function.BiFunction;

public enum FilePermission {
	READ((uuid, holder) ->
			holder.locationType() == SchematicLocation.PUBLIC ||
					holder.owner().equals(uuid) ||
					holder.additionalReadPlayers().contains(uuid)),
	DELETE((uuid, holder) ->
			holder.owner().equals(uuid) ||
					holder.additionalRemovePlayers().contains(uuid));

	private final BiFunction<UUID, SchematicHolder, Boolean> checkFunc;

	FilePermission(BiFunction<UUID, SchematicHolder, Boolean> checkFunc) {
		this.checkFunc = checkFunc;
	}

	public boolean check(UUID player, SchematicHolder holder) {
		return checkFunc.apply(player, holder);
	}
}
