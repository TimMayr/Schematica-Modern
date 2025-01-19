package com.github.lunatrius.schematica.fabric.client;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class SchematicaFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		WorldRenderEvents.LAST.register(context -> {
			Player player = Minecraft.getInstance().player;
			if (player != null) {
				ClientProxy.setPlayerData(player, context.tickCounter().getRealtimeDeltaTicks());
			}
		});
	}
}