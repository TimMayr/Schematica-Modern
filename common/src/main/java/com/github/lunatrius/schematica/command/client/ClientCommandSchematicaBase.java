package com.github.lunatrius.schematica.command.client;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class ClientCommandSchematicaBase {
	public static void registerClient(@NotNull CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(CommandSchematicaReplace.register(context));
	}
}
