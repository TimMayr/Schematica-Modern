package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.command.client.CommandSchematicaReplace;
import com.mojang.brigadier.CommandDispatcher;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
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

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("schematica")
		                            .then(CommandSchematicaDownload.register())
		                            .then(CommandSchematicaList.register())
		                            .then(CommandSchematicaSave.register())
		                            .then(CommandSchematicaRemove.register())
		                            .then(CommandSchematicaReplace.register()));
	}

	public abstract String getUsage(ICommandSource sender);
}
