package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.api.ISchematic;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.nio.file.Path;
import java.util.Map;

public class PlatformProxy {
	@ExpectPlatform
	public static void createAndPostPostSchematicCaptureEvent(ISchematic schematic) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void createAndPostPreSchematicSaveEvent(ISchematic schematic, Map<BlockState, BlockState> mappings) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void createAndPostPreSchematicCaptureEvent(AABB aabb) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void createAndPostPostSchematicSaveEvent(Path file) {
		throw new AssertionError();
	}
}