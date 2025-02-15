package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.handler.PlayerHandler;
import com.github.lunatrius.schematica.handler.QueueTickHandler;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicAlpha;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.github.lunatrius.schematica.world.schematic.SchematicStructure;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

import java.lang.ref.WeakReference;

public class Schematica {
	public static void init() {
		CommandRegistrationEvent.EVENT.register(
				(dispatcher, context, selection) -> CommandSchematicaBase.register(dispatcher));

		if (Platform.getEnv() == EnvType.SERVER) {
			serverInit();
		}

		PacketHandler.init();
		DownloadHandler.init();
		PlayerHandler.init();
		QueueTickHandler.init();
		SchematicFormat.FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());
		SchematicFormat.FORMATS.put(Names.NBT.FORMAT_STRUCTURE, new SchematicStructure());
	}

	public static void serverInit() {
		Reference.proxy = new ServerProxy();
		LifecycleEvent.SERVER_STARTING.register(
				(server) -> ServerProxy.serverWeakReference = new WeakReference<>(server));
		Reference.proxy.init();
	}
}