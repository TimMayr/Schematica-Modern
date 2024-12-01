package com.github.lunatrius.schematica.block.state;

import net.minecraft.block.Block;
import net.minecraft.state.IProperty;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.extensions.IForgeBlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockStateHelper {
	public static <T extends Comparable<T>> T getPropertyValue(final IForgeBlockState blockState, final String name) {
		final IProperty<T> property = getProperty(blockState, name);
		if (property == null) {
			throw new IllegalArgumentException(name + " does not exist in " + blockState);
		}

		return blockState.getBlockState().get(property);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T extends Comparable<T>> IProperty<T> getProperty(final IForgeBlockState blockState,
	                                                                 final String name) {
		for (final IProperty prop : blockState.getBlockState().getBlockState().getProperties()) {
			if (prop.getName().equals(name)) {
				return prop;
			}
		}

		return null;
	}

	public static List<String> getFormattedProperties(final IForgeBlockState blockState) {
		final List<String> list = new ArrayList<>();

		for (final Map.Entry<IProperty<?>, Comparable<?>> entry : blockState.getBlockState().getValues().entrySet()) {
			final IProperty<?> key = entry.getKey();
			final Comparable<?> value = entry.getValue();

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

	public static boolean areBlockStatesEqual(final IForgeBlockState blockStateA, final IForgeBlockState blockStateB) {
		if (blockStateA == blockStateB) {
			return true;
		}

		final Block blockA = blockStateA.getBlockState().getBlock();
		final Block blockB = blockStateB.getBlockState().getBlock();

		return blockA == blockB;
	}
}
