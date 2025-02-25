package com.github.lunatrius.schematica.accounting.loader;

import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.accounting.SchematicLocation;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.message.download.DownloadType;
import com.github.lunatrius.schematica.network.message.download.MessageRequestDownload;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import commonnetwork.api.Dispatcher;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ClientSchematicLoader {
	public static @NotNull CompletableFuture<ISchematic> get(@NotNull SchematicHolder holder) {
		if (holder.location() == SchematicLocation.LOCAL) {
			return CompletableFuture.completedFuture(SchematicFormat.readSchematic(holder.metadata(),
					Minecraft.getInstance().level));
		} else {
			CompletableFuture<ISchematic> future = new CompletableFuture<>();
			MessageRequestDownload message = new MessageRequestDownload(holder.metadata().id(),
					DownloadType.SAVE_TEMP);
			DownloadHandler.INSTANCE.registerDownloadCompleteListener(schematic -> {
				if (schematic.getMetadata().id().equals(holder.metadata().id())) {
					future.complete(schematic);
				}
			});

			Dispatcher.sendToServer(message);
			return future;
		}
	}
}
