package com.github.lunatrius.schematica.config;

import com.github.lunatrius.schematica.reference.Names;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SchematicaServerConfig {
	public final ModConfigSpec.BooleanValue loadEnabled;
	public final ModConfigSpec.IntValue playerQuotaKilobytes;
	public final ModConfigSpec.BooleanValue printerEnabled;
	public final ModConfigSpec.BooleanValue saveEnabled;

	SchematicaServerConfig(ModConfigSpec.Builder builder) {
		builder.push(Names.Config.Category.SERVER);

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