package com.github.lunatrius.schematica.handler.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;

@Environment(EnvType.CLIENT)
public class GuiHandler {
	public static GuiHandler INSTANCE;

	private GuiHandler() {
		ClientTickEvent.CLIENT_POST.register(instance -> {
			Screen screen = instance.screen;
			if (screen instanceof AbstractSignEditScreen) {
				instance.setScreen(null);
			}
		});
	}

	public static void init() {
		GuiHandler.INSTANCE = new GuiHandler();
	}
}