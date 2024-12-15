package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.LinkedList;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaList extends CommandSchematicaBase {
	private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal(Names.Command.List.NAME)
		               .executes(CommandSchematicaList::printList)
		               .then(Commands.argument("page", IntegerArgumentType.integer(1))
		                             .executes(CommandSchematicaList::printList));
	}

	private static int printList(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
		CommandSource source = commandContext.getSource();
		ServerPlayerEntity player = source.asPlayer();
		int page = 0;

		try {
			page = IntegerArgumentType.getInteger(commandContext, "page") - 1;
		} catch (IllegalArgumentException ignored) {}

		int pageSize = 9; //maximum number of lines available without opening chat.
		int pageStart = page * pageSize;
		int pageEnd = pageStart + pageSize;
		int currentFile = 0;

		LinkedList<ITextComponent> componentsToSend = new LinkedList<>();

		File schematicDirectory = Reference.proxy.getPlayerSchematicDirectory(player, true);


		if (schematicDirectory == null) {
			Reference.logger.warn("Unable to determine the schematic directory for " + "player {}", player);
			throw new CommandException(
					new TranslationTextComponent(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
		}

		if (!schematicDirectory.exists()) {
			if (!schematicDirectory.mkdirs()) {
				Reference.logger.warn("Could not create player schematic directory {}",
				                      schematicDirectory.getAbsolutePath());
				throw new CommandException(
						new TranslationTextComponent(Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
			}
		}

		File[] files = schematicDirectory.listFiles(FILE_FILTER_SCHEMATIC);
		if (files != null) {
			for (File path : files) {
				if (currentFile >= pageStart && currentFile < pageEnd) {
					String fileName = path.getName();

					ITextComponent chatComponent = new StringTextComponent(
							String.format("%2d (%s): %s [", currentFile + 1,
							              FileUtils.humanReadableByteCount(path.length()),
							              FilenameUtils.removeExtension(fileName)));

					String removeCommand =
							String.format("/%s %s", Reference.MODID + " " + Names.Command.Remove.NAME, fileName);
					ITextComponent removeLink =
							withStyle(new TranslationTextComponent(Names.Command.List.Message.REMOVE),
							          TextFormatting.RED, removeCommand);
					chatComponent.appendSibling(removeLink);
					chatComponent.appendText("][");

					String downloadCommand =
							String.format("/%s %s", Reference.MODID + " " + Names.Command.Download.NAME, fileName);
					ITextComponent downloadLink =
							withStyle(new TranslationTextComponent(Names.Command.List.Message.DOWNLOAD),
							          TextFormatting.GREEN, downloadCommand);
					chatComponent.appendSibling(downloadLink);
					chatComponent.appendText("]");

					componentsToSend.add(chatComponent);
				}
				++currentFile;
			}
		}

		if (currentFile == 0) {
			throw new CommandException(new TranslationTextComponent(Names.Command.List.Message.NO_SCHEMATICS));
		}

		int totalPages = (currentFile - 1) / pageSize;
		if (page > totalPages) {
			throw new CommandException(new TranslationTextComponent(Names.Command.List.Message.NO_SUCH_PAGE));
		}

		source.sendFeedback(
				withStyle(new TranslationTextComponent(Names.Command.List.Message.PAGE_HEADER, page, totalPages),
				          TextFormatting.DARK_GREEN, null), true);
		for (ITextComponent chatComponent : componentsToSend) {
			source.sendFeedback(chatComponent, true);
		}

		return page;
	}

	@Override
	public String getUsage(ICommandSource sender) {
		return Names.Command.List.Message.USAGE;
	}
}