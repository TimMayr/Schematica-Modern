package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.accounting.SchematicLocation;
import com.github.lunatrius.schematica.network.message.commands.MessageDeleteSchematic;
import com.github.lunatrius.schematica.proxy.CommonProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import commonnetwork.api.Dispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CommandSchematicaRemove extends CommandSchematicaBase {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(Names.Command.Remove.NAME)
				.then(Commands.argument("name", StringArgumentType.string())
						.suggests(
								((context, builder) ->
										CommandSchematicaBase.getSchematicNamesSuggestions(
												context.getSource().getPlayer(),
												StringArgumentType.getString(context, "name"),
												builder,
												FilePermission.DELETE)))
						.executes(CommandSchematicaRemove::showDeleteConfirmation)
						.then(Commands.argument("confirm", BoolArgumentType.bool())
								.executes(CommandSchematicaRemove::delete)));
	}

	private static int showDeleteConfirmation(@NotNull CommandContext<CommandSourceStack> commandContext)
			throws CommandSyntaxException {
		CommandSourceStack source = commandContext.getSource();
		ServerPlayer player = source.getPlayerOrException();
		String name = StringArgumentType.getString(commandContext, "name");

		Map<String, SchematicHolder> schematics = SchematicAccounter.sortedSchematics(player, FilePermission.DELETE);
		SchematicHolder holder = schematics.get(name);

		if (holder != null) {
			String confirmCommand =
					String.format("/%s %s \"%s\" %b", Names.Command.BASE, Names.Command.Remove.NAME, name, true);
			Component chatComponent = Component.translatable(Names.Command.Remove.Message.ARE_YOU_SURE, name)
					.append(Component.literal(" "))
					.append(withStyle(ComponentUtils.wrapInSquareBrackets(
									Component.translatable(Names.Command.Remove.Message.YES)),
							ChatFormatting.RED, confirmCommand));

			source.sendSystemMessage(chatComponent);
			return 0;
		} else {
			source.sendFailure(Component.translatable(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND, name));
			return -1;
		}
	}

	private static int delete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		String name = StringArgumentType.getString(context, "name");
		boolean confirm = BoolArgumentType.getBool(context, "confirm");
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();
		Map<String, SchematicHolder> schematics = SchematicAccounter.sortedSchematics(player, FilePermission.DELETE);
		SchematicHolder holder = schematics.get(name);

		if (holder == null) {
			source.sendFailure(Component.translatable(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND, name));
			return -1;
		}

		if (confirm) {
			if (holder.location() == SchematicLocation.LOCAL) {
				Path file = Reference.proxy.resolveSchematic(holder.metadata());
				try {
					CommonProxy.recentlyRemoved.add(file);
					SchematicAccounter.removeSchematic(holder.metadata().id(), false);
					Files.delete(file);
					CommonProxy.scheduler.schedule(() -> CommonProxy.recentlyRemoved.remove(file), 200,
							TimeUnit.MILLISECONDS);
					source.sendSuccess(
							() -> Component.translatable(Names.Command.Remove.Message.SCHEMATIC_REMOVED, name), true);
					return 0;
				} catch (IOException e) {
					source.sendFailure(Component.translatable(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND, name));
					return -1;
				}
			} else {
				MessageDeleteSchematic message = new MessageDeleteSchematic(holder.metadata().id());
				Dispatcher.sendToClient(message, player);
				return 0;
			}
		} else {
			return showDeleteConfirmation(context);
		}
	}
}