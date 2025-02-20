package com.github.lunatrius.schematica.accounting.loaders;

import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.accounting.SchematicLocation;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.network.message.download.MessageRequestDownload;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import commonnetwork.api.Dispatcher;

import java.util.concurrent.CompletableFuture;

public class ClientSchematicLoader {
	public static CompletableFuture<ISchematic> get(SchematicHolder holder) {
		if (holder.locationType() == SchematicLocation.LOCAL) {
			return CompletableFuture.supplyAsync(() -> SchematicFormat.readFromFile(holder.getPath(),
					Reference.proxy.getLevel()));
		} else {
			return CompletableFuture.supplyAsync(() -> {
				Dispatcher.sendToServer(new MessageRequestDownload(holder.get));
			});
		}
	}
}
