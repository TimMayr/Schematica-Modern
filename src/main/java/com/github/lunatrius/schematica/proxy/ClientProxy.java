package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.handler.SchematicaClientConfig;
import com.github.lunatrius.schematica.handler.client.*;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.File;
import java.io.IOException;

public class ClientProxy extends CommonProxy {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	public static final Vector3d playerPosition = new Vector3d();
	public static final MBlockPos pointA = new MBlockPos();
	public static final MBlockPos pointB = new MBlockPos();
	public static final MBlockPos pointMin = new MBlockPos();
	public static final MBlockPos pointMax = new MBlockPos();
	public static boolean isRenderingGuide = false;
	public static boolean isPendingReset = false;
	public static Direction orientation = null;
	public static int rotationRender = 0;
	public static SchematicWorld schematic = null;
	public static Direction axisFlip = Direction.UP;
	public static Direction axisRotation = Direction.UP;
	public static RayTraceResult objectMouseOver = null;

	public static void setPlayerData(PlayerEntity player, float partialTicks) {
		playerPosition.x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * partialTicks;
		playerPosition.y = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * partialTicks;
		playerPosition.z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * partialTicks;

		orientation = getOrientation(player);

		rotationRender = MathHelper.floor(player.rotationYaw / 90) & 3;
	}

	private static Direction getOrientation(PlayerEntity player) {
		if (player.rotationPitch > 45) {
			return Direction.DOWN;
		} else if (player.rotationPitch < -45) {
			return Direction.UP;
		} else {
			switch (MathHelper.floor(player.rotationYaw / 90.0 + 0.5) & 3) {
				case 0:
					return Direction.SOUTH;
				case 1:
					return Direction.WEST;
				case 2:
					return Direction.NORTH;
				case 3:
					return Direction.EAST;
			}
		}

		return null;
	}

	public static void movePointToPlayer(MBlockPos point) {
		point.x = (int) Math.floor(playerPosition.x);
		point.y = (int) Math.floor(playerPosition.y);
		point.z = (int) Math.floor(playerPosition.z);

		switch (rotationRender) {
			case 0:
				point.x -= 1;
				point.z += 1;
				break;
			case 1:
				point.x -= 1;
				point.z -= 1;
				break;
			case 2:
				point.x += 1;
				point.z -= 1;
				break;
			case 3:
				point.x += 1;
				point.z += 1;
				break;
		}
	}

	public static void moveSchematicToPlayer(SchematicWorld schematic) {
		if (schematic != null) {
			MBlockPos position = schematic.position;
			position.x = (int) Math.floor(playerPosition.x);
			position.y = (int) Math.floor(playerPosition.y);
			position.z = (int) Math.floor(playerPosition.z);

			switch (rotationRender) {
				case 0:
					position.x -= schematic.getWidth();
					position.z += 1;
					break;
				case 1:
					position.x -= schematic.getWidth();
					position.z -= schematic.getLength();
					break;
				case 2:
					position.x += 1;
					position.z -= schematic.getLength();
					break;
				case 3:
					position.x += 1;
					position.z += 1;
					break;
			}
		}
	}

	@SubscribeEvent
	public void preInit(FMLClientSetupEvent event) {
		this.createFolders();
		SchematicaClientConfig.populateExtraAirBlocks();
		SchematicaClientConfig.normalizeSchematicPath();

		for (KeyBinding keyBinding : InputHandler.KEY_BINDINGS) {
			ClientRegistry.registerKeyBinding(keyBinding);
		}
	}

	@SubscribeEvent
	public void init(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(InputHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(TickHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(RenderTickHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(RenderSchematic.INSTANCE);
		MinecraftForge.EVENT_BUS.register(GuiHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new OverlayHandler());
		MinecraftForge.EVENT_BUS.register(new WorldHandler());
	}

	@SubscribeEvent
	public void postInit(FMLClientSetupEvent event) {
		resetSettings();
	}

	@Override
	public File getDataDirectory() {
		File file = MINECRAFT.gameDir;
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			Reference.logger.debug("Could not canonize path!", e);
		}
		return file;
	}

	@Override
	public void resetSettings() {
		super.resetSettings();

		SchematicPrinter.INSTANCE.setEnabled(true);
		unloadSchematic();

		isRenderingGuide = false;

		playerPosition.set(0, 0, 0);
		orientation = null;
		rotationRender = 0;

		pointA.set(0, 0, 0);
		pointB.set(0, 0, 0);
		updatePoints();
	}

	public static void updatePoints() {
		pointMin.x = Math.min(pointA.x, pointB.x);
		pointMin.y = Math.min(pointA.y, pointB.y);
		pointMin.z = Math.min(pointA.z, pointB.z);

		pointMax.x = Math.max(pointA.x, pointB.x);
		pointMax.y = Math.max(pointA.y, pointB.y);
		pointMax.z = Math.max(pointA.z, pointB.z);
	}

	@Override
	public void unloadSchematic() {
		schematic = null;
		RenderSchematic.INSTANCE.setWorldAndLoadRenderers(Minecraft.getInstance().world);
		SchematicPrinter.INSTANCE.setSchematic(null);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean loadSchematic(PlayerEntity player, File directory, String filename) {
		ISchematic schematic = SchematicFormat.readFromFile(directory, filename);
		if (schematic == null) {
			return false;
		}

		SchematicWorld world = new SchematicWorld(schematic);

		Reference.logger.debug("Loaded {} [w:{},h:{},l:{}]", filename, world.getWidth(), world.getHeight(),
		                       world.getLength());

		ClientProxy.schematic = world;
		RenderSchematic.INSTANCE.setWorldAndLoadRenderers(world);
		SchematicPrinter.INSTANCE.setSchematic(world);
		world.isRendering = true;

		return true;
	}

	@Override
	public boolean isPlayerQuotaExceeded(PlayerEntity player) {
		return false;
	}

	@Override
	public File getPlayerSchematicDirectory(PlayerEntity player, boolean privateDirectory) {
		return SchematicaClientConfig.schematicDirectory;
	}
}
