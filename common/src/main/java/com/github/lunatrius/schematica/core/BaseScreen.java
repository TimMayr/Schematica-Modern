package com.github.lunatrius.schematica.core;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class BaseScreen extends Screen {
	protected final Screen parent;

	public BaseScreen(Screen parent) {
		super(CommonComponents.EMPTY);
		this.parent = parent;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}

	@Override
	public void init() {
		this.children().clear();
	}

	public Screen getParent() {
		return parent;
	}
}