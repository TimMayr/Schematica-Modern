package com.github.lunatrius.schematica.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SchematicaConfig {
	public static final ModConfigSpec serverSpec;
	public static final SchematicaServerConfig SERVER;
	public static final ModConfigSpec clientSpec;
	public static final SchematicaClientConfig CLIENT;

	static {
		Pair<SchematicaServerConfig, ModConfigSpec> serverSpecPair =
				new ModConfigSpec.Builder().configure(SchematicaServerConfig::new);
		serverSpec = serverSpecPair.getRight();
		SERVER = serverSpecPair.getLeft();

		Pair<SchematicaClientConfig, ModConfigSpec> clientSpecPair =
				new ModConfigSpec.Builder().configure(SchematicaClientConfig::new);
		clientSpec = clientSpecPair.getRight();
		CLIENT = clientSpecPair.getLeft();
	}
}