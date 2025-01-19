package com.github.lunatrius.schematica.fabric.proxy.fabric;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.event.PostSchematicCaptureCallback;
import com.github.lunatrius.schematica.api.event.PostSchematicSaveCallback;
import com.github.lunatrius.schematica.api.event.PreSchematicCaptureCallback;
import com.github.lunatrius.schematica.api.event.PreSchematicSaveCallback;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.io.File;
import java.util.Map;

public class PlatformProxyImpl {
	public static void createAndPostPostSchematicCaptureEvent(ISchematic schematic) {
		PostSchematicCaptureCallback.EVENT.invoker().postCapture(schematic);
	}

	public static void createAndPostPreSchematicSaveEvent(ISchematic schematic, Map<String, Block> mappings) {
		PreSchematicSaveCallback.EVENT.invoker().preSave(schematic, mappings, null);
	}

	public static void createAndPostPreSchematicCaptureEvent(AABB aabb) {
		PreSchematicCaptureCallback.EVENT.invoker().postCapture(aabb);
	}

	public static void createAndPostPostSchematicSaveEvent(File file) {
		PostSchematicSaveCallback.EVENT.invoker().postSave(file);
	}
}