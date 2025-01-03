package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.network.message.MessageCapabilities;
import dev.architectury.event.events.common.PlayerEvent;

public class PlayerHandler {
	public static final PlayerHandler INSTANCE = new PlayerHandler();

	private PlayerHandler() {
		PlayerEvent.PLAYER_JOIN.register((player) -> PacketHandler.INSTANCE.send(
				PacketDistributor.PLAYER.with(() -> player),
				new MessageCapabilities(SchematicaConfig.SERVER.printerEnabled.get(),
				                        SchematicaConfig.SERVER.saveEnabled.get(),
				                        SchematicaConfig.SERVER.loadEnabled.get())));

		PlayerEvent.PLAYER_QUIT.register(
				player -> DownloadHandler.INSTANCE.transferMap.remove(player.getScoreboardName()));
	}
}