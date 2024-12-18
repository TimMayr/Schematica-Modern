package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.network.message.MessageCapabilities;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PlayerHandler {
	public static final PlayerHandler INSTANCE = new PlayerHandler();

	private PlayerHandler() {}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getPlayer() instanceof PlayerEntity) {
			try {
				PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
				                            new MessageCapabilities(SchematicaConfig.SERVER.printerEnabled.get(),
				                                                    SchematicaConfig.SERVER.saveEnabled.get(),
				                                                    SchematicaConfig.SERVER.loadEnabled.get()));
			} catch (Exception ex) {
				Reference.logger.error("Failed to send capabilities!", ex);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getPlayer() instanceof PlayerEntity) {
			DownloadHandler.INSTANCE.transferMap.remove(event.getPlayer());
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(final RenderWorldLastEvent event) {
		final PlayerEntity player = Minecraft.getInstance().player;
		if (player != null) {
			ClientProxy.setPlayerData(player, event.getPartialTicks());
		}
	}
}