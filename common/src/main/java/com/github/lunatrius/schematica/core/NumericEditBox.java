package com.github.lunatrius.schematica.core;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.CommonComponents;

public class NumericEditBox extends LimitedEditBox {
	public static final Character[] LEGAL_NUMERIC_CHARS = {'-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	public NumericEditBox(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height, CommonComponents.EMPTY, LEGAL_NUMERIC_CHARS);
	}

	public NumericEditBox(Font font, int width, int height) {
		super(font, width, height, CommonComponents.EMPTY, LEGAL_NUMERIC_CHARS);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (codePoint == '-' && this.getCursorPosition() != 0) {
			return false;
		}

		return super.charTyped(codePoint, modifiers);
	}
}
