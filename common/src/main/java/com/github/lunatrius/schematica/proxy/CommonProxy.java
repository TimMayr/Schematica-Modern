package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.core.FileUtils;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class CommonProxy {
	public boolean isSaveEnabled = true;
	public boolean isLoadEnabled = true;
	/**
	 * Stores the path of the schematic currently getting added. Currently only needed to make sure the watchService
	 * doesn't discover the schematic we are actively saving.
	 */
	public static final Set<Path> recentlyAdded = ConcurrentHashMap.newKeySet();
	public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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

	public Path getDirectory(String directory) {
		Path dataDirectory = getDataDirectory();
		Path subDirectory = dataDirectory.resolve(directory);

		if (!Files.exists(subDirectory)) {
			try {
				Files.createDirectories(subDirectory);
			} catch (IOException e) {
				Reference.logger.error("Could not create directory [{}]!", subDirectory.toAbsolutePath());
			}
		}

		return subDirectory.normalize();
	}

	public abstract Path getDataDirectory();

	public void resetSettings() {
		this.isSaveEnabled = true;
		this.isLoadEnabled = true;
	}

	public void unloadSchematic() {
	}

	public void copyChunkToSchematic(ISchematic schematic, Level level, int chunkX, int chunkZ, int minX, int maxX,
	                                 int minY, int maxY, int minZ, int maxZ) {
		MBlockPos pos = new MBlockPos();
		MBlockPos localPos = new MBlockPos();
		int localMinX = minX < (chunkX << 4) ? 0 : (minX & 15);
		int localMaxX = maxX > ((chunkX << 4) + 15) ? 15 : (maxX & 15);
		int localMinZ = minZ < (chunkZ << 4) ? 0 : (minZ & 15);
		int localMaxZ = maxZ > ((chunkZ << 4) + 15) ? 15 : (maxZ & 15);

		for (int chunkLocalX = localMinX; chunkLocalX <= localMaxX; chunkLocalX++) {
			for (int chunkLocalZ = localMinZ; chunkLocalZ <= localMaxZ; chunkLocalZ++) {
				for (int y = minY; y <= maxY; y++) {
					int x = chunkLocalX | (chunkX << 4);
					int z = chunkLocalZ | (chunkZ << 4);

					int localX = x - minX;
					int localY = y - minY;
					int localZ = z - minZ;

					pos.set(x, y, z);
					localPos.set(localX, localY, localZ);

					try {
						BlockState blockState = level.getBlockState(pos);
						Block block = blockState.getBlock();
						boolean success = schematic.setBlockState(localPos, blockState);

						if (success && block instanceof EntityBlock) {
							BlockEntity blockEntity = level.getBlockEntity(pos);
							if (blockEntity != null) {
								schematic.setBlockEntity(localPos, blockEntity);
							}
						}
					} catch (Exception e) {
						Reference.logger.error("Something went wrong!", e);
					}
				}
			}
		}

		int minX1 = localMinX | (chunkX << 4);
		int minZ1 = localMinZ | (chunkZ << 4);
		int maxX1 = localMaxX | (chunkX << 4);
		int maxZ1 = localMaxZ | (chunkZ << 4);
		AABB bb = new AABB(minX1, minY, minZ1, maxX1 + 1, maxY + 1, maxZ1 + 1);
		List<Entity> entities = level.getEntitiesOfClass(Entity.class, bb);
		for (Entity entity : entities) {
			schematic.addEntity(entity);
		}
	}

	public abstract boolean saveSchematic(Player player, @NotNull String filename, Level level,
	                                      @NotNull String format, @NotNull BlockPos from, @NotNull BlockPos to,
	                                      boolean isPrivate, @NotNull String iconName);

	public abstract RegistryAccess getRegistryAccess();

	public abstract boolean loadSchematic(Player player, Path directory, String filename);

	public abstract boolean isPlayerQuotaExceeded(Player player);

	public abstract Path getPlayerSchematicDirectory(Player player, boolean privateDirectory);

	public List<Path> getAllAccessibleSchematics(Player player) {
		List<Path> dirs = getAllAccessibleDirectories(player);
		return getPaths(dirs);
	}

	public abstract List<Path> getAllAccessibleDirectories(Player player);

	@NotNull
	private List<Path> getPaths(@NotNull List<Path> dirs) {
		List<Path> schematics = new LinkedList<>();

		for (Path dir : dirs) {
			schematics.addAll(FileUtils.getAllFilesInDirectory(dir));
		}

		schematics.sort(Comparator.comparing(path -> path.getFileName().toFile()));

		return schematics;
	}

	public List<Path> getAllSchematics() {
		List<Path> dirs = getAllSchematicDirectories();
		return getPaths(dirs);
	}

	public abstract List<Path> getAllSchematicDirectories();

	public abstract void init();

	public abstract Level getLevel(Player player);

	public abstract Level getLevel();

	public abstract void addSchematic(Path schematic);
}