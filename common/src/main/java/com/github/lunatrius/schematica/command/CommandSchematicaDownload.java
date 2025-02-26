package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.message.download.DownloadType;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CommandSchematicaDownload extends CommandSchematicaBase {
	private static final DirectoryStream.Filter<Path> FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(Names.Command.Download.NAME)
				.then(Commands.argument("filename", StringArgumentType.string())
						.suggests((context, builder) -> CommandSchematicaBase.getSchematicNamesSuggestions(context,
								builder, FilePermission.READ))
						.executes((commandContext) -> {
							CommandSourceStack source = commandContext.getSource();
							ServerPlayer player = source.getPlayerOrException();

							String filename = StringArgumentType.getString(commandContext, "filename");
							Map<String, SchematicHolder> schematics = SchematicAccounter.sortedSchematics(player,
									FilePermission.READ);

							if (schematics.get(filename) == null) {
								Reference.logger.error("Schematic [{}] does not exist, or is not accessible by " +
												"player [{}], and can therefore not be downloaded", filename,
										player.getScoreboardName());

								source.sendFailure(Component.translatable(
										Names.Command.Download.Message.DOWNLOAD_FAILED));
								return -1;
							}

							try {
								return schematics.get(filename).getSchematic().thenApply(schematic -> {
									if (schematic != null) {
										DownloadHandler.INSTANCE.transferMap.put(player.getScoreboardName(),
												new SchematicTransfer(schematic, filename, DownloadType.SAVE));
										source.sendSuccess(() -> Component.translatable(
												Names.Command.Download.Message.DOWNLOAD_STARTED, filename), true);
										return 0;
									} else {
										source.sendFailure(Component.translatable(
												Names.Command.Download.Message.DOWNLOAD_FAILED));
										return -1;
									}
								}).get();
							} catch (InterruptedException | ExecutionException e) {
								Reference.logger.error("Download failed");
								return -1;
							}
						}));
	}
}