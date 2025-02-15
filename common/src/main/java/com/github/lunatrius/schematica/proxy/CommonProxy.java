package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class CommonProxy {
	public boolean isSaveEnabled = true;
	public boolean isLoadEnabled = true;

	public void createFolders() {
		if (!SchematicaClientConfig.schematicDirectory.exists()) {
			if (!SchematicaClientConfig.schematicDirectory.mkdirs()) {
				Reference.logger.warn("Could not create schematic directory [{}]!",
						SchematicaClientConfig.schematicDirectory.getAbsolutePath());
			}
		}
	}

	public File getDirectory(String directory) {
		File dataDirectory = getDataDirectory();
		File subDirectory = new File(dataDirectory, directory);

		if (!subDirectory.exists()) {
			if (!subDirectory.mkdirs()) {
				Reference.logger.error("Could not create directory [{}]!", subDirectory.getAbsolutePath());
			}
		}

		try {
			return subDirectory.getCanonicalFile();
		} catch (IOException e) {
			Reference.logger.error("Could not canonize directory [{}]!", subDirectory.getAbsolutePath());
		}

		return subDirectory;
	}

	public abstract File getDataDirectory();

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

	public abstract boolean loadSchematic(Player player, File directory, String filename);

	public abstract boolean isPlayerQuotaExceeded(Player player);

	public abstract File getPlayerSchematicDirectory(Player player, boolean privateDirectory);

	public List<File> getAllAccessibleSchematics(Player player, FileFilter filter) {
		List<File> dirs = getAllAccessibleDirectories(player);
		List<File> schematics = new LinkedList<>();

		for (File dir : dirs) {
			Collections.addAll(schematics, dir.listFiles(filter));
		}

		return schematics;
	}

	public abstract List<File> getAllAccessibleDirectories(Player player);

	public abstract void init();

	public abstract Level getLevel(Player player);

	public abstract Level getLevel();
}