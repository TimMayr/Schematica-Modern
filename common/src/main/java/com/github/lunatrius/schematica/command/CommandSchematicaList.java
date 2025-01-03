package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;

public class CommandSchematicaList extends CommandSchematicaBase {
	private static final FileFilter FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(Names.Command.List.NAME)
		               .executes(CommandSchematicaList::printList)
		               .then(Commands.argument("page", IntegerArgumentType.integer(1))
		                             .executes(CommandSchematicaList::printList));
	}

	private static int printList(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandContext.getSource();
		ServerPlayer player = source.getPlayerOrException();
		int page = 0;

		try {
			page = IntegerArgumentType.getInteger(commandContext, "page") - 1;
		} catch (IllegalArgumentException ignored) {
		}

		int pageSize = 9; //maximum number of lines available without opening chat.
		int pageStart = page * pageSize;
		int pageEnd = pageStart + pageSize;
		int currentFile = 0;

		LinkedList<Component> componentsToSend = new LinkedList<>();

		File schematicDirectory = Reference.proxy.getPlayerSchematicDirectory(player, true);


		if (schematicDirectory == null) {
			Reference.logger.warn("Unable to determine the schematic directory for " + "player {}", player);
			source.sendFailure(Component.translatable(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
			return -1;
		}

		if (!schematicDirectory.exists()) {
			if (!schematicDirectory.mkdirs()) {
				Reference.logger.warn("Could not create player schematic directory {}",
				                      schematicDirectory.getAbsolutePath());

				source.sendFailure(Component.translatable(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
				return -1;
			}
		}

		File[] files = schematicDirectory.listFiles(FILE_FILTER_SCHEMATIC);
		if (files != null) {
			for (File path : files) {
				if (currentFile >= pageStart && currentFile < pageEnd) {
					String fileName = path.getName();

					Component chatComponent = Component.literal(String.format("%2d (%s): %s [", currentFile + 1,
					                                                          FileUtils.humanReadableByteCount(
							                                                          path.length()),
					                                                          FilenameUtils.removeExtension(fileName)));

					String removeCommand =
							String.format("/%s %s", Reference.MOD_ID + " " + Names.Command.Remove.NAME, fileName);
					Component removeLink =
							withStyle(Component.translatable(Names.Command.List.Message.REMOVE), ChatFormatting.RED,
							          removeCommand);
					chatComponent = chatComponent.copy().append(removeLink).append("][");

					String downloadCommand =
							String.format("/%s %s", Reference.MOD_ID + " " + Names.Command.Download.NAME, fileName);
					Component downloadLink =
							withStyle(Component.translatable(Names.Command.List.Message.DOWNLOAD),
							          ChatFormatting.GREEN,
							          downloadCommand);
					chatComponent = chatComponent.copy().append(downloadLink).append("]");

					componentsToSend.add(chatComponent);
				}
				++currentFile;
			}
		}

		if (currentFile == 0) {
			source.sendFailure(Component.translatable(Names.Command.List.Message.NO_SCHEMATICS));
			return -1;
		}

		int totalPages = (currentFile - 1) / pageSize;
		if (page > totalPages) {
			source.sendFailure(Component.translatable(Names.Command.List.Message.NO_SUCH_PAGE));
			return 1;
		}

		source.sendSystemMessage(
				withStyle(Component.translatable(Names.Command.List.Message.PAGE_HEADER, page + 1, totalPages + 1),
				          ChatFormatting.DARK_GREEN, null));
		for (Component chatComponent : componentsToSend) {
			source.sendSystemMessage(chatComponent);
		}

		return page;
	}
}