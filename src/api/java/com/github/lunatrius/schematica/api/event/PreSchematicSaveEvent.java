package com.github.lunatrius.schematica.api.event;

import com.github.lunatrius.schematica.api.ISchematic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;

import java.util.Map;

/**
 * This event is fired after the schematic has been Captured, but before it is serialized to the schematic format.
 * This is your opportunity to add Metadata.
 * Register to this event using NeoForge.EVENT_BUS
 */
public class PreSchematicSaveEvent extends Event {
	/**
	 * The schematic that will be saved.
	 */
	public final ISchematic schematic;
	/**
	 * The Extended Metadata tag compound provides a facility to add custom metadata to the schematic.
	 */
	public final CompoundTag extendedMetadata;
	private final Map<String, Block> mappings;

	@Deprecated
	public PreSchematicSaveEvent(Map<String, Block> mappings) {
		this(null, mappings);
	}

	public PreSchematicSaveEvent(ISchematic schematic, Map<String, Block> mappings) {
		this.schematic = schematic;
		this.mappings = mappings;
		this.extendedMetadata = new CompoundTag();
	}

	/**
	 * Replaces the block mapping from one name to another. Use this method with care as it is possible that the
	 * schematic
	 * will not be usable or will have blocks missing if you use an invalid value.
	 * <p>
	 * Attempting to remap two blocks to the same name will result in a DuplicateMappingException. If you wish for this
	 * type of collision, you can work around it by merging the two sets of block into a single BlockType in the
	 * PostSchematicCaptureEvent.
	 *
	 * @param oldName
	 * 		The old name of the block mapping.
	 * @param newName
	 * 		The new name of the block mapping.
	 *
	 * @return true if a mapping was replaced.
	 *
	 * @throws DuplicateMappingException
	 * 		If the mapping already exists
	 */
	public boolean replaceMapping(String oldName, String newName) throws DuplicateMappingException {
		if (this.mappings.containsKey(newName)) {
			throw new DuplicateMappingException(String.format(
					"Could not replace block type %s, the block type %s already exists in the " + "schematic.",
					oldName,
					newName));
		}

		Block id = this.mappings.get(oldName);
		if (id != null) {
			this.mappings.remove(oldName);
			this.mappings.put(newName, id);
			return true;
		}

		return false;
	}
}