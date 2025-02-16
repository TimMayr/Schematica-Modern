package com.github.lunatrius.schematica.accounting;

import java.util.UUID;
import java.util.function.BiFunction;

public enum FilePermission {
	READ((uuid, holder) ->
			holder.getLocationType() == SchematicLocation.PUBLIC ||
					holder.getOwner().equals(uuid) ||
					holder.getAdditionalReadPlayers().contains(uuid)),
	DELETE((uuid, holder) ->
			holder.getOwner().equals(uuid) ||
					holder.getAdditionalRemovePlayers().contains(uuid));

	private final BiFunction<UUID, SchematicHolder, Boolean> checkFunc;

	FilePermission(BiFunction<UUID, SchematicHolder, Boolean> checkFunc) {
		this.checkFunc = checkFunc;
	}

	public boolean check(UUID player, SchematicHolder holder) {
		return checkFunc.apply(player, holder);
	}
}
