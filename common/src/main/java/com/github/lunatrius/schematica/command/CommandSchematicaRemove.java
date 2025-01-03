package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileFilter;

public class CommandSchematicaRemove extends CommandSchematicaBase {
	private static final FileFilter FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(Names.Command.Remove.NAME)
		               .then(Commands.argument("name", StringArgumentType.string())
		                             .suggests(
				                             ((context, builder) -> CommandSchematicaBase.getSchematicNamesSuggestions(
						                             context, builder, FILE_FILTER_SCHEMATIC)))
		                             .executes(CommandSchematicaRemove::showDeleteConfirmation)
		                             .then(Commands.argument("hash", StringArgumentType.string())
		                                           .executes(CommandSchematicaRemove::delete)));
	}

	private static int delete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		String name = StringArgumentType.getString(context, "name");
		String actual = StringArgumentType.getString(context, "hash");
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();
		File file;

		try {
			file = getSchematicFile(player, name);
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.translatable(e.getMessage()));
			return -1;
		}

		if (actual.length() == 32) {
			String expected = DigestUtils.md5Hex(name);
			if (actual.equals(expected)) {
				if (file.delete()) {
					source.sendSuccess(
							() -> Component.translatable(Names.Command.Remove.Message.SCHEMATIC_REMOVED, name), true);
				} else {
					source.sendFailure(Component.translatable(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND));
					return -1;
				}
			}
		}
		return 0;
	}

	private static int showDeleteConfirmation(CommandContext<CommandSourceStack> commandContext)
			throws CommandSyntaxException {
		CommandSourceStack source = commandContext.getSource();
		ServerPlayer player = source.getPlayerOrException();
		String name = StringArgumentType.getString(commandContext, "name");
		File file;
		try {
			file = getSchematicFile(player, name);
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.translatable(e.getMessage()));
			return -1;
		}

		if (file.exists()) {
			String hash = DigestUtils.md5Hex(name);
			String confirmCommand =
					String.format("/%s %s %s", Reference.MOD_ID + " " + Names.Command.Remove.NAME, name, hash);
			Component chatComponent = Component.translatable(Names.Command.Remove.Message.ARE_YOU_SURE_START, name)
			                                   .append(withStyle(Component.literal("[")
			                                                              .append(Component.translatable(
					                                                              Names.Command.Remove.Message.YES))
			                                                              .append(Component.literal("]")),
			                                                     ChatFormatting.RED, confirmCommand));

			source.sendSystemMessage(chatComponent);
			return 0;
		} else {
			source.sendFailure(Component.translatable(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND));
			return -1;
		}
	}

	private static File getSchematicFile(Player player, String name) {
		File schematicDirectory = Reference.proxy.getPlayerSchematicDirectory(player, true);
		File file = new File(schematicDirectory, name);

		if (!FileUtils.contains(schematicDirectory, file)) {
			Reference.logger.error("{} has tried to download the file {}", player.getName(), name);
			throw new IllegalArgumentException(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND);
		}

		return file;
	}
}