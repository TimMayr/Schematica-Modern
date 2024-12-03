package com.github.lunatrius.schematica.command;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CommandSchematicaBase {
	protected static <T extends ITextComponent> T withStyle(T component, TextFormatting formatting,
	                                                        @Nullable String command) {
		Style style = new Style();
		style.setColor(formatting);

		if (command != null) {
			style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		}

		component.setStyle(style);

		return component;
	}

	public abstract String getUsage(ICommandSource sender);
}
