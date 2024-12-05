package com.github.lunatrius.schematica.config;

import com.github.lunatrius.schematica.reference.Names;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class SchematicaServerConfig {
	public final ForgeConfigSpec.BooleanValue loadEnabled;
	public final ForgeConfigSpec.IntValue playerQuotaKilobytes;
	public final ForgeConfigSpec.BooleanValue printerEnabled;
	public final ForgeConfigSpec.BooleanValue saveEnabled;

	public final ForgeConfigSpec.BooleanValue destroyBlocks;
	public final ForgeConfigSpec.BooleanValue destroyInstantly;
	public final ForgeConfigSpec.BooleanValue placeAdjacent;
	public final ForgeConfigSpec.IntValue placeDelay;
	public final ForgeConfigSpec.IntValue placeDistance;
	public final ForgeConfigSpec.BooleanValue placeInstantly;
	public final ForgeConfigSpec.IntValue timeout;
	public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> swapSlots;

	SchematicaServerConfig(ForgeConfigSpec.Builder builder) {
		builder.comment(Names.Config.Category.PRINTER).push(Names.Config.Category.PRINTER);

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


		builder.comment(Names.Config.Category.SERVER).push(Names.Config.Category.SERVER);

		loadEnabled = builder.comment(Names.Config.LOAD_ENABLED_DESC)
		                     .translation("schematica.config.loadEnabled.tooltip")
		                     .define(Names.Config.LOAD_ENABLED, true);

		saveEnabled = builder.comment(Names.Config.SAVE_ENABLED_DESC)
		                     .translation("schematica.config.saveEnabled.tooltip")
		                     .define(Names.Config.SAVE_ENABLED, true);

		printerEnabled = builder.comment(Names.Config.PRINTER_ENABLED_DESC)
		                        .translation("schematica.config.printerEnabled.tooltip")
		                        .define(Names.Config.PRINTER_ENABLED, true);

		playerQuotaKilobytes = builder.comment(Names.Config.PLAYER_QUOTA_KILOBYTES_DESC)
		                              .translation("schematica.config.playerQuotaKilobytes.tooltip")
		                              .defineInRange(Names.Config.PLAYER_QUOTA_KILOBYTES, 8192, 0, Integer.MAX_VALUE);
	}

}
