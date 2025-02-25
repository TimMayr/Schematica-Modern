package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.command.client.CommandSchematicaList;
import com.github.lunatrius.schematica.reference.Names;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class CommandSchematicaBase {
	public static @NotNull MutableComponent withStyle(MutableComponent component, ChatFormatting formatting,
	                                                  @Nullable String command) {
		Style style = Style.EMPTY.applyFormat(formatting);

		if (command != null) {
			style = style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		}

		return component.copy().withStyle(style);
	}

	public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal(Names.Command.BASE)
				.then(CommandSchematicaDownload.register())
				.then(CommandSchematicaSave.register())
				.then(CommandSchematicaRemove.register()));
	}

	public static CompletableFuture<Suggestions> getSchematicNamesSuggestions(
			@NotNull CommandContext<CommandSourceStack> context, SuggestionsBuilder builder,
			FilePermission permission) {
		CommandSourceStack source = context.getSource();
		Player player;
		String name = "";
		try {
			name = StringArgumentType.getString(context, "name");
		} catch (IllegalArgumentException ignored) {
		}

		try {
			player = source.getPlayerOrException();
		} catch (CommandSyntaxException e) {
			return builder.buildFuture();
		}

		List<String> filenames = SchematicAccounter.sortedNames(player, permission);

		if (!filenames.isEmpty()) {
			//Copy so that the lambda can access it
			String finalName = name;
			filenames.stream().filter(s -> s.startsWith(finalName)).forEach(s -> builder.suggest("\"" + s + "\""));
		}

		return builder.buildFuture();
	}
}