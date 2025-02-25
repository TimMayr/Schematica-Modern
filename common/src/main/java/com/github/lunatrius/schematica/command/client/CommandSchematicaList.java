package com.github.lunatrius.schematica.command.client;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Map;

import static com.github.lunatrius.schematica.command.CommandSchematicaBase.withStyle;

public class CommandSchematicaList extends ClientCommandSchematicaBase {
	public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> register() {
		return ClientCommandRegistrationEvent.literal(Names.Command.BASE).then(
				ClientCommandRegistrationEvent.literal(Names.Command.List.NAME)
						.executes(CommandSchematicaList::printList)
						.then(ClientCommandRegistrationEvent.argument("page", IntegerArgumentType.integer(1))
								.executes(CommandSchematicaList::printList)));
	}

	private static int printList(@NotNull CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> commandContext)
			throws CommandSyntaxException {
		ClientCommandRegistrationEvent.ClientCommandSourceStack source = commandContext.getSource();
		LocalPlayer player = source.arch$getPlayer();
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

		Map<String, SchematicHolder> schematics = SchematicAccounter.sortedSchematics(player);

		for (Map.Entry<String, SchematicHolder> entry : schematics.entrySet()) {
			if (currentFile >= pageStart && currentFile < pageEnd) {
				String fileName = entry.getKey();

				Component chatComponent = Component.literal(String.format("%2d (%s): %s [", currentFile + 1,
						FileUtils.humanReadableByteCount(entry.getValue().metadata().filesize()),
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
			source.arch$sendFailure(Component.translatable(Names.Command.List.Message.NO_SCHEMATICS));
			return -1;
		}

		int totalPages = (currentFile - 1) / pageSize;
		if (page > totalPages) {
			source.arch$sendFailure(Component.translatable(Names.Command.List.Message.NO_SUCH_PAGE));
			return 1;
		}

		int finalPage = page;
		source.arch$sendSuccess(() ->
				withStyle(Component.translatable(Names.Command.List.Message.PAGE_HEADER, finalPage + 1,
								totalPages + 1),
						ChatFormatting.DARK_GREEN, null), false);
		for (Component chatComponent : componentsToSend) {
			player.displayClientMessage(chatComponent, false);
		}

		return page;
	}
}