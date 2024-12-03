package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.SchematicaConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

@Mod.EventBusSubscriber
public class OverlayHandler {
	private static final String SCHEMATICA_PREFIX =
			"[" + TextFormatting.GOLD + "Schematica" + TextFormatting.RESET + "] ";
	private static final String SCHEMATICA_SUFFIX = " [" + TextFormatting.GOLD + "S" + TextFormatting.RESET + "]";
	private final Minecraft minecraft = Minecraft.getInstance();

	@SubscribeEvent
	public void onText(RenderGameOverlayEvent.Text event) {
		if (this.minecraft.gameSettings.showDebugInfo && SchematicaConfig.CLIENT.showDebugInfo.get()) {
			SchematicWorld schematic = ClientProxy.schematic;
			if (schematic != null && schematic.isRendering) {
				ArrayList<String> left = event.getLeft();
				ArrayList<String> right = event.getRight();

				left.add("");
				left.add(SCHEMATICA_PREFIX + schematic.getDebugDimensions());
				left.add(SCHEMATICA_PREFIX + RenderSchematic.INSTANCE.getDebugInfoTileEntities());
				left.add(SCHEMATICA_PREFIX + RenderSchematic.INSTANCE.getDebugInfoRenders());

				RayTraceResult rtr = ClientProxy.objectMouseOver;
				if (rtr != null && rtr.getType() == RayTraceResult.Type.BLOCK) {
					BlockPos pos = new BlockPos(rtr.getHitVec());
					BlockState blockState = schematic.getBlockState(pos);

					right.add("");
					right.add(ForgeRegistries.BLOCKS.getKey(blockState.getBlock()) + SCHEMATICA_SUFFIX);

					for (String formattedProperty : BlockStateHelper.getFormattedProperties(blockState)) {
						right.add(formattedProperty + SCHEMATICA_SUFFIX);
					}

					BlockPos offsetPos = pos.add(schematic.position);
					String lookMessage = getLookMessage(pos, offsetPos);

					left.add(SCHEMATICA_PREFIX + lookMessage);
				}
			}
		}
	}

	private String getLookMessage(BlockPos pos, BlockPos offsetPos) {
		String lookMessage =
				String.format("Looking at: %d %d %d (%d %d %d)", pos.getX(), pos.getY(), pos.getZ(), offsetPos.getX(),
				              offsetPos.getY(), offsetPos.getZ());
		if (this.minecraft.objectMouseOver != null
				&& this.minecraft.objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos origPos = new BlockPos(this.minecraft.objectMouseOver.getHitVec());
			if (offsetPos.equals(origPos)) {
				lookMessage += " (matches)";
			}
		}
		return lookMessage;
	}
}
