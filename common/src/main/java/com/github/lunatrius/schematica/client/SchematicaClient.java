package com.github.lunatrius.schematica.client;

import com.github.lunatrius.schematica.command.client.ClientCommandSchematicaBase;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.handler.client.*;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static com.github.lunatrius.schematica.config.client.SchematicaClientConfig.SCHEMATIC_DEFAULT_FOLDER;

@Environment(EnvType.CLIENT)
public class SchematicaClient {
	public static void clientInit() {
		Reference.proxy = new ClientProxy();
		Reference.proxy.init();

		ClientLifecycleEvent.CLIENT_STARTED.register(instance -> SchematicaClientConfig.schematicDirectory =
				Reference.proxy.getDataDirectory().resolve(SCHEMATIC_DEFAULT_FOLDER));
		ClientCommandRegistrationEvent.EVENT.register(ClientCommandSchematicaBase::register);

		GuiHandler.init();
		InputHandler.init();
		OverlayHandler.init();
		TickHandler.init();
		WorldHandler.init();
	}
}