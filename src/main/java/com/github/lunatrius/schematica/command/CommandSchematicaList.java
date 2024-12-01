package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal(Names.Command.List.NAME)
				.then(Commands.argument("page", IntegerArgumentType.integer(1)).executes((commandContext) -> {
					CommandSource source = commandContext.getSource();
					ServerPlayerEntity player = source.asPlayer();
					int page = IntegerArgumentType.getInteger(commandContext, "page");

					final int pageSize = 9; //maximum number of lines available without opening chat.
					final int pageStart = page * pageSize;
					final int pageEnd = pageStart + pageSize;
					int currentFile = 0;

					final LinkedList<ITextComponent> componentsToSend = new LinkedList<>();

					final File schematicDirectory = Reference.proxy.getPlayerSchematicDirectory(player, true);


					if (schematicDirectory == null) {
						Reference.logger.warn("Unable to determine the schematic directory for player {}", player);
						throw new CommandException(new TranslationTextComponent(
								Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
					}

					if (!schematicDirectory.exists()) {
						if (!schematicDirectory.mkdirs()) {
							Reference.logger.warn("Could not create player schematic directory {}",
									schematicDirectory.getAbsolutePath());
							throw new CommandException(new TranslationTextComponent(
									Names.Command.Save.Message.PLAYER_SCHEMATIC_DIR_UNAVAILABLE));
						}
					}

					final File[] files = schematicDirectory.listFiles(FILE_FILTER_SCHEMATIC);
					if (files != null) {
						for (final File path : files) {
							if (currentFile >= pageStart && currentFile < pageEnd) {
								final String fileName = path.getName();

								final ITextComponent chatComponent = new StringTextComponent(
										String.format("%2d (%s): %s [", currentFile + 1,
												FileUtils.humanReadableByteCount(path.length()),
												FilenameUtils.removeExtension(fileName)));

								final String removeCommand =
										String.format("/%s %s", Names.Command.Remove.NAME, fileName);
								final ITextComponent removeLink =
										withStyle(new TranslationTextComponent(Names.Command.List.Message.REMOVE),
												TextFormatting.RED, removeCommand);
								chatComponent.appendSibling(removeLink);
								chatComponent.appendText("][");

								final String downloadCommand =
										String.format("/%s %s", Names.Command.Download.NAME, fileName);
								final ITextComponent downloadLink =
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
						throw new CommandException(
								new TranslationTextComponent(Names.Command.List.Message.NO_SCHEMATICS));
					}

					final int totalPages = (currentFile - 1) / pageSize;
					if (page > totalPages) {
						throw new CommandException(
								new TranslationTextComponent(Names.Command.List.Message.NO_SUCH_PAGE));
					}

					source.sendFeedback(withStyle(
							new TranslationTextComponent(Names.Command.List.Message.PAGE_HEADER, page + 1,
									totalPages + 1), TextFormatting.DARK_GREEN, null), true);
					for (final ITextComponent chatComponent : componentsToSend) {
						source.sendFeedback(chatComponent, true);
					}

					return page;
				})));
	}

	@Override
	public String getUsage(final ICommandSource sender) {
		return Names.Command.List.Message.USAGE;
	}
}
