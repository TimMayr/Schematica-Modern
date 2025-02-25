package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueTickHandler {
	public static QueueTickHandler INSTANCE;
	private final Queue<SchematicContainer> queue = new ArrayDeque<>();

	private QueueTickHandler() {
		TickEvent.SERVER_POST.register((server) -> processQueue());
	}

	public static void init() {
		QueueTickHandler.INSTANCE = new QueueTickHandler();
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
								container.file.getFileName().toString());
				container.player.displayClientMessage(component, false);
			}

			container.next();

			if (container.hasNextChunk()) {
				this.queue.offer(container);
			} else {
				SchematicFormat.writeToFileAndNotify(container.schematic, container.player);
			}
		}
	}

	public void queueSchematic(SchematicContainer container) {
		this.queue.offer(container);
	}
}