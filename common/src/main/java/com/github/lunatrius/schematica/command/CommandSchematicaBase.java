package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.command.client.CommandSchematicaReplace;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

	public static CompletableFuture<Suggestions> getSchematicNamesSuggestions(CommandContext<CommandSource> context,
	                                                                          SuggestionsBuilder builder,
	                                                                          FileFilterSchematic FILE_FILTER_SCHEMATIC) {
		CommandSource source = context.getSource();
		PlayerEntity player;
		String name = "";
		try {
			name = StringArgumentType.getString(context, "name");
		} catch (IllegalArgumentException ignored) {}

		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			return builder.buildFuture();
		}

		File directory = Reference.proxy.getPlayerSchematicDirectory(player, true);
		File[] files = directory.listFiles(FILE_FILTER_SCHEMATIC);

		if (files != null) {
			List<String> filenames = new ArrayList<>();

			for (File file : files) {
				filenames.add(file.getName());
			}

			String finalName = name;
			filenames.stream().filter(s -> s.startsWith(finalName)).forEach(builder::suggest);
			return builder.buildFuture();
		}

		return builder.buildFuture();
	}

	public abstract String getUsage(ICommandSource sender);
}