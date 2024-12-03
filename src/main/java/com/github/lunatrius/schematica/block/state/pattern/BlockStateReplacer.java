package com.github.lunatrius.schematica.block.state.pattern;

import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockStateMatcher;
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

	public static BlockStateMatcher getMatcher(BlockState blockState) {
		BlockStateMatcher matcher = BlockStateMatcher.forBlock(blockState.getBlock());

		for (IProperty<?> property : blockState.getProperties()) {
			matcher.where(property, input -> input != null && input.equals(property));
		}

		return matcher;
	}

	@SuppressWarnings({"rawtypes"})
	public BlockState getReplacement(BlockState original, Map<IProperty, Comparable> properties) {
		BlockState replacement = original;

		if (original == null) {
			replacement = defaultReplacement;
		}

		replacement = applyProperties(replacement, properties);

		return replacement;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private BlockState applyProperties(BlockState state, Map<IProperty, Comparable> properties) {
		BlockState mutableState = state;

		for (Map.Entry<IProperty, Comparable> entry : properties.entrySet()) {
			IProperty property = entry.getKey();
			Comparable value = entry.getValue();

			if (mutableState.getBlockState().get(property) != value) {
				mutableState = mutableState.with(property, value);
			}
		}

		return mutableState;
	}
}
