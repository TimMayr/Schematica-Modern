package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.accounting.SchematicLocation;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicDimensions;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.core.FileUtils;
import com.github.lunatrius.schematica.handler.QueueTickHandler;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import com.github.lunatrius.schematica.world.storage.Schematic;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ServerProxy extends CommonProxy {
	public static WeakReference<MinecraftServer> serverWeakReference = null;

	@Override
	public void createFolders() {
		if (!Files.exists(Reference.proxy.getSchematicDirectory().resolve("schematics"))) {
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
		MinecraftServer server = ServerProxy.serverWeakReference != null ? ServerProxy.serverWeakReference.get() :
				null;
		Path file = server != null ? server.getFile(".") : Path.of(".");
		return file.toAbsolutePath().normalize();
	}

	@Override
	public boolean saveSchematic(Player player, @NotNull String filename, Level level, @NotNull String format,
	                             @NotNull BlockPos from, @NotNull BlockPos to, boolean isPrivate,
	                             @NotNull String iconName) {
		return ServerProxy.saveServerSchematic(player, filename, level, format, from, to, isPrivate, iconName);
	}

	public static boolean saveServerSchematic(Player player, String filename, Level level, @NotNull String format,
	                                          BlockPos from, BlockPos to, boolean isPrivate,
	                                          @Nullable String iconName) {
		try {
			Path directory = Reference.proxy.getSchematicDirectory();
			int minX = Math.min(from.getX(), to.getX());
			int maxX = Math.max(from.getX(), to.getX());
			int minY = Math.min(from.getY(), to.getY());
			int maxY = Math.max(from.getY(), to.getY());
			int minZ = Math.min(from.getZ(), to.getZ());
			int maxZ = Math.max(from.getZ(), to.getZ());

			short width = (short) (Math.abs(maxX - minX) + 1);
			short height = (short) (Math.abs(maxY - minY) + 1);
			short length = (short) (Math.abs(maxZ - minZ) + 1);

			SchematicMetadata metadata = new SchematicMetadata(filename, player.getUUID(), new HashMap<>(),
					SchematicFormat.getFormatFromName(format), new SchematicDimensions(width, height, length),
					SchematicUtil.getIconFromName(iconName), UUID.randomUUID(), -1, Instant.now(), isPrivate);
			ISchematic schematic = new Schematic(metadata);

			PlatformProxy.createAndPostPreSchematicCaptureEvent(new AABB(minX, minY, minZ, maxX, maxY, maxZ));

			SchematicContainer container =
					new SchematicContainer(schematic, player, level, directory.resolve(filename), format, minX, maxX,
							minY, maxY, minZ, maxZ);
			QueueTickHandler.INSTANCE.queueSchematic(container);

			return true;
		} catch (Exception e) {
			Reference.logger.error("Failed to save schematic!", e);
		}

		return false;
	}

	@Override
	public RegistryAccess getRegistryAccess() {
		return serverWeakReference.get().registryAccess();
	}

	@Override
	public boolean loadSchematic(Player player, SchematicMetadata metadata) {
		return false;
	}

	@Override
	public boolean isPlayerQuotaExceeded(UUID id) {
		int spaceUsed = 0;

		Path schematicDirectory = getSchematicDirectory(id);
		spaceUsed += getSpaceUsedByDirectory(schematicDirectory);

		return ((spaceUsed / 1024) > SchematicaConfig.SERVER.playerQuotaKilobytes.get());
	}

	private int getSpaceUsedByDirectory(Path directory) {
		int spaceUsed = 0;
		//If we don't have a player directory yet, then they haven't uploaded any files yet.
		if (directory == null || !Files.exists(directory)) {
			return 0;
		}

		try {
			List<Path> files = FileUtils.getAllFilesInDirectory(directory);
			for (Path path : files) {
				spaceUsed += (int) Files.size(path);
			}
		} catch (IOException ignored) {}
		return spaceUsed;
	}

	public Path getServerSchematicDirectory() {
		return getDirectory("schematics");
	}

	@Override
	public void init() {
		Reference.logger.info("Initializing server proxy");

		LifecycleEvent.SERVER_STARTED.register(server -> {
			ServerProxy.serverWeakReference = new WeakReference<>(server);

			SchematicAccounter.init();
		});
	}

	@Override
	public Level getLevel(Player player) {
		try (Level level = player.level()) {
			return serverWeakReference.get().getLevel(level.dimension());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Level getLevel() {
		return serverWeakReference.get().getAllLevels().iterator().next();
	}

	@Override
	public void addSchematic(@NotNull Path schematic) {
		SchematicAccounter.addSchematic(new SchematicHolder(SchematicFormat.readMetaFromFile(schematic),
				SchematicLocation.LOCAL), false);
	}

	@Override
	public @NotNull String getUsernameForUUID(@NotNull UUID uuid) {
		Path file = getServerSchematicDirectory().resolve(uuid.toString()).resolve(Names.NBT.USERNAME_FILE);
		try {
			return Files.readString(file);
		} catch (IOException e) {
			Reference.logger.warn("Unable to get username for uuid \"{}\"", uuid);
			return uuid.toString();
		}
	}

	@Override
	public Path getSchematicDirectory() {
		return null;
	}

	@Override
	public Path getSchematicDirectory(@NotNull UUID id) {
		Path playerDir = getServerSchematicDirectory().resolve(id.toString());
		if (!Files.exists(playerDir)) {
			try {
				Files.createDirectories(playerDir);
			} catch (IOException e) {
				Reference.logger.error("Could not create directory [{}]!", playerDir.toAbsolutePath());
			}
		}

		return playerDir;
	}

	@Override
	public void updatePlayerUsername(@NotNull ServerPlayer player) {
		Path usernameFile =
				getSchematicDirectory().resolve(player.getUUID().toString()).resolve(Names.NBT.USERNAME_FILE);
		try {
			if (!Files.exists(usernameFile)) {
				Files.createDirectories(usernameFile);
			}
			Files.writeString(usernameFile, player.getScoreboardName(), StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			Reference.logger.warn("Unable to update username for player [{}]", player.getScoreboardName());
		}
	}

	@Override
	public void sendMessage(@Nullable Player player, Component component) {
		if (player != null) {
			player.displayClientMessage(component, false);
		}
	}
}