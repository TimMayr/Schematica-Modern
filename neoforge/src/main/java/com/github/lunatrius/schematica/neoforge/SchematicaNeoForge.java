package com.github.lunatrius.schematica.neoforge;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Reference;
import net.neoforged.fml.common.Mod;

@Mod(Reference.MODID)
public final class SchematicaNeoForge {
	public SchematicaNeoForge() {
		// Run our common setup.
		Schematica.init();
	}
}