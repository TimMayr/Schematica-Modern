package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaRemove extends CommandSchematicaBase {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal(Names.Command.Remove.NAME)
				.requires((source) -> source.hasPermissionLevel(0))
				.then(Commands.argument("name", StringArgumentType.string())
						.executes(CommandSchematicaRemove::showDeleteConfirmation)
						.then(Commands.argument("hash", StringArgumentType.string())
								.executes(CommandSchematicaRemove::delete))));
	}

	private static int delete(CommandContext<CommandSource> context) throws CommandSyntaxException {
		String name = StringArgumentType.getString(context, "name");
		String actual = StringArgumentType.getString(context, "hash");
		CommandSource sender = context.getSource();
		ServerPlayerEntity player = sender.asPlayer();

		final File file = getSchematicFile(player, name);

		if (actual.length() == 32) {
			final String expected = DigestUtils.md5Hex(name);
			if (actual.equals(expected)) {
				if (file.delete()) {
					sender.sendFeedback(
							new TranslationTextComponent(Names.Command.Remove.Message.SCHEMATIC_REMOVED, name), true);
				} else {
					throw new CommandException(
							new TranslationTextComponent(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND));
				}
			}
		}
		return 0;
	}

	private static int showDeleteConfirmation(CommandContext<CommandSource> commandContext)
			throws CommandSyntaxException {
		CommandSource sender = commandContext.getSource();
		ServerPlayerEntity player = sender.asPlayer();
		String name = StringArgumentType.getString(commandContext, "name");

		final File file = getSchematicFile(player, name);


		if (file.exists()) {
			final String hash = DigestUtils.md5Hex(name);
			final String confirmCommand = String.format("/%s %s %s", Names.Command.Remove.NAME, name, hash);
			final ITextComponent chatComponent =
					new TranslationTextComponent(Names.Command.Remove.Message.ARE_YOU_SURE_START, name);
			chatComponent.appendSibling(withStyle(TextComponentUtils.wrapInSquareBrackets(
							new TranslationTextComponent(Names.Command.Remove.Message.YES)), TextFormatting.RED,
					confirmCommand));

			sender.sendFeedback(chatComponent, true);
			return 0;
		} else {
			throw new CommandException(new TranslationTextComponent(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND));
		}
	}

	private static File getSchematicFile(PlayerEntity player, String name) {
		final File schematicDirectory = Reference.proxy.getPlayerSchematicDirectory(player, true);
		final File file = new File(schematicDirectory, name);

		if (!FileUtils.contains(schematicDirectory, file)) {
			Reference.logger.error("{} has tried to download the file {}", player.getName(), name);
			throw new CommandException(new TranslationTextComponent(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND));
		}

		return file;
	}

	@Override
	public String getUsage(final ICommandSource sender) {
		return Names.Command.Remove.Message.USAGE;
	}
}
