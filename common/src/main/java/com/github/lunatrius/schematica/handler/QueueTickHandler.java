package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.chunk.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueTickHandler {
	public static final QueueTickHandler INSTANCE = new QueueTickHandler();
	private final Queue<SchematicContainer> queue = new ArrayDeque<>();

	private QueueTickHandler() {
		TickEvent.PLAYER_POST.register((player) -> {
			if (player instanceof LocalPlayer localPlayer) {
				if (!localPlayer.connection.getConnection().isMemoryConnection()) {
					processQueue();
				}
			}
		});

		TickEvent.SERVER_POST.register((server) -> processQueue());
	}

	private void processQueue() {
		if (this.queue.isEmpty()) {
			return;
		}

		SchematicContainer container = this.queue.poll();
		if (container == null) {
			return;
		}

		if (container.hasNextChunk()) {
			if (container.isFirstChunk()) {
				Component component =
						Component.translatable(Names.Command.Save.Message.SAVE_STARTED, container.chunkCount,
						                       container.file.getName());
				container.player.displayClientMessage(component, false);
			}

			container.next();

			if (container.hasNextChunk()) {
				this.queue.offer(container);
			} else {
				SchematicFormat.writeToFileAndNotify(container.file, container.format, container.schematic,
				                                     container.player);
			}
		}
	}

	public void queueSchematic(SchematicContainer container) {
		this.queue.offer(container);
	}
}