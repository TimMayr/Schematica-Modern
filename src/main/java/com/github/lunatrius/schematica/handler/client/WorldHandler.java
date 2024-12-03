package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class WorldHandler {
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event) {
		World world = event.getWorld().getWorld();
		if (world.isRemote && !(world instanceof SchematicWorld)) {
			RenderSchematic.INSTANCE.setWorldAndLoadRenderers(ClientProxy.schematic);
			addWorldAccess(world, RenderSchematic.INSTANCE);
		}
	}

	public static void addWorldAccess(World world, RenderSchematic schematic) {
		if (world != null && schematic != null) {
			Reference.logger.debug("Adding world access to {}", world);
			schematic.addWorld(world);
		}
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event) {
		World world = event.getWorld().getWorld();
		if (world.isRemote) {
			removeWorldAccess(world, RenderSchematic.INSTANCE);
		}
	}

	public static void removeWorldAccess(World world, RenderSchematic schematic) {
		if (world != null && schematic != null) {
			Reference.logger.debug("Removing world access from {}", world);
			schematic.removeWorld(world);
		}
	}
}
