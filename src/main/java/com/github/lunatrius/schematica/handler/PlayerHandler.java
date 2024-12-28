package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.network.message.MessageCapabilities;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerHandler {
	public static final PlayerHandler INSTANCE = new PlayerHandler();

	private PlayerHandler() {}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof Player) {
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
		if (event.getEntity() instanceof Player) {
			DownloadHandler.INSTANCE.transferMap.remove(event.getEntity());
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
			return;
		}

		Player player = Minecraft.getInstance().player;
		if (player != null) {
			ClientProxy.setPlayerData(player, event.getPartialTick().getRealtimeDeltaTicks());
		}
	}
}