package com.github.lunatrius.schematica.client;

import com.github.lunatrius.schematica.command.client.ClientCommandSchematicaBase;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;

import static com.github.lunatrius.schematica.config.client.SchematicaClientConfig.SCHEMATIC_DEFAULT_FOLDER;

@Environment(EnvType.CLIENT)
public class SchematicaClient {
	public static void clientInit() {
		Reference.proxy = new ClientProxy();
		SchematicaClientConfig.schematicDirectory =
				new File(Reference.proxy.getDataDirectory(), SCHEMATIC_DEFAULT_FOLDER);
		ClientCommandRegistrationEvent.EVENT.register(ClientCommandSchematicaBase::registerClient);
		Reference.proxy.init();
	}
}