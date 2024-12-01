package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSchematicaDownload extends CommandSchematicaBase {
	private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal(Names.Command.Download.NAME)
				.then(Commands.argument("filename", StringArgumentType.string()).suggests(((context, builder) -> {
					CommandSource source = context.getSource();
					PlayerEntity player;
					final String name = StringArgumentType.getString(context, "filename");

					try {
						player = source.asPlayer();
					} catch (CommandSyntaxException e) {
						return builder.buildFuture();
					}

					final File directory = Reference.proxy.getPlayerSchematicDirectory(player, true);
					final File[] files = directory.listFiles(FILE_FILTER_SCHEMATIC);

					if (files != null) {
						final List<String> filenames = new ArrayList<>();

						for (final File file : files) {
							filenames.add(file.getName());
						}

						filenames.stream().filter(s -> s.startsWith(name)).forEach(builder::suggest);
						return builder.buildFuture();
					}

					return builder.buildFuture();
				})).executes((commandContext) -> {
					CommandSource source = commandContext.getSource();
					ServerPlayerEntity player = source.asPlayer();

					final String filename = StringArgumentType.getString(commandContext, "filename");
					final File directory = Reference.proxy.getPlayerSchematicDirectory(player, true);

					if (!FileUtils.contains(directory, filename)) {
						Reference.logger.error("{} has tried to download the file {}", player.getName(), filename);
						throw new CommandException(
								new TranslationTextComponent(Names.Command.Download.Message.DOWNLOAD_FAILED));
					}

					final ISchematic schematic = SchematicFormat.readFromFile(directory, filename);

					if (schematic != null) {
						DownloadHandler.INSTANCE.transferMap.put(player, new SchematicTransfer(schematic, filename));
						source.sendFeedback(
								new TranslationTextComponent(Names.Command.Download.Message.DOWNLOAD_STARTED,
										filename),
								true);
					} else {
						throw new CommandException(
								new TranslationTextComponent(Names.Command.Download.Message.DOWNLOAD_FAILED));
					}

					return 0;
				})));
	}

	@Override
	public String getUsage(final ICommandSource sender) {
		return Names.Command.Download.Message.USAGE;
	}
}
