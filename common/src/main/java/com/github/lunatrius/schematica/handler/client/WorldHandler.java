package com.github.lunatrius.schematica.handler.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WorldHandler {
	public static WorldHandler INSTANCE;

	private WorldHandler() {
//		ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(world -> {
//          RenderSchematic.getINSTANCE().setWorldAndLoadRenderers(ClientProxy.schematic);
//			addWorldAccess(world, RenderSchematic.getINSTANCE());
//		});
	}

	public static void init() {
		WorldHandler.INSTANCE = new WorldHandler();
	}

//
//	public static void addWorldAccess(Level world, RenderSchematic schematic) {
//		if (world != null && schematic != null) {
//			Reference.logger.debug("Adding world access to {}", world);
//			schematic.addWorld(world);
//		}
//	}
//
//	@SubscribeEvent
//	public void onUnload(WorldEvent.Unload event) {
//		Level world = event.getWorld().getWorld();
//		if (world.isRemote) {
//			removeWorldAccess(world, RenderSchematic.getINSTANCE());
//		}
//	}
//
//	public static void removeWorldAccess(Level world, RenderSchematic schematic) {
//		if (world != null && schematic != null) {
//			Reference.logger.debug("Removing world access from {}", world);
//			schematic.removeWorld(world);
//		}
//	}
}