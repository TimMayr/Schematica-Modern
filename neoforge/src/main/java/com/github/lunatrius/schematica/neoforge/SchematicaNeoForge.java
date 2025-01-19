package com.github.lunatrius.schematica.neoforge;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.reference.Reference;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Reference.MOD_ID)
public final class SchematicaNeoForge {
	public SchematicaNeoForge(IEventBus modEventBus, ModContainer modContainer) {
		// Run our common setup.
		Schematica.init();

		NeoForge.EVENT_BUS.register(this);
		modEventBus.register(this);

		modContainer.registerConfig(ModConfig.Type.CLIENT, SchematicaConfig.clientSpec);
		modContainer.registerConfig(ModConfig.Type.SERVER, SchematicaConfig.serverSpec);
	}
}