package com.github.lunatrius.schematica.core;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class PlayerUtils {
	public static @Nullable Player getClientPlayer() {
		if (Platform.getEnv() == EnvType.CLIENT) {
			try {
				return Minecraft.getInstance().player;
			} catch (Exception e) {
				throw new RuntimeException("Failed to get client player", e);
			}
		}
		return null;
	}
}