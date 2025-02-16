package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.network.message.MessageCapabilities;
import commonnetwork.api.Dispatcher;
import dev.architectury.event.events.common.PlayerEvent;

public class PlayerHandler {
	public static PlayerHandler INSTANCE;

	private PlayerHandler() {
		PlayerEvent.PLAYER_JOIN.register((player) -> {
			Dispatcher.sendToClient(
					new MessageCapabilities(SchematicaConfig.SERVER.printerEnabled.get(),
							SchematicaConfig.SERVER.saveEnabled.get(),
							SchematicaConfig.SERVER.loadEnabled.get()), player);

			SchematicAccounter.syncAllToPlayer(player);
		});

		PlayerEvent.PLAYER_QUIT.register(
				player -> DownloadHandler.INSTANCE.transferMap.remove(player.getScoreboardName()));
	}

	public static void init() {
		PlayerHandler.INSTANCE = new PlayerHandler();
	}
}