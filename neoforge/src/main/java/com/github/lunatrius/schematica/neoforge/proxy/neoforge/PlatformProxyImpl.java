package com.github.lunatrius.schematica.neoforge.proxy.neoforge;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.neoforge.api.event.PostSchematicCaptureEvent;
import com.github.lunatrius.schematica.neoforge.api.event.PostSchematicSaveEvent;
import com.github.lunatrius.schematica.neoforge.api.event.PreSchematicCaptureEvent;
import com.github.lunatrius.schematica.neoforge.api.event.PreSchematicSaveEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;

import java.io.File;
import java.util.Map;

public class PlatformProxyImpl {
	public static void createAndPostPostSchematicCaptureEvent(ISchematic schematic) {
		NeoForge.EVENT_BUS.post(new PostSchematicCaptureEvent(schematic));
	}

	public static void createAndPostPreSchematicSaveEvent(ISchematic schematic, Map<String, Block> mappings) {
		NeoForge.EVENT_BUS.post(new PreSchematicSaveEvent(schematic, mappings));
	}

	public static void createAndPostPreSchematicCaptureEvent(AABB aabb) {
		NeoForge.EVENT_BUS.post(new PreSchematicCaptureEvent(aabb));
	}

	public static void createAndPostPostSchematicSaveEvent(File file) {
		NeoForge.EVENT_BUS.post(new PostSchematicSaveEvent(file));
	}
}