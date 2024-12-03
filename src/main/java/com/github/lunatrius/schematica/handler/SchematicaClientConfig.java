package com.github.lunatrius.schematica.handler;

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
	private static final Set<Block> extraAirBlockList = new HashSet<>();
	public static Queue<Integer> swapSlotsQueue = new ArrayDeque<>();
	public static final String SCHEMATIC_DEFAULT_FOLDER = "./schematics";
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

	SchematicaClientConfig(ForgeConfigSpec.Builder builder) {
		builder.comment(Names.Config.Category.RENDER).push(Names.Config.Category.RENDER);

		alpha = builder.comment(Names.Config.ALPHA_DESC)
		               .translation("schematica.config.alpha.tooltip")
		               .defineInRange(Names.Config.ALPHA, 1.0f, 0, 1);

		alphaEnabled = builder.comment(Names.Config.ALPHA_ENABLED_DESC)
		                      .translation("schematica.config.alphaEnabled.tooltip")
		                      .define(Names.Config.SHOW_DEBUG_INFO, false);

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


		builder.comment(Names.Config.Category.DEBUG).push(Names.Config.Category.DEBUG);

		dumpBlockList = builder.comment(Names.Config.DUMP_BLOCK_LIST_DESC)
		                       .translation("schematica.config.dumpBlockList.tooltip")
		                       .define(Names.Config.DUMP_BLOCK_LIST, false);

		showDebugInfo = builder.comment(Names.Config.SHOW_DEBUG_INFO_DESC)
		                       .translation("schematica.config.showDebugInfo.tooltip")
		                       .define(Names.Config.SHOW_DEBUG_INFO, true);


		builder.comment(Names.Config.Category.GENERAL).push(Names.Config.Category.GENERAL);

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

