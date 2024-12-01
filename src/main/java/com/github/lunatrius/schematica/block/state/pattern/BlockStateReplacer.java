package com.github.lunatrius.schematica.block.state.pattern;

import com.github.lunatrius.core.exceptions.LocalizedException;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//TODO: FIX THIS SHIT
public class BlockStateReplacer {
	private final BlockState defaultReplacement;

	private BlockStateReplacer(final BlockState defaultReplacement) {
		this.defaultReplacement = defaultReplacement;
	}

	public static BlockStateReplacer forBlockState(final BlockState replacement) {
		return new BlockStateReplacer(replacement);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static BlockStateMatcher getMatcher(final BlockStateInfo blockStateInfo) {
		final BlockStateMatcher matcher = BlockStateMatcher.forBlock(blockStateInfo.block);
		for (final Map.Entry<IProperty, Comparable> entry : blockStateInfo.stateData.entrySet()) {
			matcher.where(entry.getKey(), input -> input != null && input.equals(entry.getValue()));
		}

		return matcher;
	}

	@SuppressWarnings({"rawtypes"})
	public static BlockStateInfo fromBlockState(BlockState blockState) {
		Map<IProperty, Comparable> properties = new HashMap<>();
		for (IProperty<?> property : blockState.getProperties()) {
			Comparable values = blockState.get(property);
			properties.put(property, values);
		}
		return new BlockStateInfo(blockState.getBlock(), properties);
	}

	@SuppressWarnings({"rawtypes", "DataFlowIssue"})
	public static BlockStateInfo fromString(final String input) throws LocalizedException {
		final int start = input.indexOf('[');
		final int end = input.indexOf(']');

		final String blockName;
		final String stateData;
		if (start > -1 && end > -1) {
			blockName = input.substring(0, start);
			stateData = input.substring(start + 1, end);
		} else {
			blockName = input;
			stateData = "";
		}

		final ResourceLocation location = new ResourceLocation(blockName);
		final IForgeRegistry<Block> blocks = ForgeRegistries.BLOCKS;
		if (!blocks.containsKey(location)) {
			throw new LocalizedException(Names.Messages.INVALID_BLOCK, blockName);
		}

		final Block block = blocks.getValue(location);

		final Map<IProperty, Comparable> propertyData = parsePropertyData(block.getDefaultState(), stateData, true);
		return new BlockStateInfo(block, propertyData);
	}

	@SuppressWarnings({"rawtypes"})
	public static Map<IProperty, Comparable> parsePropertyData(final IForgeBlockState blockState,
	                                                           final String stateData, final boolean strict)
			throws LocalizedException {
		final HashMap<IProperty, Comparable> map = new HashMap<>();
		if (stateData == null || stateData.isEmpty()) {
			return map;
		}

		final String[] propertyPairs = stateData.split(",");
		for (final String propertyPair : propertyPairs) {
			final String[] split = propertyPair.split("=");
			if (split.length != 2) {
				throw new LocalizedException(Names.Messages.INVALID_PROPERTY, propertyPair);
			}

			putMatchingProperty(map, blockState, split[0], split[1], strict);
		}

		return map;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static boolean putMatchingProperty(final Map<IProperty, Comparable> map, final IForgeBlockState blockState,
	                                           final String name, final String value, final boolean strict)
			throws LocalizedException {
		for (final IProperty property : blockState.getBlockState().getProperties()) {
			if (property.getName().equalsIgnoreCase(name)) {
				final Collection<Comparable> allowedValues = property.getAllowedValues();
				for (final Comparable allowedValue : allowedValues) {
					if (String.valueOf(allowedValue).equalsIgnoreCase(value)) {
						map.put(property, allowedValue);
						return true;
					}
				}
			}
		}

		if (strict) {
			throw new LocalizedException(Names.Messages.INVALID_PROPERTY_FOR_BLOCK, name + "=" + value,
					blockState.getBlockState().getBlock().getNameTextComponent());
		}

		return false;
	}


	@SuppressWarnings({"rawtypes"})
	public BlockState getReplacement(final BlockState original,
	                                                           final Map<IProperty, Comparable> properties) {
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
				mutableState = mutableState.getBlockState().with(property, value);
			}
		}

		return mutableState;
	}


	@SuppressWarnings("rawtypes")
	public static class BlockStateInfo {
		public final Block block;
		public final Map<IProperty, Comparable> stateData;

		public BlockStateInfo(final Block block,
		                      final Map<IProperty, Comparable> stateData) {
			this.block = block;
			this.stateData = stateData;
		}
	}
}
