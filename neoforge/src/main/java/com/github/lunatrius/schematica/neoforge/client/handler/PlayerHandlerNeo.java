package com.github.lunatrius.schematica.neoforge.client.handler;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
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