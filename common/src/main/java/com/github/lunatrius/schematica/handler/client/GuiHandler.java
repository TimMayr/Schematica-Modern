package com.github.lunatrius.schematica.handler.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;

public class GuiHandler {
	public static final GuiHandler INSTANCE = new GuiHandler();

	private GuiHandler() {
		ClientTickEvent.CLIENT_POST.register(instance -> {
			Screen screen = instance.screen;
			if (screen instanceof AbstractSignEditScreen) {
				instance.setScreen(null);
			}
		});
	}
}