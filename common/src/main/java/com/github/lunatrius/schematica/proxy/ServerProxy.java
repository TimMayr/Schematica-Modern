package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.handler.QueueTickHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ServerProxy extends CommonProxy {
	public static WeakReference<MinecraftServer> serverWeakReference = null;

	private static List<File> getAllPublicDirs(@NotNull File directory) {
		List<File> publicDirectories = new LinkedList<>();

		if (directory.isDirectory()) {
			File[] subFiles = directory.listFiles();
			if (subFiles != null) {
				for (File file : subFiles) {
					if (file.isDirectory()) {
						if (file.getName().equals("public")) {
							publicDirectories.add(file);
						}
						publicDirectories.addAll(getAllPublicDirs(file));
					}
				}
			}
		}

		return publicDirectories;
	}

	@Override
	public File getDataDirectory() {
		MinecraftServer server = ServerProxy.serverWeakReference != null ? ServerProxy.serverWeakReference.get() :
				null;
		File file = server != null ? server.getFile(".").toFile() : new File(".");
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			Reference.logger.warn("Could not canonize path!", e);
		}
		return file;
	}

	@Override
	public boolean saveSchematic(Player player, @NotNull String filename, Level level, @Nullable String format,
	                             @NotNull BlockPos from, @NotNull BlockPos to, boolean isPrivate,
	                             @NotNull String iconName) {
		return ServerProxy.saveServerSchematic(player, filename, level, format, from, to, isPrivate, iconName);
	}

	public static boolean saveServerSchematic(Player player, String filename, Level level, @Nullable String format,
	                                          BlockPos from, BlockPos to, boolean isPrivate,
	                                          @Nullable String iconName) {
		try {
			File directory = Reference.proxy.getPlayerSchematicDirectory(player, isPrivate);
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
					new SchematicContainer(schematic, player, level, new File(directory, filename), format, minX, maxX,
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
	public boolean loadSchematic(Player player, File directory, String filename) {
		return false;
	}

	@Override
	public boolean isPlayerQuotaExceeded(Player player) {
		int spaceUsed = 0;

		//Space used by private directory
		File schematicDirectory = getPlayerSchematicDirectory(player, true);
		spaceUsed += getSpaceUsedByDirectory(schematicDirectory);

		//Space used by public directory
		schematicDirectory = getPlayerSchematicDirectory(player, false);
		spaceUsed += getSpaceUsedByDirectory(schematicDirectory);
		return ((spaceUsed / 1024) > SchematicaConfig.SERVER.playerQuotaKilobytes.get());
	}

	private int getSpaceUsedByDirectory(File directory) {
		int spaceUsed = 0;
		//If we don't have a player directory yet, then they haven't uploaded any files yet.
		if (directory == null || !directory.exists()) {
			return 0;
		}

		File[] files = directory.listFiles();
		if (files == null) {
			files = new File[0];
		}
		for (File path : files) {
			spaceUsed += (int) path.length();
		}
		return spaceUsed;
	}

	@Override
	public File getPlayerSchematicDirectory(@NotNull Player player, boolean privateDirectory) {
		UUID playerId = player.getUUID();
		File playerDir = new File(getServerSchematicDirectory(), playerId.toString());

		if (privateDirectory) {
			playerDir = new File(playerDir, "private");
		} else {
			playerDir = new File(playerDir, "public");
		}

		if (!playerDir.exists()) {
			if (!playerDir.mkdirs()) {
				Reference.logger.error("Could not create directory [{}]!", playerDir.getAbsolutePath());
			}
		}

		return playerDir;
	}

	@Override
	public List<File> getAllAccessibleDirectories(Player player) {
		List<File> dirs = new LinkedList<>();
		dirs.add(getPlayerSchematicDirectory(player, true));
		dirs.addAll(getAllPublicDirs());
		return dirs;
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
	public void init() {
	}

	@Override
	public Level getLevel() {
		return serverWeakReference.get().getAllLevels().iterator().next();
	}

	public File getServerSchematicDirectory() {
		return getDirectory("schematics");
	}

	public List<File> getAllPublicDirs() {
		File directory = getServerSchematicDirectory();
		return getAllPublicDirs(directory);
	}
}