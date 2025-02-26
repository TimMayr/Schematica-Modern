package com.github.lunatrius.schematica.command;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.message.download.DownloadType;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class CommandSchematicaDownload extends CommandSchematicaBase {
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

								source.sendFailure(
										Component.translatable(Names.Command.Download.Message.DOWNLOAD_FAILED));
								return -1;
							}

							return schematics.get(filename).getSchematic().thenApply(schematic -> {
								if (schematic != null) {
									DownloadHandler.INSTANCE.transferMap.put(player.getUUID(),
											new SchematicTransfer(schematic, DownloadType.SAVE));
									source.sendSuccess(() -> Component.translatable(
											Names.Command.Download.Message.DOWNLOAD_STARTED, filename), true);
									return 0;
								} else {
									source.sendFailure(Component.translatable(
											Names.Command.Download.Message.DOWNLOAD_FAILED));
									return -1;
								}
							}).getNow(0);
						}));
	}
}