package com.github.lunatrius.schematica.neoforge.api.event;

import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.Event;

/**
 * This event is fired before an ISchematic has been created out of a part of the world.
 * This is an appropriate place to modify the world's blocks, metadata and tile entities before they are persisted.
 * Register to this event using NeoForge.EVENT_BUS
 */
public class PreSchematicCaptureEvent extends Event {
	/**
	 * The AABB enclosing the schematic
	 */
	public final AABB aabb;

	public PreSchematicCaptureEvent(AABB aabb) {
		this.aabb = aabb;
	}
}