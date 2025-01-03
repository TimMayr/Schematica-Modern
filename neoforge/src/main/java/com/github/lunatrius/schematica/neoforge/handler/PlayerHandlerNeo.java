package com.github.lunatrius.schematica.neoforge.handler;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class PlayerHandlerNeo {
	public static PlayerHandlerNeo INSTANCE = new PlayerHandlerNeo();

	private PlayerHandlerNeo() {}

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