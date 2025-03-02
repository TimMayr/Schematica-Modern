package com.github.lunatrius.schematica.command.client;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.message.download.DownloadType;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class CommandSchematicaDownload extends ClientCommandSchematicaBase {
	public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> register() {
		return ClientCommandRegistrationEvent.literal(Names.Command.BASE).then(
				ClientCommandRegistrationEvent.literal(Names.Command.Download.NAME)
						.then(ClientCommandRegistrationEvent.argument("filename", StringArgumentType.string())
								.suggests((context, builder) ->
										CommandSchematicaBase.getSchematicNamesSuggestions(context.getSource()
														.arch$getPlayer(),
												context.getArgument("name", String.class), builder,
												FilePermission.READ))
								.executes((commandContext) -> {
									ClientCommandRegistrationEvent.ClientCommandSourceStack source =
											commandContext.getSource();
									LocalPlayer player = source.arch$getPlayer();

									String filename = StringArgumentType.getString(commandContext, "filename");
									Map<String, SchematicHolder> schematics =
											SchematicAccounter.sortedSchematics(player,
													FilePermission.READ);

									if (schematics.get(filename) == null) {
										Reference.logger.error("Schematic [{}] does not exist, or is not accessible " +
														"by player [{}], and can therefore not be downloaded",
												filename, player.getScoreboardName());

										source.arch$sendFailure(
												Component.translatable(Names.Command.Download.Message.DOWNLOAD_FAILED));
										return -1;
									}

									return schematics.get(filename).getSchematic().thenApply(schematic -> {
										if (schematic != null) {
											DownloadHandler.INSTANCE.transferMap.put(player.getUUID(),
													new SchematicTransfer(schematic, DownloadType.SAVE));
											source.arch$sendSuccess(() -> Component.translatable(
													Names.Command.Download.Message.DOWNLOAD_STARTED, filename), true);
											return 0;
										} else {
											source.arch$sendFailure(Component.translatable(
													Names.Command.Download.Message.DOWNLOAD_FAILED));
											return -1;
										}
									}).getNow(0);
								})));
	}
}