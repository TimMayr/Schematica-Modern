package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.handler.QueueTickHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.SchematicContainer;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
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
			e.printStackTrace();
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

	public boolean saveSchematic(Player player, File directory, String filename, Level level, @Nullable String format,
	                             BlockPos from, BlockPos to) {
		try {
			String iconName = "";

			try {
				String[] parts = filename.split(";");
				if (parts.length == 2) {
					iconName = parts[0];
					filename = parts[1];
				}
			} catch (Exception e) {
				Reference.logger.error("Failed to parse icon data!", e);
			}

			int minX = Math.min(from.getX(), to.getX());
			int maxX = Math.max(from.getX(), to.getX());
			int minY = Math.min(from.getY(), to.getY());
			int maxY = Math.max(from.getY(), to.getY());
			int minZ = Math.min(from.getZ(), to.getZ());
			int maxZ = Math.max(from.getZ(), to.getZ());

			short width = (short) (Math.abs(maxX - minX) + 1);
			short height = (short) (Math.abs(maxY - minY) + 1);
			short length = (short) (Math.abs(maxZ - minZ) + 1);

			ISchematic schematic = new Schematic(SchematicUtil.getIconFromName(iconName), width, height, length,
			                                     player.getScoreboardName());
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

	public abstract boolean loadSchematic(Player player, File directory, String filename);

	public abstract boolean isPlayerQuotaExceeded(Player player);

	public abstract File getPlayerSchematicDirectory(Player player, boolean privateDirectory);
}