package com.github.lunatrius.schematica.neoforge.client;

import com.github.lunatrius.schematica.neoforge.client.handler.PlayerHandlerNeo;
import com.github.lunatrius.schematica.reference.Reference;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Reference.MOD_ID, dist = Dist.CLIENT)
public class SchematicaNeoClient {
	public SchematicaNeoClient() {
		NeoForge.EVENT_BUS.register(PlayerHandlerNeo.INSTANCE);
	}
}