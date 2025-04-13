package com.github.lunatrius.schematica.fabric.api.event;

import com.github.lunatrius.schematica.api.ISchematic;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This event is fired after the schematic has been Captured, but before it is serialized to the schematic format.
 * This is your opportunity to add Metadata.
 * Register to this event using PreSchematicSaveCallback.EVENT.register()
 */
public interface PreSchematicSaveCallback {
	Event<PreSchematicSaveCallback> EVENT = EventFactory.createArrayBacked(PreSchematicSaveCallback.class,
			(listeners) -> (schematic, mappings,
			                extendedMetadata) -> {
				if (extendedMetadata == null) {
					extendedMetadata =
							new CompoundTag();
				}

				for (PreSchematicSaveCallback listener
						: listeners) {

					boolean result =
							listener.preSave(schematic,
									mappings,
									extendedMetadata);

					if (!result) {
						return false;
					}
				}

				return true;
			});

	/**
	 * Replaces the block mapping from one name to another. Use this method with care as it is possible that
	 * the schematic will not be usable or will have blocks missing if you use an invalid value.
	 *
	 * @param oldState The old blockstate of the blockmapping.
	 * @param newState The new blockstate of the blockmapping.
	 * @param mappings the mappings in which to replace.
	 * @return true if a mapping was replaced.
	 */
	static boolean replaceMapping(@NotNull Map<BlockState, BlockState> mappings, BlockState oldState,
	                              BlockState newState) {
		mappings.remove(oldState);
		mappings.put(oldState, newState);
		return true;
	}

	boolean preSave(ISchematic schematic, Map<BlockState, BlockState> mappings, CompoundTag extendedMetadata);
}