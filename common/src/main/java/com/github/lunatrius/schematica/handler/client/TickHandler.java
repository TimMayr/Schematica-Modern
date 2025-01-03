package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.profiling.Profiler;

public class TickHandler {
	public static final TickHandler INSTANCE = new TickHandler();

	private int ticks = -1;

	private TickHandler() {
		PlayerEvent.PLAYER_QUIT.register(player -> {
			Reference.logger.info("Scheduling client settings reset.");
			ClientProxy.isPendingReset = true;
		});

		ClientTickEvent.CLIENT_PRE.register(instance -> {
			if (instance.isPaused()) {
				return;
			}

			Profiler.get().push("schematica");
			ClientLevel world = instance.level;
			LocalPlayer player = instance.player;
			SchematicWorld schematic = ClientProxy.schematic;
			if (world != null && player != null && schematic != null && schematic.isRendering) {
				Profiler.get().push("printer");
				SchematicPrinter printer = SchematicPrinter.INSTANCE;
				if (printer.isEnabled() && printer.isPrinting() && this.ticks-- < 0) {
					this.ticks = SchematicaConfig.CLIENT.placeDelay.get();

					printer.print(world, player);
				}

				Profiler.get().pop();
			}

			if (ClientProxy.isPendingReset) {
				Reference.proxy.resetSettings();
				ClientProxy.isPendingReset = false;
				Reference.logger.info("Client settings have been reset.");
			}

			Profiler.get().pop();
		});
	}
}