package com.github.lunatrius.schematica.fabric.api.event;

import com.github.lunatrius.schematica.api.ISchematic;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * This event is fired after an ISchematic has been created out of a part of the world.
 * This is an appropriate place to modify the schematic's blocks, metadata and tile entities before they are persisted.
 * Register to this event using PostSchematicCaptureCallback.EVENT.register()
 */
public interface PostSchematicCaptureCallback {
	Event<PostSchematicCaptureCallback> EVENT =
			EventFactory.createArrayBacked(PostSchematicCaptureCallback.class, (listeners) -> (schematic -> {
				for (PostSchematicCaptureCallback listener : listeners) {

					boolean result = listener.postCapture(schematic);

					if (!result) {
						return false;
					}
				}

				return true;
			}));

	boolean postCapture(ISchematic schematic);
}