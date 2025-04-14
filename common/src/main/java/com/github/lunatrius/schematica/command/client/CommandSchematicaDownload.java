package com.github.lunatrius.schematica.command.client;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.message.download.DownloadType;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.github.lunatrius.schematica.reference.Constants.Log.SCHEMATIC_NOT_ACCESSIBLE_DOWNLOAD_ERROR;

@Environment(EnvType.CLIENT)
public class CommandSchematicaDownload extends ClientCommandSchematicaBase {
	public static LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> register() {
		//@formatter:off
		return ClientCommandRegistrationEvent
				.literal(Names.Command.BASE)
				.then(ClientCommandRegistrationEvent
						.literal(Names.Command.Download.NAME)
						.then(ClientCommandRegistrationEvent
								.argument("name", StringArgumentType.string())
								.suggests((context, builder) ->
												CommandSchematicaBase.getSchematicNamesSuggestions(
														context.getSource().arch$getPlayer(),
											ClientCommandSchematicaBase.getArgumentAsString(context, "name"),
														builder, FilePermission.READ))
								.executes(CommandSchematicaDownload::download)));
	}

	private static int download(@NotNull CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack>
			                            commandContext) {
		//@formatter:on
		ClientCommandRegistrationEvent.ClientCommandSourceStack source = commandContext.getSource();
		LocalPlayer player = source.arch$getPlayer();

		String name = StringArgumentType.getString(commandContext, "name");
		Map<String, SchematicHolder> schematics = SchematicAccounter.sortedSchematics(player, FilePermission.READ);

		if (schematics.get(name) == null) {
			Reference.logger.error(SCHEMATIC_NOT_ACCESSIBLE_DOWNLOAD_ERROR, name, player.getScoreboardName());

			source.arch$sendFailure(Component.translatable(Names.Command.Download.Message.DOWNLOAD_FAILED));
			return -1;
		}

		ISchematic stub = new Schematic(schematics.get(name).metadata());
		DownloadHandler.INSTANCE.getTransferMap().put(player.getUUID(), new SchematicTransfer(stub,
				DownloadType.SAVE));
		//@formatter:off
		DownloadHandler.INSTANCE.registerDownloadCompleteListener((schematic) ->
				source.arch$sendSuccess(() ->
						Component.translatable(Names.Command.Download.Message.DOWNLOAD_SUCCEEDED, schematic.getName()),
						false), true);
		//@formatter:on

		source.arch$sendSuccess(() -> Component.translatable(Names.Command.Download.Message.DOWNLOAD_STARTED, name),
				false);

		return 0;
	}
}