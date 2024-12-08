package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
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
import java.util.ArrayList;
import java.util.List;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaRemove extends CommandSchematicaBase {
	private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal(Names.Command.Remove.NAME)
		               .requires((source) -> source.hasPermissionLevel(0))
		               .then(Commands.argument("name", StringArgumentType.string())
		                             .suggests(((context, builder) -> {
			                             CommandSource source = context.getSource();
			                             PlayerEntity player;
			                             String name = StringArgumentType.getString(context, "filename");

			                             try {
				                             player = source.asPlayer();
			                             } catch (CommandSyntaxException e) {
				                             return builder.buildFuture();
			                             }

			                             File directory = Reference.proxy.getPlayerSchematicDirectory(player, true);
			                             File[] files = directory.listFiles(FILE_FILTER_SCHEMATIC);

			                             if (files != null) {
				                             List<String> filenames = new ArrayList<>();

				                             for (File file : files) {
					                             filenames.add(file.getName());
				                             }

				                             filenames.stream()
				                                      .filter(s -> s.startsWith(name))
				                                      .forEach(builder::suggest);
				                             return builder.buildFuture();
			                             }

			                             return builder.buildFuture();
		                             }))
		                             .executes(CommandSchematicaRemove::showDeleteConfirmation)
		                             .then(Commands.argument("hash", StringArgumentType.string())
		                                           .executes(CommandSchematicaRemove::delete)));
	}

	private static int delete(CommandContext<CommandSource> context) throws CommandSyntaxException {
		String name = StringArgumentType.getString(context, "name");
		String actual = StringArgumentType.getString(context, "hash");
		CommandSource sender = context.getSource();
		ServerPlayerEntity player = sender.asPlayer();

		File file = getSchematicFile(player, name);

		if (actual.length() == 32) {
			String expected = DigestUtils.md5Hex(name);
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

		File file = getSchematicFile(player, name);


		if (file.exists()) {
			String hash = DigestUtils.md5Hex(name);
			String confirmCommand = String.format("/%s %s %s", Names.Command.Remove.NAME, name, hash);
			ITextComponent chatComponent =
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
		File schematicDirectory = Reference.proxy.getPlayerSchematicDirectory(player, true);
		File file = new File(schematicDirectory, name);

		if (!FileUtils.contains(schematicDirectory, file)) {
			Reference.logger.error("{} has tried to download the file {}", player.getName(), name);
			throw new CommandException(new TranslationTextComponent(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND));
		}

		return file;
	}

	@Override
	public String getUsage(ICommandSource sender) {
		return Names.Command.Remove.Message.USAGE;
	}
}
