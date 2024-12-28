package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueTickHandler {
	public static final QueueTickHandler INSTANCE = new QueueTickHandler();

	private final Queue<SchematicContainer> queue = new ArrayDeque<>();

	private QueueTickHandler() {}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent.Post event) {
		// TODO: find a better way... maybe?
		try {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null && !player.connection.getConnection().isMemoryConnection()) {
				processQueue();
			}
		} catch (Exception e) {
			Reference.logger.error("Something went wrong...", e);
		}
	}

	private void processQueue() {
		if (this.queue.isEmpty()) {
			return;
		}

		SchematicContainer container = this.queue.poll();
		if (container == null) {
			return;
		}

		if (container.hasNext()) {
			if (container.isFirst()) {
				Component component =
						Component.translatable(Names.Command.Save.Message.SAVE_STARTED, container.chunkCount,
						                       container.file.getName());
				if (container.player != null && !container.player.isLocalPlayer()) {
					((ServerPlayer) container.player).sendSystemMessage(component);
				}

				container.next();
			}

			if (container.hasNext()) {
				this.queue.offer(container);
			} else {
				SchematicFormat.writeToFileAndNotify(container.file, container.format, container.schematic,
				                                     container.player);
			}
		}
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) {
		processQueue();
	}

	public void queueSchematic(SchematicContainer container) {
		this.queue.offer(container);
	}
}