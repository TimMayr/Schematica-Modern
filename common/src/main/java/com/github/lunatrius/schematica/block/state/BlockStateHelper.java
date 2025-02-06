package com.github.lunatrius.schematica.block.state;


import net.minecraft.ChatFormatting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockStateHelper {
	public static List<String> getFormattedProperties(BlockState blockState) {
		List<String> list = new ArrayList<>();

		for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
			Property<?> key = entry.getKey();
			Comparable<?> value = entry.getValue();

			String formattedValue = value.toString();
			if (Boolean.TRUE.equals(value)) {
				formattedValue = ChatFormatting.GREEN + formattedValue + ChatFormatting.RESET;
			} else if (Boolean.FALSE.equals(value)) {
				formattedValue = ChatFormatting.RED + formattedValue + ChatFormatting.RESET;
			}

			list.add(key.getName() + ": " + formattedValue);
		}

		return list;
	}

	@SuppressWarnings("rawtypes")
	public static <T> Map<Property, T> getProperties(BlockState blockState) {
		Map<Property, T> properties = new HashMap<>();

		blockState.getProperties()
		          .forEach(property -> properties.put(property, BlockStateHelper.getPropertyValue(blockState,
		                                                                                          property.getName())));

		return properties;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> T getPropertyValue(BlockState blockState, String name) {
		Property<T> property = getProperty(blockState, name);
		if (property == null) {
			throw new IllegalArgumentException(name + " does not exist in " + blockState);
		}

		return blockState.getValue(property);
	}

	@SuppressWarnings({"rawtypes"})
	public static Property getProperty(BlockState blockState, String name) {
		for (Property prop : blockState.getProperties()) {
			if (prop.getName().equals(name)) {
				return prop;
			}
		}

		return null;
	}

	public static boolean areBlockStatesEqual(BlockState blockStateA, BlockState blockStateB) {
		if (blockStateA == blockStateB) {
			return true;
		}

		Block blockA = blockStateA.getBlock();
		Block blockB = blockStateB.getBlock();

		return blockA == blockB;
	}
}