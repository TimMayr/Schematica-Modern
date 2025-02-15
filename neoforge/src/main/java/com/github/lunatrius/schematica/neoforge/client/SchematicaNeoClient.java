package com.github.lunatrius.schematica.neoforge.client;

import com.github.lunatrius.schematica.client.SchematicaClient;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.neoforge.client.handler.PlayerHandlerNeo;
import com.github.lunatrius.schematica.neoforge.client.handler.RenderTickHandlerNeo;
import com.github.lunatrius.schematica.reference.Reference;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

@Mod(value = Reference.MOD_ID, dist = Dist.CLIENT)
public class SchematicaNeoClient {
	public SchematicaNeoClient(@NotNull ModContainer modContainer) {
		SchematicaClient.clientInit();
		NeoForge.EVENT_BUS.register(PlayerHandlerNeo.INSTANCE);
		NeoForge.EVENT_BUS.register(RenderTickHandlerNeo.INSTANCE);
		modContainer.registerConfig(ModConfig.Type.CLIENT, SchematicaClientConfig.clientSpec);
	}
}