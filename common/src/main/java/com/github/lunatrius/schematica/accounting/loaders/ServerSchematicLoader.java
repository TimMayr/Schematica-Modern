package com.github.lunatrius.schematica.accounting.loaders;

import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.accounting.SchematicLocation;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;

import java.util.concurrent.CompletableFuture;

public class ServerSchematicLoader {
	public static CompletableFuture<ISchematic> get(SchematicHolder holder) {
		if (holder.locationType() == SchematicLocation.LOCAL) {
			return CompletableFuture.supplyAsync(() -> SchematicFormat.readFromFile(holder.getPath(),
					Reference.proxy.getLevel()));
		} else {
			return CompletableFuture.supplyAsync(() -> {

			});
		}
	}
}
