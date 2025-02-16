package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
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
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class CommandSchematicaList extends CommandSchematicaBase {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(Names.Command.List.NAME)
				.executes(CommandSchematicaList::printList)
				.then(Commands.argument("page", IntegerArgumentType.integer(1))
						.executes(CommandSchematicaList::printList));
	}

	private static int printList(@NotNull CommandContext<CommandSourceStack> commandContext)
			throws CommandSyntaxException {
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

		List<SchematicHolder> schematics = SchematicAccounter.sorted(player);

		for (SchematicHolder entry : schematics) {
			if (currentFile >= pageStart && currentFile < pageEnd) {
				String fileName = entry.getName();

				Component chatComponent = Component.literal(String.format("%2d (%s): %s [", currentFile + 1,
						FileUtils.humanReadableByteCount(entry.getFileSize()),
						FilenameUtils.removeExtension(fileName)));

				String removeCommand = String.format("/%s %s \"%s\"", Reference.MOD_ID, Names.Command.Remove.NAME,
						fileName);
				Component removeLink = withStyle(
						Component.translatable(Names.Command.List.Message.REMOVE),
						ChatFormatting.RED,
						removeCommand);
				chatComponent = chatComponent.copy().append(removeLink).append("][");

				String downloadCommand = String.format("/%s %s \"%s\"", Reference.MOD_ID, Names.Command.Download.NAME,
						fileName);
				Component downloadLink = withStyle(
						Component.translatable(Names.Command.List.Message.DOWNLOAD),
						ChatFormatting.GREEN,
						downloadCommand);
				chatComponent = chatComponent.copy().append(downloadLink).append("]");

				componentsToSend.add(chatComponent);
			}
			++currentFile;
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