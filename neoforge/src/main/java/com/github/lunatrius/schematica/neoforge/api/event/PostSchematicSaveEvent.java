package com.github.lunatrius.schematica.neoforge.api.event;

import net.neoforged.bus.api.Event;

import java.nio.file.Path;

/**
 * This event is fired after the schematic has been serialized to the schematic format.
 * This is your opportunity to modify the resulting schematic directly, or modify the file itself.
 * Register to this event using NeoForge.EVENT_BUS
 */
public class PostSchematicSaveEvent extends Event {
	/**
	 * The file to which the schematic was saved.
	 */
	public final Path file;

	public PostSchematicSaveEvent(Path schematic) {
		this.file = schematic;
	}
}