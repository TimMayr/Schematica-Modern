package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.core.FileNameUtils;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class CommandSchematicaRemove extends CommandSchematicaBase {
	private static final FileFilter FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(Names.Command.Remove.NAME)
				.then(Commands.argument("name", StringArgumentType.string())
						.suggests(
								((context, builder) -> CommandSchematicaBase.getSchematicNamesSuggestions(
										context, builder, FILE_FILTER_SCHEMATIC)))
						.executes(CommandSchematicaRemove::showDeleteConfirmation)
						.then(Commands.argument("confirm", BoolArgumentType.bool())
								.executes(CommandSchematicaRemove::delete)));
	}

	private static int delete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		String name = StringArgumentType.getString(context, "name");
		boolean confirm = BoolArgumentType.getBool(context, "confirm");
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();
		File file;

		try {
			file = getSchematicFile(player, name);
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.literal(e.getMessage()));
			return -1;
		}

		if (confirm) {
			if (file.delete()) {
				source.sendSuccess(
						() -> Component.translatable(Names.Command.Remove.Message.SCHEMATIC_REMOVED, name), true);
				return 0;
			} else {
				source.sendFailure(Component.translatable(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND, name));
				return -1;
			}
		} else {
			return showDeleteConfirmation(context);
		}
	}

	private static @NotNull File getSchematicFile(Player player, String name) {
		List<File> schematics = Reference.proxy.getAllAccessibleSchematics(player, FILE_FILTER_SCHEMATIC);
		File toReturn = FileNameUtils.getFileByName(schematics, name);

		if (toReturn == null) {
			Reference.logger.error("{} has tried to delete the file {}, but it does not exist", player.getName(),
					name);
			throw new IllegalArgumentException(Component.translatable(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND
					, name).getString());
		}

		if (!toReturn.getParentFile().equals(Reference.proxy.getPlayerSchematicDirectory(player, true)) &&
				!toReturn.getParentFile().equals(Reference.proxy.getPlayerSchematicDirectory(player, false))) {
			Reference.logger.error("{} has tried to delete the file {}, but is not granted access", player.getName(),
					name);
			throw new IllegalArgumentException(Component.translatable(
					Names.Command.Remove.Message.SCHEMATIC_NOT_ACCESSIBLE, name).getString());
		}

		return toReturn;
	}

	private static int showDeleteConfirmation(@NotNull CommandContext<CommandSourceStack> commandContext)
			throws CommandSyntaxException {
		CommandSourceStack source = commandContext.getSource();
		ServerPlayer player = source.getPlayerOrException();
		String name = StringArgumentType.getString(commandContext, "name");
		File file;
		try {
			file = getSchematicFile(player, name);
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.literal(e.getMessage()));
			return -1;
		}

		if (file.exists()) {
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
}