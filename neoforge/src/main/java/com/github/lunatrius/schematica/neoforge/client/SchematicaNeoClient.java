package com.github.lunatrius.schematica.neoforge.client;

import com.github.lunatrius.schematica.client.SchematicaClient;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.handler.client.InputHandler;
import com.github.lunatrius.schematica.neoforge.client.handler.PlayerHandlerNeo;
import com.github.lunatrius.schematica.neoforge.client.handler.RenderTickHandlerNeo;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

@Mod(value = Reference.MOD_ID, dist = Dist.CLIENT)
public class SchematicaNeoClient {
	public SchematicaNeoClient(@NotNull IEventBus modBus, @NotNull ModContainer modContainer) {
		SchematicaClient.clientInit();
		NeoForge.EVENT_BUS.register(PlayerHandlerNeo.INSTANCE);
		NeoForge.EVENT_BUS.register(RenderTickHandlerNeo.INSTANCE);
		modBus.register(this);

		modContainer.registerConfig(ModConfig.Type.CLIENT, SchematicaClientConfig.clientSpec);
	}

	//TODO: Maybe move this back to arch in case that works
	@SubscribeEvent
	public void registerKeys(@NotNull RegisterKeyMappingsEvent event) {
		for (KeyMapping keyBinding : InputHandler.KEY_BINDINGS) {
			event.register(keyBinding);
		}
	}
}