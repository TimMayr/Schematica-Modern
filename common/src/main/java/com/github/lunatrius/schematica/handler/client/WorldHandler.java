package com.github.lunatrius.schematica.handler.client;

public class WorldHandler {
//	@SubscribeEvent
//	public void onLoad(WorldEvent.Load event) {
//		World world = event.getWorld().getWorld();
//		if (world.isRemote && !(world instanceof SchematicWorld)) {
//			RenderSchematic.getINSTANCE().setWorldAndLoadRenderers(ClientProxy.schematic);
//			addWorldAccess(world, RenderSchematic.getINSTANCE());
//		}
//	}
//
//	public static void addWorldAccess(World world, RenderSchematic schematic) {
//		if (world != null && schematic != null) {
//			Reference.logger.debug("Adding world access to {}", world);
//			schematic.addWorld(world);
//		}
//	}
//
//	@SubscribeEvent
//	public void onUnload(WorldEvent.Unload event) {
//		World world = event.getWorld().getWorld();
//		if (world.isRemote) {
//			removeWorldAccess(world, RenderSchematic.getINSTANCE());
//		}
//	}
//
//	public static void removeWorldAccess(World world, RenderSchematic schematic) {
//		if (world != null && schematic != null) {
//			Reference.logger.debug("Removing world access from {}", world);
//			schematic.removeWorld(world);
//		}
//	}
}