package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.command.CommandSchematicaBase;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.handler.PlayerHandler;
import com.github.lunatrius.schematica.handler.QueueTickHandler;
import com.github.lunatrius.schematica.handler.client.*;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.ref.WeakReference;

@Mod(Reference.MODID)
public class Schematica {
	public static Schematica instance;

	public Schematica() {
		instance = this;
		MinecraftForge.EVENT_BUS.register(this);
		Reference.proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);

		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
		System.out.println("Mod started loading");
	}

	@SubscribeEvent
	public void init(FMLCommonSetupEvent event) {
		PacketHandler.init();

		MinecraftForge.EVENT_BUS.register(QueueTickHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(DownloadHandler.INSTANCE);

		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");
		System.out.println("Mod proxy started loading");

	}

	@SubscribeEvent
	public void preInit(FMLClientSetupEvent event) {
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");
		System.out.println("Mod client started loading");

		Reference.proxy.createFolders();
		SchematicaClientConfig.populateExtraAirBlocks();
		SchematicaClientConfig.normalizeSchematicPath();

		for (KeyBinding keyBinding : InputHandler.KEY_BINDINGS) {
			ClientRegistry.registerKeyBinding(keyBinding);
		}

		MinecraftForge.EVENT_BUS.register(InputHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(TickHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(RenderTickHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(RenderSchematic.getINSTANCE());
		MinecraftForge.EVENT_BUS.register(GuiHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new OverlayHandler());
		MinecraftForge.EVENT_BUS.register(new WorldHandler());
		Reference.proxy.resetSettings();
	}

	@SubscribeEvent
	public void serverStarting(FMLServerStartingEvent event) {
		MinecraftForge.EVENT_BUS.register(PlayerHandler.INSTANCE);
		CommandSchematicaBase.register(event.getCommandDispatcher());
		ServerProxy.serverWeakReference = new WeakReference<>(event.getServer());

		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
		System.out.println("Mod server started loading");
	}
}
