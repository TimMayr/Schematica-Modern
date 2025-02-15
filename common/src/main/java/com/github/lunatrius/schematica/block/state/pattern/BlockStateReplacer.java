package com.github.lunatrius.schematica.block.state.pattern;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BlockStateReplacer {
	private final BlockState defaultReplacement;

	private BlockStateReplacer(BlockState defaultReplacement) {
		this.defaultReplacement = defaultReplacement;
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull BlockStateReplacer forBlockState(BlockState replacement) {
		return new BlockStateReplacer(replacement);
	}

	@SuppressWarnings({"rawtypes"})
	public BlockState getReplacement(Map<Property, Comparable> properties) {
		return applyProperties(defaultReplacement, properties);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private BlockState applyProperties(BlockState state, @NotNull Map<Property, Comparable> properties) {
		BlockState mutableState = state;

		for (Map.Entry<Property, Comparable> entry : properties.entrySet()) {
			Property property = entry.getKey();
			Comparable value = entry.getValue();

			if (mutableState.hasProperty(property) && mutableState.getValue(property) != value) {
				mutableState = mutableState.setValue(property, value);
			}
		}

		return mutableState;
	}
}