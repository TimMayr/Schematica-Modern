package com.github.lunatrius.schematica.command;

import com.github.lunatrius.core.util.FileUtils;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.FileFilter;

public class CommandSchematicaDownload extends CommandSchematicaBase {
	private static final FileFilter FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(Names.Command.Download.NAME)
		               .then(Commands.argument("filename", StringArgumentType.string())
		                             .suggests(
				                             ((context, builder) -> CommandSchematicaBase.getSchematicNamesSuggestions(
						                             context, builder, FILE_FILTER_SCHEMATIC)))
		                             .executes((commandContext) -> {
			                             CommandSourceStack source = commandContext.getSource();
			                             ServerPlayer player = source.getPlayerOrException();

			                             String filename = StringArgumentType.getString(commandContext, "filename");
			                             File directory = Reference.proxy.getPlayerSchematicDirectory(player, true);

			                             if (!FileUtils.contains(directory, filename)) {
				                             Reference.logger.error("{} has tried to download" + " the file " + "{}",
				                                                    player.getName(), filename);

				                             source.sendFailure(Component.translatable(
						                             Names.Command.Download.Message.DOWNLOAD_FAILED));
				                             return -1;
			                             }

			                             ISchematic schematic = SchematicFormat.readFromFile(directory, filename,
			                                                                                 Reference.proxy.getLevel(
					                                                                                 player));

			                             if (schematic != null) {
				                             DownloadHandler.INSTANCE.transferMap.put(player.getScoreboardName(),
				                                                                      new SchematicTransfer(schematic,
				                                                                                            filename));
				                             source.sendSuccess(() -> Component.translatable(
						                             Names.Command.Download.Message.DOWNLOAD_STARTED, filename), true);
			                             } else {
				                             source.sendFailure(Component.translatable(
						                             Names.Command.Download.Message.DOWNLOAD_FAILED));
				                             return -1;
			                             }

			                             return 0;
		                             }));
	}
}