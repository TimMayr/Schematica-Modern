package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.File;
import java.io.IOException;

public class ClientProxy extends CommonProxy {
	public static final Vector3d playerPosition = new Vector3d();
	public static final MBlockPos pointA = new MBlockPos();
	public static final MBlockPos pointB = new MBlockPos();
	public static final MBlockPos pointMin = new MBlockPos();
	public static final MBlockPos pointMax = new MBlockPos();
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	public static boolean isRenderingGuide = false;
	public static boolean isPendingReset = false;
	public static Direction orientation = null;
	public static int rotationRender = 0;
	public static SchematicWorld schematic = null;
	public static Direction axisFlip = Direction.UP;
	public static Direction axisRotation = Direction.UP;
	public static HitResult objectMouseOver = null;

	public static void setPlayerData(Player player, float partialTicks) {
		playerPosition.set(new Vector3d(player.getPosition(partialTicks)));

		orientation = getOrientation(player);

		rotationRender = (int) Math.floor(player.getYRot() / 90) & 3;
	}

	private static Direction getOrientation(Player player) {
		if (player.getXRot() > 45) {
			return Direction.DOWN;
		} else if (player.getXRot() < -45) {
			return Direction.UP;
		} else {
			switch ((int) Math.floor(player.getYRot() / 90.0 + 0.5) & 3) {
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

	@Override
	public File getDataDirectory() {
		File file = MINECRAFT.gameDirectory;
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
		SchematicPrinter.INSTANCE.setSchematic(null);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean loadSchematic(Player player, File directory, String filename) {
		ISchematic schematic = SchematicFormat.readFromFile(directory, filename);
		if (schematic == null) {
			return false;
		}

		SchematicWorld world = new SchematicWorld(schematic);

		Reference.logger.debug("Loaded {} [w:{},h:{},l:{}]", filename, world.getWidth(), world.getHeight(),
		                       world.getLength());

		ClientProxy.schematic = world;
		SchematicPrinter.INSTANCE.setSchematic(world);
		world.isRendering = true;

		return true;
	}

	@Override
	public boolean isPlayerQuotaExceeded(Player player) {
		return false;
	}

	@Override
	public File getPlayerSchematicDirectory(Player player, boolean privateDirectory) {
		return SchematicaClientConfig.schematicDirectory;
	}
}