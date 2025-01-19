package com.github.lunatrius.schematica.api.event;

import com.github.lunatrius.schematica.api.ISchematic;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;

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
	 * the schematic will not be usable or will have blocks missing if you use an invalid value. <p> Attempting to
	 * remap two blocks to the same name will result in a DuplicateMappingException. If you wish for this type of
	 * collision, you can work around it by merging the two sets of block into a single BlockType in
	 * the PostSchematicCaptureEvent.
	 *
	 * @param oldName
	 * 		The old name of the blockmapping.
	 * @param newName
	 * 		The new name of the block mapping.
	 * @param mappings
	 * 		the mappings in which to replace
	 *
	 * @return true if a mapping was replaced.
	 *
	 * @throws DuplicateMappingException
	 * 		If the mapping already exists
	 */
	static boolean replaceMapping(Map<String, Block> mappings, String oldName, String newName)
			throws DuplicateMappingException {
		if (mappings.containsKey(newName)) {
			throw new DuplicateMappingException(String.format(
					"Could not replace block type %s, the block type %s already exists in the " + "schematic.",
					oldName,
					newName));
		}

		Block id = mappings.get(oldName);
		if (id != null) {
			mappings.remove(oldName);
			mappings.put(newName, id);
			return true;
		}

		return false;
	}

	boolean preSave(ISchematic schematic, Map<String, Block> mappings, CompoundTag extendedMetadata);
}