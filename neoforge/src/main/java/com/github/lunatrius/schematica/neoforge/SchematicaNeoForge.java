package com.github.lunatrius.schematica.neoforge;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.reference.Reference;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Reference.MOD_ID)
public final class SchematicaNeoForge {
	public SchematicaNeoForge(ModContainer modContainer) {
		// Run our common setup.
		Schematica.init();

		modContainer.registerConfig(ModConfig.Type.CLIENT, SchematicaConfig.clientSpec);
		modContainer.registerConfig(ModConfig.Type.SERVER, SchematicaConfig.serverSpec);
	}
}