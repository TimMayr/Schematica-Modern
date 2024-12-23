package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.handler.PlayerHandler;
import com.github.lunatrius.schematica.handler.QueueTickHandler;
import com.github.lunatrius.schematica.handler.client.*;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Reference;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.lang.ref.WeakReference;

@Mod(Reference.MODID)
public class Schematica {
	public static Schematica instance;

	public Schematica(IEventBus modEventBus, ModContainer modContainer) {
		instance = this;
		NeoForge.EVENT_BUS.register(this);
		Reference.proxy = FMLLoader.getDist() == Dist.CLIENT ? new ClientProxy() : new ServerProxy();
		modEventBus.register(this);

		modContainer.registerConfig(ModConfig.Type.CLIENT, SchematicaConfig.clientSpec);
		modContainer.registerConfig(ModConfig.Type.SERVER, SchematicaConfig.serverSpec);
	}

	@SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent event) {
		PacketHandler.init();

		NeoForge.EVENT_BUS.register(QueueTickHandler.INSTANCE);
		NeoForge.EVENT_BUS.register(DownloadHandler.INSTANCE);
	}

	@SubscribeEvent
	public void clientSetup(FMLClientSetupEvent event) {
		Reference.proxy.createFolders();
		SchematicaClientConfig.populateExtraAirBlocks();
		SchematicaClientConfig.normalizeSchematicPath();

		for (KeyBinding keyBinding : InputHandler.KEY_BINDINGS) {
			ClientRegistry.registerKeyBinding(keyBinding);
		}

		NeoForge.EVENT_BUS.register(InputHandler.INSTANCE);
		NeoForge.EVENT_BUS.register(TickHandler.INSTANCE);
		NeoForge.EVENT_BUS.register(RenderTickHandler.INSTANCE);
		NeoForge.EVENT_BUS.register(GuiHandler.INSTANCE);
		NeoForge.EVENT_BUS.register(new OverlayHandler());
		NeoForge.EVENT_BUS.register(new WorldHandler());
		Reference.proxy.resetSettings();
	}

	@SubscribeEvent
	public void serverStarting(ServerStartingEvent event) {
		NeoForge.EVENT_BUS.register(PlayerHandler.INSTANCE);
		CommandSchematicaBase.register(event.getServer().getCommands().getDispatcher());
		ServerProxy.serverWeakReference = new WeakReference<>(event.getServer());
	}
}