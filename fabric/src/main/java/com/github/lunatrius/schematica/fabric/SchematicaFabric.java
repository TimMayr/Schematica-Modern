package com.github.lunatrius.schematica.fabric;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.reference.Reference;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;

public final class SchematicaFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// Run our common setup.
		NeoForgeConfigRegistry.INSTANCE.register(Reference.MOD_ID, ModConfig.Type.CLIENT,
				SchematicaClientConfig.clientSpec);
		NeoForgeConfigRegistry.INSTANCE.register(Reference.MOD_ID, ModConfig.Type.SERVER, SchematicaConfig.serverSpec);

		Schematica.init();
	}
}