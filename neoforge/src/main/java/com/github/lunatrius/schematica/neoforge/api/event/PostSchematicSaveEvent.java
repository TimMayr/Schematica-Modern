package com.github.lunatrius.schematica.api.event;

import net.neoforged.bus.api.Event;

import java.io.File;

/**
 * This event is fired after the schematic has been serialized to the schematic format.
 * This is your opportunity to modify the resulting schematic directly, or modify the file itself.
 * Register to this event using NeoForge.EVENT_BUS
 */
public class PostSchematicSaveEvent extends Event {
	/**
	 * The file to which the schematic was saved.
	 */
	public final File file;

	public PostSchematicSaveEvent(File schematic) {
		this.file = schematic;
	}
}