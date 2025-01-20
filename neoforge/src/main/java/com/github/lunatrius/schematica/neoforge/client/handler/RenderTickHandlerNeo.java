package com.github.lunatrius.schematica.neoforge.client.handler;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;


public class RenderTickHandlerNeo {
	public static final RenderTickHandlerNeo INSTANCE = new RenderTickHandlerNeo();

	private final Minecraft minecraft = Minecraft.getInstance();


	private RenderTickHandlerNeo() {

	}

	@SubscribeEvent
	public void onRenderTick(RenderFrameEvent.Post event) {
		FakeLevel schematic = ClientProxy.schematic;
		ClientProxy.objectMouseOver = schematic != null ? rayTrace(schematic, 1.0f) : null;
	}

	@SuppressWarnings("SameParameterValue")
	private HitResult rayTrace(FakeLevel schematic, float partialTicks) {
		Entity renderViewEntity = this.minecraft.getCameraEntity();
		if (renderViewEntity == null) {
			return null;
		}

		if (this.minecraft.gameMode != null) {
			//TODO: This shit again
			double blockReachDistance = this.minecraft.gameMode.getBlockReachDistance();

			double posX = renderViewEntity.getX();
			double posY = renderViewEntity.getY();
			double posZ = renderViewEntity.getZ();

			renderViewEntity.setPos(posX - schematic.position.x,
			                        posY - schematic.position.y,
			                        posZ - schematic.position.z);

			Vec3 vecPosition = renderViewEntity.getEyePosition(partialTicks);
			Vec3 vecLook = renderViewEntity.getLookAngle();
			Vec3 vecExtendedLook = vecPosition.add(vecLook.x * blockReachDistance, vecLook.y * blockReachDistance,
			                                       vecLook.z * blockReachDistance);

			renderViewEntity.setPos(posX, posY, posZ);

			return schematic.rayTraceBlocks(
					new RayTraceContext(vecPosition, vecExtendedLook, RayTraceContext.BlockMode.OUTLINE,
					                    RayTraceContext.FluidMode.NONE, renderViewEntity));
		}

		throw new IllegalStateException("Error rendering Schematic");
	}
}