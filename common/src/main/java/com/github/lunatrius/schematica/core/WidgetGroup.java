package com.github.lunatrius.schematica.core;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
public abstract class WidgetGroup extends Button {
	protected final Set<AbstractWidget> children = new HashSet<>();

	protected WidgetGroup(int x, int y, int width, int height, Component message, OnPress onPress,
	                      CreateNarration createNarration) {
		super(x, y, width, height, message, onPress, createNarration);
	}

	@Override
	public int getHeight() {
		int maxY = 0;
		int minY = 0;

		for (AbstractWidget child : this.children) {
			maxY = Math.max(child.getY() + child.getHeight(), maxY);
			minY = Math.min(child.getY(), minY);
		}

		return maxY - minY;
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		for (AbstractWidget child : this.children) {
			child.onRelease(mouseX, mouseY);
		}

		super.onRelease(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.mouseClicked(mouseX, mouseY, button)) {
				toReturn = true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button) || toReturn;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.mouseReleased(mouseX, mouseY, button)) {
				toReturn = true;
			}
		}

		return super.mouseReleased(mouseX, mouseY, button) || toReturn;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
				toReturn = true;
			}
		}

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY) || toReturn;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.isMouseOver(mouseX, mouseY)) {
				toReturn = true;
			}
		}

		return super.isMouseOver(mouseX, mouseY) || toReturn;
	}

	@Override
	public int getWidth() {
		int maxX = 0;
		int minX = 0;

		for (AbstractWidget child : this.children) {
			maxX = Math.max(child.getX() + child.getWidth(), maxX);
			minX = Math.min(child.getX(), minX);
		}

		return maxX - minX;
	}

	@Override
	public void setAlpha(float alpha) {
		for (AbstractWidget child : this.children) {
			child.setAlpha(alpha);
		}

		super.setAlpha(alpha);
	}

	@Override
	public Component getMessage() {
		MutableComponent component = Component.empty();
		for (AbstractWidget child : this.children) {
			component.append(child.getMessage());
		}

		component.append(super.getMessage());
		return component;
	}

	@Override
	public boolean isFocused() {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.isFocused()) {
				toReturn = true;
			}
		}

		return super.isFocused() || toReturn;
	}

	@Override
	public boolean isHovered() {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.isHovered()) {
				toReturn = true;
			}
		}

		return super.isHovered() || toReturn;
	}

	@Override
	public boolean isHoveredOrFocused() {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.isHoveredOrFocused()) {
				toReturn = true;
			}
		}

		return super.isHoveredOrFocused() || toReturn;
	}

	@Override
	public boolean isActive() {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.isActive()) {
				toReturn = true;
			}
		}

		return super.isActive() || toReturn;
	}

	public void setActive(boolean active) {
		for (AbstractWidget child : this.children) {
			child.active = active;
		}

		this.active = active;
	}

	@Override
	public void setFocused(boolean focused) {
		for (AbstractWidget child : this.children) {
			child.setFocused(focused);
		}

		super.setFocused(focused);
	}

	@Override
	public int getX() {
		int minX = 0;

		for (AbstractWidget child : this.children) {
			minX = Math.min(child.getX(), minX);
		}

		return minX;
	}

	@Override
	public int getY() {
		int minY = 0;

		for (AbstractWidget child : this.children) {
			minY = Math.min(child.getY(), minY);
		}

		return minY;
	}

	@Override
	public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
		for (AbstractWidget child : this.children) {
			child.visitWidgets(consumer);
		}

		super.visitWidgets(consumer);
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		MutableComponent component = Component.empty();
		for (AbstractWidget child : this.children) {
			component.append(wrapDefaultNarrationMessage(child.getMessage()));
		}

		component.append(super.createNarrationMessage());
		return component;
	}

	@Override
	protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			for (AbstractWidget child : this.children) {
				child.render(guiGraphics, mouseX, mouseY, partialTicks);
			}
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		for (AbstractWidget child : this.children) {
			child.onClick(mouseX, mouseY);
		}

		super.onClick(mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.keyPressed(keyCode, scanCode, modifiers)) {
				toReturn = true;
			}
		}

		return super.keyPressed(keyCode, scanCode, modifiers) || toReturn;
	}

	public void setVisible(boolean visible) {
		for (AbstractWidget child : this.children) {
			child.visible = visible;
		}

		this.visible = visible;
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for (AbstractWidget child : this.children) {
			child.onRelease(mouseX, mouseY);
		}

		super.onRelease(mouseX, mouseY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
				toReturn = true;
			}
		}

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || toReturn;
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.keyReleased(keyCode, scanCode, modifiers)) {
				toReturn = true;
			}
		}

		return super.keyReleased(keyCode, scanCode, modifiers) || toReturn;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		boolean toReturn = false;

		for (AbstractWidget child : this.children) {
			if (child.charTyped(codePoint, modifiers)) {
				toReturn = true;
			}
		}

		return super.charTyped(codePoint, modifiers) || toReturn;
	}
}