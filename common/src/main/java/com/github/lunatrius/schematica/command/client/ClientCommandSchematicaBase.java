package com.github.lunatrius.schematica.command.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class ClientCommandSchematicaBase {
	public static void register(
			@NotNull CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher,
			CommandBuildContext context) {
		dispatcher.register(CommandSchematicaReplace.register(context));
		dispatcher.register(CommandSchematicaDownload.register());
	}

	protected static @NotNull String getArgumentAsString(
			@NotNull CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context,
			String argumentName) {
		if (context.getInput().endsWith(" ")) {
			return "";
		}

		return context.getArgument(argumentName, String.class);
	}
}
