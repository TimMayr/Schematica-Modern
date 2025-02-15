package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.FakeLevel;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class OverlayHandler {
	private static final String SCHEMATICA_PREFIX =
			"[" + ChatFormatting.GOLD + "Schematica" + ChatFormatting.RESET + "] ";
	private static final String SCHEMATICA_SUFFIX = " [" + ChatFormatting.GOLD + "S" + ChatFormatting.RESET + "]";
	private static OverlayHandler INSTANCE;
	private final Minecraft minecraft = Minecraft.getInstance();

	private OverlayHandler() {
		ClientGuiEvent.DEBUG_TEXT_LEFT.register(strings -> {
			if (SchematicaClientConfig.CLIENT.showDebugInfo.get()) {
				FakeLevel level = ClientProxy.schematic;
				if (level != null && level.isRendering()) {
					HitResult rtr = ClientProxy.objectMouseOver;

					if (rtr != null && rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos pos = new MBlockPos(rtr.getLocation());
						BlockPos offsetPos = pos.offset(level.getWorldPos());
						String lookMessage = getLookMessage(pos, offsetPos);

						strings.add(SCHEMATICA_PREFIX + lookMessage);
					}
				}
			}
		});

		ClientGuiEvent.DEBUG_TEXT_RIGHT.register(strings -> {
			if (SchematicaClientConfig.CLIENT.showDebugInfo.get()) {
				FakeLevel level = ClientProxy.schematic;
				if (level != null && level.isRendering()) {
					HitResult rtr = ClientProxy.objectMouseOver;

					if (rtr != null && rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos pos = new MBlockPos(rtr.getLocation());
						BlockState blockState = level.getBlockState(pos);
						strings.add("");
						strings.add(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()) + SCHEMATICA_SUFFIX);

						for (String formattedProperty : BlockStateHelper.getFormattedProperties(blockState)) {
							strings.add(formattedProperty + SCHEMATICA_SUFFIX);
						}
					}
				}
			}
		});
	}

	private String getLookMessage(@NotNull BlockPos pos, @NotNull BlockPos offsetPos) {
		String lookMessage =
				String.format("Looking at: %d %d %d (%d %d %d)", pos.getX(), pos.getY(), pos.getZ(), offsetPos.getX(),
						offsetPos.getY(), offsetPos.getZ());
		if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos origPos = new MBlockPos(this.minecraft.hitResult.getLocation());
			if (offsetPos.equals(origPos)) {
				lookMessage += " (matches)";
			}
		}
		return lookMessage;
	}

	public static void init() {
		OverlayHandler.INSTANCE = new OverlayHandler();
	}
}