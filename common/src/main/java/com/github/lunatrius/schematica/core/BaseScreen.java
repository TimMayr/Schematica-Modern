package com.github.lunatrius.schematica.core;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class BaseScreen extends Screen {
	protected final Screen parentScreen;

	public BaseScreen(Screen parentScreen) {
		super(CommonComponents.EMPTY);
		this.parentScreen = parentScreen;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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