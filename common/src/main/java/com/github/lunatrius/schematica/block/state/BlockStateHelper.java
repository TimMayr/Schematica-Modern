package com.github.lunatrius.schematica.block.state;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockStateHelper {
	public static List<String> getFormattedProperties(BlockState blockState) {
		List<String> list = new ArrayList<>();

		for (Map.Entry<IProperty<?>, Comparable<?>> entry : blockState.getBlockState().getValues().entrySet()) {
			IProperty<?> key = entry.getKey();
			Comparable<?> value = entry.getValue();

			String formattedValue = value.toString();
			if (Boolean.TRUE.equals(value)) {
				formattedValue = TextFormatting.GREEN + formattedValue + TextFormatting.RESET;
			} else if (Boolean.FALSE.equals(value)) {
				formattedValue = TextFormatting.RED + formattedValue + TextFormatting.RESET;
			}

			list.add(key.getName() + ": " + formattedValue);
		}

		return list;
	}

	@SuppressWarnings("rawtypes")
	public static <T> Map<IProperty, T> getProperties(BlockState blockState) {
		Map<IProperty, T> properties = new HashMap<>();

		blockState.getProperties()
		          .forEach(property -> properties.put(property, BlockStateHelper.getPropertyValue(blockState,
		                                                                                          property.getName())));

		return properties;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> T getPropertyValue(BlockState blockState, String name) {
		IProperty<T> property = getProperty(blockState, name);
		if (property == null) {
			throw new IllegalArgumentException(name + " does not exist in " + blockState);
		}

		return blockState.getBlockState().get(property);
	}

	@SuppressWarnings({"rawtypes"})
	public static IProperty getProperty(BlockState blockState, String name) {
		for (IProperty prop : blockState.getProperties()) {
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
