package com.github.lunatrius.schematica.fabric.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.nio.file.Path;

/**
 * This event is fired after the schematic has been serialized to the schematic format.
 * This is your opportunity to modify the resulting schematic directly, or modify the file itself.
 * Register to this event using PostSchematicSaveCallback.EVENT.register()
 */
public interface PostSchematicSaveCallback {
	Event<PostSchematicSaveCallback> EVENT =
			EventFactory.createArrayBacked(PostSchematicSaveCallback.class, (listeners) -> (file) -> {

				for (PostSchematicSaveCallback listener : listeners) {

					boolean result = listener.postSave(file);

					if (!result) {
						return false;
					}
				}

				return true;
			});

	boolean postSave(Path file);
}