package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

@Environment(EnvType.CLIENT)
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
	public static FakeLevel schematic = null;
	public static Direction.Axis axisFlip = Direction.Axis.Z;
	public static Direction axisRotation = Direction.UP;
	public static HitResult objectMouseOver = null;

	public static void setPlayerData(@NotNull Player player, float partialTicks) {
		playerPosition.set(new Vector3d(player.getPosition(partialTicks)));
		orientation = getOrientation(player);
		rotationRender = (int) Math.floor(player.getYRot() / 90) & 3;
	}

	private static @NotNull Direction getOrientation(@NotNull Player player) {
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

	public static void movePointToPlayer(@NotNull MBlockPos point) {
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

	public static void moveSchematicToPlayer(FakeLevel level) {
		if (level != null) {
			MBlockPos position = new MBlockPos(level.getWorldPos());
			position.x = (int) Math.floor(playerPosition.x);
			position.y = (int) Math.floor(playerPosition.y);
			position.z = (int) Math.floor(playerPosition.z);

			switch (rotationRender) {
				case 0:
					position.x -= level.getLevelSource().getMaxX();
					position.z += 1;
					break;
				case 1:
					position.x -= level.getLevelSource().getMaxX();
					position.z -= level.getLevelSource().getMaxZ();
					break;
				case 2:
					position.x += 1;
					position.z -= level.getLevelSource().getMaxZ();
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

	@Override
	public RegistryAccess getRegistryAccess() {
		return Minecraft.getInstance().level.registryAccess();
	}

	@Override
	public boolean loadSchematic(Player player, File directory, String filename) {
		ISchematic schematic = SchematicFormat.readFromFile(directory, filename, Reference.proxy.getLevel(player));
		if (schematic == null) {
			return false;
		}

		FakeLevel world = FakeLevel.of(schematic);

		Reference.logger.debug("Loaded {} [w:{},h:{},l:{}]", filename, world.getLevelSource().getMaxX(),
		                       world.getHeight(),
		                       world.getLevelSource().getMaxZ());

		ClientProxy.schematic = world;
		SchematicPrinter.INSTANCE.setSchematic(world);
		world.setRendering(true);

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

	@Override
	public void init() {
		ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
			Reference.proxy.createFolders();
			SchematicaClientConfig.populateExtraAirBlocks();
			SchematicaClientConfig.normalizeSchematicPath();
//		NeoForge.EVENT_BUS.register(new WorldHandler());
			Reference.proxy.resetSettings();
		});
	}

	@Override
	public Level getLevel(Player player) {
		return Minecraft.getInstance().level;
	}
}