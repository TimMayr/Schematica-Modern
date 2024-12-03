package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Queue;

@Mod.EventBusSubscriber
public class QueueTickHandler {
	public static final QueueTickHandler INSTANCE = new QueueTickHandler();

	private final Queue<SchematicContainer> queue = new ArrayDeque<>();

	private QueueTickHandler() {}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}

		// TODO: find a better way... maybe?
		try {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (player != null && player.connection != null && !player.connection.getNetworkManager()
			                                                                     .isLocalChannel()) {
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
				TranslationTextComponent component =
						new TranslationTextComponent(Names.Command.Save.Message.SAVE_STARTED, container.chunkCount,
						                             container.file.getName());
				container.player.sendMessage(component);
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

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}

		processQueue();
	}

	public void queueSchematic(SchematicContainer container) {
		this.queue.offer(container);
	}
}
