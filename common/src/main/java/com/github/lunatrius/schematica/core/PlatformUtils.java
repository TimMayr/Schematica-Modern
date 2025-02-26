package com.github.lunatrius.schematica.core;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

public class PlatformUtils {
	public static boolean isPlatformServer() {
		return Platform.getEnv() == EnvType.SERVER;
	}

	public static boolean isPlatformClient() {
		return Platform.getEnv() == EnvType.CLIENT;
	}
}
