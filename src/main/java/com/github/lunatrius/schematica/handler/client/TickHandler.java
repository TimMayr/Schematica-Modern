package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.SchematicaConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickHandler {
	public static final TickHandler INSTANCE = new TickHandler();

	private final Minecraft minecraft = Minecraft.getInstance();

	private int ticks = -1;

	private TickHandler() {}

	@SubscribeEvent
	public void onClientDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
		Reference.logger.info("Scheduling client settings reset.");
		ClientProxy.isPendingReset = true;
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (this.minecraft.isGamePaused() || event.phase != TickEvent.Phase.END) {
			return;
		}

		this.minecraft.getProfiler().startSection("schematica");
		ClientWorld world = this.minecraft.world;
		ClientPlayerEntity player = this.minecraft.player;
		SchematicWorld schematic = ClientProxy.schematic;
		if (world != null && player != null && schematic != null && schematic.isRendering) {
			this.minecraft.getProfiler().startSection("printer");
			SchematicPrinter printer = SchematicPrinter.INSTANCE;
			if (printer.isEnabled() && printer.isPrinting() && this.ticks-- < 0) {
				this.ticks = SchematicaConfig.SERVER.placeDelay.get();

				printer.print(world, player);
			}

			this.minecraft.getProfiler().endSection();
		}

		if (ClientProxy.isPendingReset) {
			Reference.proxy.resetSettings();
			ClientProxy.isPendingReset = false;
			Reference.logger.info("Client settings have been reset.");
		}

		this.minecraft.getProfiler().endSection();
	}
}
