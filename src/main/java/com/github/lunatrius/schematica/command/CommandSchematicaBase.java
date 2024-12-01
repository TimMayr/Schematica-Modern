package com.github.lunatrius.schematica.command;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CommandSchematicaBase {
	public static List<String> getListOfStringsMatchingLastWord(String[] args, List<String> possibilities) {
		String lastWord = args[args.length - 1];
		List<String> matches = new ArrayList<>();

		for (String possibility : possibilities) {
			if (possibility.startsWith(lastWord)) {
				matches.add(possibility);
			}
		}

		return matches;
	}

	protected static <T extends ITextComponent> T withStyle(final T component, final TextFormatting formatting,
	                                                        @Nullable final String command) {
		final Style style = new Style();
		style.setColor(formatting);

		if (command != null) {
			style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		}

		component.setStyle(style);

		return component;
	}

	public abstract String getUsage(final ICommandSource sender);
}
