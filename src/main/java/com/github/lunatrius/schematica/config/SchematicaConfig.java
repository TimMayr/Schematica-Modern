package com.github.lunatrius.schematica.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SchematicaConfig {
	public static final ForgeConfigSpec serverSpec;
	public static final SchematicaServerConfig SERVER;
	public static final ForgeConfigSpec clientSpec;
	public static final SchematicaClientConfig CLIENT;

	static {
		Pair<SchematicaServerConfig, ForgeConfigSpec> serverSpecPair =
				new ForgeConfigSpec.Builder().configure(SchematicaServerConfig::new);
		serverSpec = serverSpecPair.getRight();
		SERVER = serverSpecPair.getLeft();

		Pair<SchematicaClientConfig, ForgeConfigSpec> clientSpecPair =
				new ForgeConfigSpec.Builder().configure(SchematicaClientConfig::new);
		clientSpec = clientSpecPair.getRight();
		CLIENT = clientSpecPair.getLeft();
	}
}