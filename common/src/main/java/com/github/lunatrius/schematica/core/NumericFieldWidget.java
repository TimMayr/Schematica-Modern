package com.github.lunatrius.schematica.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class NumericFieldWidget extends WidgetGroup {
	private static final int DEFAULT_VALUE = 0;
	private static final int BUTTON_WIDTH = 12;

	private final NumericEditBox numericEditBox;
	private final Button buttonDec;
	private final Button buttonInc;
	private int minimum = Integer.MIN_VALUE;
	private int maximum = Integer.MAX_VALUE;
	private int value = DEFAULT_VALUE;

	public NumericFieldWidget(int x, int y, OnPress onPress) {
		this(x, y, 100, 20, onPress);
	}

	public NumericFieldWidget(int x, int y, int width, int height, OnPress onPress) {
		super(0, 0, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
		this.numericEditBox =
				new NumericEditBox(Minecraft.getInstance().font, x, y, width - BUTTON_WIDTH * 2 - 2, height);

		this.buttonDec =
				new Button.Builder(Component.literal("-"), (event) -> this.decrement())
						.bounds(x + width - BUTTON_WIDTH * 2, y, BUTTON_WIDTH, height).build();

		this.buttonInc =
				new Button.Builder(Component.literal("+"), (event) -> this.increment())
						.bounds(x + width - BUTTON_WIDTH, y, BUTTON_WIDTH, height).build();

		setValue(DEFAULT_VALUE);
		this.children.add(numericEditBox);
		this.children.add(buttonDec);
		this.children.add(buttonInc);
	}

	public NumericFieldWidget(int x, int y, int width, OnPress onPress) {
		this(x, y, width, 20, onPress);
	}

	private void increment() {
		this.setValue(this.getValue() + 1);
	}

	private void decrement() {
		this.setValue(this.getValue() - 1);
	}

	public boolean isFocused() {
		return this.numericEditBox.isFocused();
	}

	@Override
	public void setFocused(boolean focused) {
		this.numericEditBox.setFocused(focused);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		boolean toReturn = this.numericEditBox.charTyped(codePoint, modifiers);
		this.value = Integer.parseInt(this.numericEditBox.getValue());
		return toReturn;
	}

	public int getValue() {
		return this.value;
	}

	public void setValue(int value) {
		if (value <= this.getMaximum() && value >= this.getMinimum()) {
			this.value = value;
			this.numericEditBox.setValue(String.valueOf(value));
		}
	}

	public int getMaximum() {
		return this.maximum;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public int getMinimum() {
		return this.minimum;
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

	public void setPosition(int x, int y) {
		this.numericEditBox.setPosition(x, y);
		this.buttonInc.setPosition(x + width - BUTTON_WIDTH * 2, y);
		this.buttonDec.setPosition(x + width - BUTTON_WIDTH, y);
	}
}