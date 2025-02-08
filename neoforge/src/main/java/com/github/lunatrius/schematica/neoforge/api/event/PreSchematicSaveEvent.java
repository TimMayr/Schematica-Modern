package com.github.lunatrius.schematica.neoforge.api.event;

import com.github.lunatrius.schematica.api.ISchematic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
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
	private final Map<BlockState, BlockState> mappings;

	public PreSchematicSaveEvent(ISchematic schematic, Map<BlockState, BlockState> mappings) {
		this.schematic = schematic;
		this.mappings = mappings;
		this.extendedMetadata = new CompoundTag();
	}

	/**
	 * Replaces the block mapping from one name to another. Use this method with care as it is possible that the
	 * schematic
	 * will not be usable or will have blocks missing if you use an invalid value.
	 *
	 * @param oldState The old name of the block mapping.
	 * @param newState The new name of the block mapping.
	 * @return true if a mapping was replaced.
	 */
	public boolean replaceMapping(BlockState oldState, BlockState newState) {
		mappings.remove(oldState);
		mappings.put(oldState, newState);
		return true;
	}
}