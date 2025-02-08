package com.github.lunatrius.schematica.core;

public enum Glyph {
	ASC('↑', "ascending"),
	DESC('↓', "descending");

	public final char glyph;
	public final String label;

	Glyph(char glyph, String label) {
		this.glyph = glyph;
		this.label = label;
	}

	@Override
	public String toString() {
		return Character.toString(glyph);
	}
}
