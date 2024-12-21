package com.github.lunatrius.schematica.block.state.pattern;

import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;

import java.util.Map;

public class BlockStateReplacer {
	private final BlockState defaultReplacement;

	private BlockStateReplacer(BlockState defaultReplacement) {
		this.defaultReplacement = defaultReplacement;
	}

	public static BlockStateReplacer forBlockState(BlockState replacement) {
		return new BlockStateReplacer(replacement);
	}

	@SuppressWarnings({"rawtypes"})
	public BlockState getReplacement(Map<IProperty, Comparable> properties) {
		return applyProperties(defaultReplacement, properties);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private BlockState applyProperties(BlockState state, Map<IProperty, Comparable> properties) {
		BlockState mutableState = state;

		for (Map.Entry<IProperty, Comparable> entry : properties.entrySet()) {
			IProperty property = entry.getKey();
			Comparable value = entry.getValue();

			if (mutableState.getBlockState().has(property) && mutableState.getBlockState().get(property) != value) {
				mutableState = mutableState.with(property, value);
			}
		}

		return mutableState;
	}
}