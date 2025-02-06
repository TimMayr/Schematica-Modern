package com.github.lunatrius.schematica.neoforge.client.handler;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE;

@OnlyIn(Dist.CLIENT)
public class RenderTickHandlerNeo {
	public static final RenderTickHandlerNeo INSTANCE = new RenderTickHandlerNeo();
	private final Minecraft minecraft = Minecraft.getInstance();

	private RenderTickHandlerNeo() {}

	@SubscribeEvent
	public void onRenderTick(RenderFrameEvent.Post event) {
		FakeLevel schematic = ClientProxy.schematic;
		ClientProxy.objectMouseOver = schematic != null ? rayTrace(schematic, 1.0f) : null;
	}

	@SuppressWarnings("SameParameterValue")
	private @Nullable HitResult rayTrace(FakeLevel schematic, float partialTicks) {
		Entity renderViewEntity = this.minecraft.getCameraEntity();
		if (renderViewEntity == null) {
			return null;
		}

		if (this.minecraft.gameMode != null) {
			double blockReachDistance = this.minecraft.player.getAttributeValue(ENTITY_INTERACTION_RANGE);

			double posX = renderViewEntity.getX();
			double posY = renderViewEntity.getY();
			double posZ = renderViewEntity.getZ();

			renderViewEntity.setPos(posX - schematic.getWorldPos().x, posY - schematic.getWorldPos().y,
			                        posZ - schematic.getWorldPos().z);

			Vec3 vecPosition = renderViewEntity.getEyePosition(partialTicks);
			Vec3 vecLook = renderViewEntity.getLookAngle();
			Vec3 vecExtendedLook = vecPosition.add(vecLook.x * blockReachDistance, vecLook.y * blockReachDistance,
			                                       vecLook.z * blockReachDistance);

			renderViewEntity.setPos(posX, posY, posZ);

			return schematic.clip(
					new ClipContext(vecPosition, vecExtendedLook, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
					                renderViewEntity));
		}

		throw new IllegalStateException("Error rendering Schematic");
	}
}