package com.github.lunatrius.schematica.client.gui.core;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class LimitedEditBox extends EditBox {
	private final Set<Character> legalChars;

	public LimitedEditBox(Font font, int x, int y, int width, int height, Component message,
	                      @NotNull Set<Character> legalChars) {
		super(font, x, y, width, height, message);
		this.legalChars = Set.copyOf(legalChars);
	}

	public LimitedEditBox(Font font, int width, int height, Component message, @NotNull Set<Character> legalChars) {
		super(font, width, height, message);
		this.legalChars = Set.copyOf(legalChars);
	}

	public LimitedEditBox(Font font, int x, int y, int width, int height, Component message,
	                      @NotNull Character[] legalChars) {
		super(font, x, y, width, height, message);
		this.legalChars = CollectionUtils.unmodifiableSetFromArray(legalChars);
	}

	public LimitedEditBox(Font font, int width, int height, Component message, @NotNull Character[] legalChars) {
		super(font, width, height, message);
		this.legalChars = CollectionUtils.unmodifiableSetFromArray(legalChars);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (!legalChars.contains(codePoint)) {
			return false;
		}

		return super.charTyped(codePoint, modifiers);
	}

	public Set<Character> getLegalChars() {
		return legalChars;
	}
}
