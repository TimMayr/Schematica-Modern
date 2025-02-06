package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.api.ISchematic;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.io.File;
import java.util.Map;

public class PlatformProxy {
	@ExpectPlatform
	public static void createAndPostPostSchematicCaptureEvent(ISchematic schematic) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void createAndPostPreSchematicSaveEvent(ISchematic schematic, Map<String, Block> mappings) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void createAndPostPreSchematicCaptureEvent(AABB aabb) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void createAndPostPostSchematicSaveEvent(File file) {
		throw new AssertionError();
	}
}