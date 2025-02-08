package com.github.lunatrius.schematica.client.gui.core;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class BaseScreen extends Screen {
	protected final Screen parentScreen;

	public BaseScreen(Screen parentScreen) {
		super(CommonComponents.EMPTY);
		this.parentScreen = parentScreen;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(parentScreen);
	}

	@Override
	public void init() {
		this.children().clear();
	}
}