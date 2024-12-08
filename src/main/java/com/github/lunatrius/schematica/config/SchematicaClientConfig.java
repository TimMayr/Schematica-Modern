package com.github.lunatrius.schematica.config;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.ItemStackSortType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SchematicaClientConfig {
	public static final Queue<Integer> swapSlotsQueue = new ArrayDeque<>();
	public static final String SCHEMATIC_DEFAULT_FOLDER = "./schematics";
	private static final Set<Block> extraAirBlockList = new HashSet<>();
	public static File schematicDirectory = new File(Reference.proxy.getDataDirectory(), SCHEMATIC_DEFAULT_FOLDER);
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> extraAirBlocks;
	public final ForgeConfigSpec.ConfigValue<String> schematicDirectoryPath;
	public final ForgeConfigSpec.EnumValue<ItemStackSortType> sortType;
	public final ForgeConfigSpec.BooleanValue dumpBlockList;
	public final ForgeConfigSpec.BooleanValue showDebugInfo;
	public final ForgeConfigSpec.DoubleValue alpha;
	public final ForgeConfigSpec.BooleanValue alphaEnabled;
	public final ForgeConfigSpec.DoubleValue blockDelta;
	public final ForgeConfigSpec.BooleanValue highlight;
	public final ForgeConfigSpec.BooleanValue highlightAir;
	public final ForgeConfigSpec.IntValue renderDistance;
	public final ForgeConfigSpec.BooleanValue destroyBlocks;
	public final ForgeConfigSpec.BooleanValue destroyInstantly;
	public final ForgeConfigSpec.BooleanValue placeAdjacent;
	public final ForgeConfigSpec.IntValue placeDelay;
	public final ForgeConfigSpec.IntValue placeDistance;
	public final ForgeConfigSpec.BooleanValue placeInstantly;
	public final ForgeConfigSpec.IntValue timeout;
	public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> swapSlots;

	SchematicaClientConfig(ForgeConfigSpec.Builder builder) {
		builder.comment(Names.Config.Category.RENDER).push(Names.Config.Category.RENDER);

		alpha = builder.comment(Names.Config.ALPHA_DESC)
		               .translation("schematica.config.alpha.tooltip")
		               .defineInRange(Names.Config.ALPHA, 1.0f, 0, 1);

		alphaEnabled = builder.comment(Names.Config.ALPHA_ENABLED_DESC)
		                      .translation("schematica.config.alphaEnabled.tooltip")
		                      .define(Names.Config.ALPHA_ENABLED, false);

		blockDelta = builder.comment(Names.Config.BLOCK_DELTA_DESC)
		                    .translation("schematica.config.blockDelta.tooltip")
		                    .defineInRange(Names.Config.BLOCK_DELTA, 0.005, 0, Float.POSITIVE_INFINITY);

		highlight = builder.comment(Names.Config.HIGHLIGHT_DESC)
		                   .translation("schematica.config.highlight.tooltip")
		                   .define(Names.Config.HIGHLIGHT, true);

		highlightAir = builder.comment(Names.Config.HIGHLIGHT_AIR_DESC)
		                      .translation("schematica.config.highlightAir.tooltip")
		                      .define(Names.Config.HIGHLIGHT_AIR, true);

		renderDistance = builder.comment(Names.Config.RENDER_DISTANCE_DESC)
		                        .translation("schematica.config.renderDistance.tooltip")
		                        .defineInRange(Names.Config.RENDER_DISTANCE, 8, 2, 32);


		builder.pop().comment(Names.Config.Category.DEBUG).push(Names.Config.Category.DEBUG);

		dumpBlockList = builder.comment(Names.Config.DUMP_BLOCK_LIST_DESC)
		                       .translation("schematica.config.dumpBlockList.tooltip")
		                       .define(Names.Config.DUMP_BLOCK_LIST, false);

		showDebugInfo = builder.comment(Names.Config.SHOW_DEBUG_INFO_DESC)
		                       .translation("schematica.config.showDebugInfo.tooltip")
		                       .define(Names.Config.SHOW_DEBUG_INFO, true);


		builder.pop().comment(Names.Config.Category.GENERAL).push(Names.Config.Category.GENERAL);

		extraAirBlocks = builder.comment(Names.Config.EXTRA_AIR_BLOCKS_DESC)
		                        .translation("schematica.config.extraAirBlocks.tooltip")
		                        .defineList(Names.Config.EXTRA_AIR_BLOCKS, Collections.singletonList("minecraft:air"),
		                                    s -> s instanceof String && ForgeRegistries.BLOCKS.containsKey(
				                                    new ResourceLocation((String) s)));

		schematicDirectoryPath = builder.comment(Names.Config.SCHEMATIC_DIRECTORY_DESC)
		                                .translation("schematica.config.schematicDirectory.tooltip")
		                                .define(Names.Config.SCHEMATIC_DIRECTORY, SCHEMATIC_DEFAULT_FOLDER);

		sortType = builder.comment(Names.Config.SORT_TYPE_DESC)
		                  .translation("schematica.config.extraAirBlocks.tooltip")
		                  .defineEnum(Names.Config.SORT_TYPE, ItemStackSortType.SIZE_DESC);

		builder.pop().comment(Names.Config.Category.PRINTER).push(Names.Config.Category.PRINTER);

		destroyBlocks = builder.comment(Names.Config.DESTROY_BLOCKS_DESC)
		                       .translation("schematica.config.destroyBlocks.tooltip")
		                       .define(Names.Config.DESTROY_BLOCKS, false);

		destroyInstantly = builder.comment(Names.Config.DESTROY_INSTANTLY_DESC)
		                          .translation("schematica.config.destroyInstantly.tooltip")
		                          .define(Names.Config.DESTROY_INSTANTLY, false);

		placeAdjacent = builder.comment(Names.Config.PLACE_ADJACENT_DESC)
		                       .translation("schematica.config.placeAdjacent.tooltip")
		                       .define(Names.Config.PLACE_ADJACENT, true);

		placeDelay = builder.comment(Names.Config.PLACE_DELAY_DESC)
		                    .translation("schematica.config.placeDelay.tooltip")
		                    .defineInRange(Names.Config.PLACE_DELAY, 1, 1, Integer.MAX_VALUE);

		placeDistance = builder.comment(Names.Config.PLACE_DISTANCE_DESC)
		                       .translation("schematica.config.placeDistance.tooltip")
		                       .defineInRange(Names.Config.PLACE_DISTANCE, 5, 1, Integer.MAX_VALUE);

		placeInstantly = builder.comment(Names.Config.PLACE_INSTANTLY_DESC)
		                        .translation("schematica.config.placeInstantly.tooltip")
		                        .define(Names.Config.PLACE_INSTANTLY, false);

		timeout = builder.comment(Names.Config.TIMEOUT_DESC)
		                 .translation("schematica.config.timeout.tooltip")
		                 .defineInRange(Names.Config.TIMEOUT, 10, 2, Integer.MAX_VALUE);

		swapSlots = builder.comment(Names.Config.SWAP_SLOT_DESC)
		                   .translation("schematica.config.swapslots.tooltip")
		                   .defineList(Names.Config.SWAP_SLOT, Arrays.asList(5, 6, 7, 8),
		                               num -> num instanceof Integer && (Integer) num > 0 && (Integer) num < 9);
	}

	public static void normalizeSchematicPath() {
		try {
			schematicDirectory = schematicDirectory.getCanonicalFile();
			String schematicPath = schematicDirectory.getAbsolutePath();
			String dataPath = Reference.proxy.getDataDirectory().getAbsolutePath();
			String newSchematicPath = mergePaths(schematicPath, dataPath);

			Reference.logger.debug("Schematic path: {}", schematicPath);
			Reference.logger.debug("Data path: {}", dataPath);
			Reference.logger.debug("New schematic path: {}", newSchematicPath);
		} catch (IOException e) {
			Reference.logger.warn("Could not canonize path!", e);
		}
	}

	private static String mergePaths(String schematicPath, String dataPath) {
		String newPath;
		if (schematicPath.startsWith(dataPath)) {
			newPath = "." + schematicPath.substring(dataPath.length());
		} else {
			newPath = schematicPath;
		}

		return newPath.replace("\\", "/");
	}

	public static void populateExtraAirBlocks() {
		extraAirBlockList.clear();
		for (String name : SchematicaConfig.CLIENT.extraAirBlocks.get()) {
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
			if (block != Blocks.AIR) {
				extraAirBlockList.add(block);
			}
		}
	}

	public static boolean isExtraAirBlock(Block block) {
		return extraAirBlockList.contains(block);
	}
}

