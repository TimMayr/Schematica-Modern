package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.accounting.SchematicLocation;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.network.message.commands.MessageSaveSchematic;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import commonnetwork.api.Dispatcher;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class ClientProxy extends CommonProxy {
	public static final Vector3d playerPosition = new Vector3d();
	public static final MBlockPos pointA = new MBlockPos();
	public static final MBlockPos pointB = new MBlockPos();
	public static final MBlockPos pointMin = new MBlockPos();
	public static final MBlockPos pointMax = new MBlockPos();
	public static boolean isRenderingGuide = false;
	public static boolean isPendingReset = false;
	public static Direction orientation = null;
	public static int rotationRender = 0;
	public static FakeLevel schematic = null;
	public static Direction.Axis axisFlip = Direction.Axis.Z;
	public static Direction axisRotation = Direction.UP;
	public static HitResult objectMouseOver = null;
	public static ISchematic tempSchematic;

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
	public void createFolders() {
		if (!Files.exists(SchematicaClientConfig.schematicDirectory)) {
			try {
				Files.createDirectories(SchematicaClientConfig.schematicDirectory);
			} catch (IOException e) {
				Reference.logger.warn("Could not create schematic directory [{}]!",
						SchematicaClientConfig.schematicDirectory.toAbsolutePath());
			}
		}
	}

	@Override
	public Path getDataDirectory() {
		Path file = Minecraft.getInstance().gameDirectory.toPath();
		try {
			return file.toRealPath().normalize();
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
	public boolean saveSchematic(Player player, @NotNull String filename, Level level, @NotNull String format,
	                             @NotNull BlockPos from, @NotNull BlockPos to, boolean isPrivate,
	                             @NotNull String iconName) {
		MessageSaveSchematic message = new MessageSaveSchematic(filename, format, isPrivate, from, to, iconName);
		Dispatcher.sendToServer(message);

		return true;
	}

	@Override
	public RegistryAccess getRegistryAccess() {
		return Minecraft.getInstance().level.registryAccess();
	}

	@Override
	public CompletableFuture<Boolean> loadSchematic(Player player, @NotNull SchematicMetadata meta) {
		Reference.logger.info("Loading schematic {}", meta.name());

		return SchematicAccounter.get(meta.id()).getSchematic().thenApply(schematic -> {
			if (schematic == null) {
				return false;
			}

			FakeLevel world = FakeLevel.of(schematic);

			Reference.logger.debug("Loaded {} [w:{},h:{},l:{}]", meta.name(), world.getLevelSource().getMaxX(),
					world.getHeight(), world.getLevelSource().getMaxZ());

			ClientProxy.schematic = world;
			SchematicPrinter.INSTANCE.setSchematic(world);
			world.setRendering(true);

			return true;
		});
	}

	@Override
	public boolean isPlayerQuotaExceeded(UUID id) {
		return false;
	}

	@Override
	public void init() {
		Reference.logger.info("Initializing client proxy");

		ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
			Reference.proxy.createFolders();
			SchematicaClientConfig.populateExtraAirBlocks();
			SchematicaClientConfig.normalizeSchematicPath();
			Reference.proxy.resetSettings();
		});

		ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> SchematicAccounter.init());
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> SchematicAccounter.reset());
	}

	@Override
	public Level getLevel(Player player) {
		return getLevel();
	}

	@Override
	public Level getLevel() {
		return Minecraft.getInstance().level;
	}

	@Override
	public void addSchematic(Path path) {
		SchematicAccounter.addSchematic(new SchematicHolder(SchematicFormat.readMetaFromFile(path),
				SchematicLocation.LOCAL), false);
	}

	@Override
	public @NotNull String getUsernameForUUID(@NotNull UUID uuid) {
		return uuid.toString();
	}

	@Override
	public Path getSchematicDirectory() {
		return SchematicaClientConfig.schematicDirectory;
	}

	@Override
	public Path getSchematicDirectory(UUID id) {
		return SchematicaClientConfig.schematicDirectory;
	}

	@Override
	public void updatePlayerUsername(ServerPlayer player) {}

	@Override
	public void sendMessage(Player player, Component component) {
		Minecraft.getInstance().player.displayClientMessage(component, false);
	}
}