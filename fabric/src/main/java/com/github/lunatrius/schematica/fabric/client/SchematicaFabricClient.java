package com.github.lunatrius.schematica.fabric.client;

import com.github.lunatrius.schematica.handler.client.InputHandler;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE;

public final class SchematicaFabricClient implements ClientModInitializer {
	private final Minecraft minecraft = Minecraft.getInstance();

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		WorldRenderEvents.LAST.register(context -> {
			Player player = Minecraft.getInstance().player;
			if (player != null) {
				ClientProxy.setPlayerData(player, context.tickCounter().getRealtimeDeltaTicks());
			}
		});

		WorldRenderEvents.END.register(context -> {
			FakeLevel schematic = ClientProxy.schematic;
			ClientProxy.objectMouseOver = schematic != null ? rayTrace(schematic, 1.0f) : null;
		});

		for (KeyMapping keyBinding : InputHandler.KEY_BINDINGS) {
			KeyBindingHelper.registerKeyBinding(keyBinding);
		}
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