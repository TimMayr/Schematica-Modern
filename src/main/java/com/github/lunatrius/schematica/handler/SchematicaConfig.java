package com.github.lunatrius.schematica.handler;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber()
public class SchematicaConfig {
	public static final ForgeConfigSpec serverSpec;
	public static final SchematicaServerConfig SERVER;
	public static final ForgeConfigSpec clientSpec;
	public static final SchematicaClientConfig CLIENT;

	static {
		Pair<SchematicaServerConfig, ForgeConfigSpec> specPair =
				new ForgeConfigSpec.Builder().configure(SchematicaServerConfig::new);
		serverSpec = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	static {
		Pair<SchematicaClientConfig, ForgeConfigSpec> specPair =
				new ForgeConfigSpec.Builder().configure(SchematicaClientConfig::new);
		clientSpec = specPair.getRight();
		CLIENT = specPair.getLeft();
	}
}
