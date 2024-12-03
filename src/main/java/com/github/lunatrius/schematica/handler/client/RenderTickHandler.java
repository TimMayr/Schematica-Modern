package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RenderTickHandler {
	public static final RenderTickHandler INSTANCE = new RenderTickHandler();

	private final Minecraft minecraft = Minecraft.getInstance();

	private RenderTickHandler() {}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		SchematicWorld schematic = ClientProxy.schematic;

		ClientProxy.objectMouseOver = schematic != null ? rayTrace(schematic, 1.0f) : null;
	}

	@SuppressWarnings("SameParameterValue")
	private RayTraceResult rayTrace(SchematicWorld schematic, float partialTicks) {
		Entity renderViewEntity = this.minecraft.getRenderViewEntity();
		if (renderViewEntity == null) {
			return null;
		}

		if (this.minecraft.playerController != null) {
			double blockReachDistance = this.minecraft.playerController.getBlockReachDistance();

			double posX = renderViewEntity.getPosX();
			double posY = renderViewEntity.getPosY();
			double posZ = renderViewEntity.getPosZ();

			renderViewEntity.setPosition(renderViewEntity.getPosX() - schematic.position.x,
			                             renderViewEntity.getPosY() - schematic.position.y,
			                             renderViewEntity.getPosZ() - schematic.position.z);

			Vec3d vecPosition = renderViewEntity.getEyePosition(partialTicks);
			Vec3d vecLook = renderViewEntity.getLook(partialTicks);
			Vec3d vecExtendedLook = vecPosition.add(vecLook.x * blockReachDistance, vecLook.y * blockReachDistance,
			                                        vecLook.z * blockReachDistance);

			renderViewEntity.setPosition(posX, posY, posZ);

			return schematic.rayTraceBlocks(
					new RayTraceContext(vecPosition, vecExtendedLook, RayTraceContext.BlockMode.OUTLINE,
					                    RayTraceContext.FluidMode.NONE, renderViewEntity));
		}

		throw new IllegalStateException("Error rendering Schematic");
	}
}
