package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.core.FileUtils;
import com.github.lunatrius.schematica.handler.QueueTickHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import com.github.lunatrius.schematica.world.storage.Schematic;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ServerProxy extends CommonProxy {
	public static WeakReference<MinecraftServer> serverWeakReference = null;

	private static @NotNull List<Path> getAllPublicDirs(@NotNull Path directory) {
		List<Path> publicDirectories = new LinkedList<>();

		if (Files.isDirectory(directory)) {
			List<Path> subFiles = FileUtils.getAllFilesInDirectory(directory);
			for (Path file : subFiles) {
				if (Files.isDirectory(file)) {
					if (file.getFileName().toString().equals("public")) {
						publicDirectories.add(file);
					}
					publicDirectories.addAll(getAllPublicDirs(file));
				}
			}
		}

		return publicDirectories;
	}

	public static boolean saveServerSchematic(Player player, String filename, Level level, @Nullable String format,
	                                          BlockPos from, BlockPos to, boolean isPrivate,
	                                          @Nullable String iconName) {
		try {
			Path directory = Reference.proxy.getPlayerSchematicDirectory(player, isPrivate);
			int minX = Math.min(from.getX(), to.getX());
			int maxX = Math.max(from.getX(), to.getX());
			int minY = Math.min(from.getY(), to.getY());
			int maxY = Math.max(from.getY(), to.getY());
			int minZ = Math.min(from.getZ(), to.getZ());
			int maxZ = Math.max(from.getZ(), to.getZ());

			short width = (short) (Math.abs(maxX - minX) + 1);
			short height = (short) (Math.abs(maxY - minY) + 1);
			short length = (short) (Math.abs(maxZ - minZ) + 1);

			ISchematic schematic = new Schematic(SchematicUtil.getIconFromName(iconName), filename, width, height,
					length, player.getScoreboardName());

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
	public boolean saveSchematic(Player player, @NotNull String filename, Level level, @Nullable String format,
	                             @NotNull BlockPos from, @NotNull BlockPos to, boolean isPrivate,
	                             @NotNull String iconName) {
		return ServerProxy.saveServerSchematic(player, filename, level, format, from, to, isPrivate, iconName);
	}

	@Override
	public Path getDataDirectory() {
		MinecraftServer server = ServerProxy.serverWeakReference != null ? ServerProxy.serverWeakReference.get() :
				null;
		Path file = server != null ? server.getFile(".") : Path.of(".");
		return file.toAbsolutePath().normalize();
	}

	@Override
	public RegistryAccess getRegistryAccess() {
		return serverWeakReference.get().registryAccess();
	}

	@Override
	public boolean loadSchematic(Player player, Path directory, String filename) {
		return false;
	}

	@Override
	public boolean isPlayerQuotaExceeded(Player player) {
		int spaceUsed = 0;

		//Space used by private directory
		Path schematicDirectory = getPlayerSchematicDirectory(player, true);
		spaceUsed += getSpaceUsedByDirectory(schematicDirectory);

		//Space used by public directory
		schematicDirectory = getPlayerSchematicDirectory(player, false);
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
	public Path getPlayerSchematicDirectory(@NotNull Player player, boolean privateDirectory) {
		UUID playerId = player.getUUID();
		Path playerDir = getServerSchematicDirectory().resolve(playerId.toString());

		if (privateDirectory) {
			playerDir = playerDir.resolve("private");
		} else {
			playerDir = playerDir.resolve("public");
		}

		if (!Files.exists(playerDir)) {
			try {
				Files.createDirectories(playerDir);
			} catch (IOException e) {
				Reference.logger.error("Could not create directory [{}]!", playerDir.toAbsolutePath());
			}
		}

		Path usernameFile = playerDir.resolve(".username");
		if (!Files.exists(usernameFile)) {
			try (DataOutputStream dataOutputStream = new DataOutputStream(
					new DataOutputStream(Files.newOutputStream(usernameFile)))) {
				dataOutputStream.writeChars(player.getScoreboardName());
			} catch (IOException e) {
				Reference.logger.warn("Unable to save username");
			}
		} else {
			try {
				String username = Files.readString(usernameFile);
				if (!username.equals(player.getScoreboardName())) {
					try (DataOutputStream dataOutputStream = new DataOutputStream(
							new DataOutputStream(Files.newOutputStream(usernameFile)))) {
						dataOutputStream.writeChars(player.getScoreboardName());
					} catch (IOException e) {
						Reference.logger.warn("Unable to update username");
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return playerDir;
	}

	@Override
	public @NotNull String getUsernameForUUID(@NotNull UUID uuid) {
		Path file = getServerSchematicDirectory().resolve(uuid.toString()).resolve(".username");
		try {
			return Files.readString(file);
		} catch (IOException e) {
			Reference.logger.warn("Unable to get username for uuid \"{}\"", uuid);
			return uuid.toString();
		}
	}

	@Override
	public List<Path> getAllAccessibleDirectories(Player player) {
		List<Path> dirs = new LinkedList<>();
		dirs.add(getPlayerSchematicDirectory(player, true));
		dirs.addAll(getAllPublicDirs());
		return dirs;
	}

	@Override
	public List<Path> getAllSchematicDirectories() {
		List<Path> dirs = new LinkedList<>();
		for (Path playerDir : FileUtils.getAllFilesInDirectory(getServerSchematicDirectory())) {
			dirs.addAll(FileUtils.getAllFilesInDirectory(playerDir));
		}

		return dirs;
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
		UUID owner = UUID.fromString(schematic.getParent().getParent().getFileName().toString());

		SchematicAccounter.addSchematic(new SchematicHolder(schematic, owner, new HashSet<>(),
				new HashSet<>()));
	}

	public List<Path> getAllPublicDirs() {
		Path directory = getServerSchematicDirectory();
		return getAllPublicDirs(directory);
	}
}