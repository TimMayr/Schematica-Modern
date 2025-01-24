package com.github.lunatrius.schematica.fabric.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.phys.AABB;

/**
 * This event is fired before an ISchematic has been created out of a part of the world.
 * This is an appropriate place to modify the world's blocks, metadata and tile entities before they are persisted.
 * Register to this event using PreSchematicCaptureCallback.EVENT.register()
 */
public interface PreSchematicCaptureCallback {
	Event<PreSchematicCaptureCallback> EVENT =
			EventFactory.createArrayBacked(PreSchematicCaptureCallback.class, (listeners) -> (aabb -> {
				for (PreSchematicCaptureCallback listener : listeners) {

					boolean result = listener.postCapture(aabb);

					if (!result) {
						return false;
					}
				}

				return true;
			}));

	boolean postCapture(AABB aabb);
}