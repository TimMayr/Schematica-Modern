package com.github.lunatrius.schematica.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class SchematicaConfig {
	public static ForgeConfigSpec serverSpec;
	public static SchematicaServerConfig SERVER;
	public static ForgeConfigSpec clientSpec;
	public static SchematicaClientConfig CLIENT;

	public static void init() {
		Pair<SchematicaServerConfig, ForgeConfigSpec> serverSpecPair =
				new ForgeConfigSpec.Builder().configure(SchematicaServerConfig::new);
		serverSpec = serverSpecPair.getRight();
		SERVER = serverSpecPair.getLeft();

		Pair<SchematicaClientConfig, ForgeConfigSpec> clientSpecPair =
				new ForgeConfigSpec.Builder().configure(SchematicaClientConfig::new);
		clientSpec = clientSpecPair.getRight();
		CLIENT = clientSpecPair.getLeft();

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
	}
}
