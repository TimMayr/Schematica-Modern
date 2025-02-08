package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicAlpha;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.github.lunatrius.schematica.world.schematic.SchematicStructure;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;

import java.io.File;
import java.lang.ref.WeakReference;

import static com.github.lunatrius.schematica.config.SchematicaClientConfig.SCHEMATIC_DEFAULT_FOLDER;

public class Schematica {
	public static void init() {
		if (Platform.getEnv() == EnvType.CLIENT) {
			Schematica.clientInit();
		}

		CommandRegistrationEvent.EVENT.register(
				(dispatcher, context, selection) -> CommandSchematicaBase.register(dispatcher));

		if (Platform.getEnv() == EnvType.SERVER) {
			LifecycleEvent.SERVER_STARTING.register(
					(server) -> ServerProxy.serverWeakReference = new WeakReference<>(server));
		}

		Reference.proxy = EnvExecutor.getEnvSpecific(() -> ClientProxy::new, () -> ServerProxy::new);
		Reference.proxy.init();
		PacketHandler.init();

		SchematicaClientConfig.schematicDirectory =
				new File(Reference.proxy.getDataDirectory(), SCHEMATIC_DEFAULT_FOLDER);

		SchematicFormat.FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());
		SchematicFormat.FORMATS.put(Names.NBT.FORMAT_STRUCTURE, new SchematicStructure());
	}

	private static void clientInit() {
		ClientCommandRegistrationEvent.EVENT.register(
				((dispatcher, context) -> CommandSchematicaBase.registerClient(context)));
	}
}