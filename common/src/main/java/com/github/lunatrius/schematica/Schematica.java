package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Reference;
import com.google.common.base.Suppliers;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

public class Schematica {
	public static final Supplier<RegistrarManager> MANAGER =
			Suppliers.memoize(() -> RegistrarManager.get(Reference.MOD_ID));

	public static void init() {
		if (Platform.getEnv() == EnvType.CLIENT) {
			Schematica.clientInit();
		}

		CommandRegistrationEvent.EVENT.register(
				(dispatcher, context, selection) -> CommandSchematicaBase.register(dispatcher));

		LifecycleEvent.SERVER_STARTING.register(
				(server) -> ServerProxy.serverWeakReference = new WeakReference<>(server));

		Reference.proxy = EnvExecutor.getEnvSpecific(() -> ClientProxy::new, () -> ServerProxy::new);
		Reference.proxy.init();
		PacketHandler.init();
	}

	private static void clientInit() {
		ClientCommandRegistrationEvent.EVENT.register(
				((dispatcher, context) -> CommandSchematicaBase.registerClient(context)));
	}
}