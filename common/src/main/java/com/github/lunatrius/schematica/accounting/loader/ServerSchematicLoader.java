package com.github.lunatrius.schematica.accounting.loader;

import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.accounting.SchematicLocation;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ServerSchematicLoader {
	public static @NotNull CompletableFuture<ISchematic> get(@NotNull SchematicHolder holder) {
		if (holder.location() == SchematicLocation.LOCAL) {
			return CompletableFuture.completedFuture(SchematicFormat.readSchematic(holder.metadata(),
					Reference.proxy.getLevel()));
		} else {
			return CompletableFuture.completedFuture(null);
		}
	}
}
