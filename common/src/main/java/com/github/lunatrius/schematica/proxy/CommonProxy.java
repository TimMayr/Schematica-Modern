package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class CommonProxy {
	/**
	 * Stores the path of the schematic currently getting added. Currently only needed to make sure the watchService
	 * doesn't discover the schematic we are actively saving.
	 */
	public static final Set<Path> recentlyAdded = ConcurrentHashMap.newKeySet();
	public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	public static Set<Path> recentlyRemoved = ConcurrentHashMap.newKeySet();
	public boolean isSaveEnabled = true;
	public boolean isLoadEnabled = true;

	public abstract void createFolders();

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

	public abstract CompletableFuture<Boolean> loadSchematic(Player player, SchematicMetadata metadata);

	public abstract boolean isPlayerQuotaExceeded(UUID id);

	public abstract void init();

	public abstract Level getLevel(Player player);

	public abstract Level getLevel();

	public abstract void addSchematic(Path schematic);

	public abstract String getUsernameForUUID(UUID uuid);

	public Path resolveSchematic(@NotNull SchematicMetadata metadata) {
		List<Path> fileList = getAllLocalSchematics();

		if (!fileList.stream().map(p -> p.getFileName().toString()).toList().contains(metadata.name())) {
			Reference.logger.error("Schematic not found in directory [{}]",
					Reference.proxy.getSchematicDirectory());

			throw new IllegalArgumentException(String.format("Schematic not found in directory [%s]",
					Reference.proxy.getSchematicDirectory()));
		}

		return fileList.stream().filter(p -> SchematicFormat.readMetaFromFile(p).id().equals(metadata.id()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Schematic not found in directory [%s]",
								Reference.proxy.getSchematicDirectory())));

	}

	public List<Path> getAllLocalSchematics() {
		try {
			List<Path> fileList = new ArrayList<>();
			Files.walkFileTree(Reference.proxy.getSchematicDirectory(), new SimpleFileVisitor<>() {
				@Override
				public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
					if (new FileFilterSchematic(false).accept(file)) {
						fileList.add(file);
					}
					return FileVisitResult.CONTINUE;
				}
			});

			return fileList;
		} catch (IOException e) {
			Reference.logger.error("Unable to find files in directory [{}]", Reference.proxy.getSchematicDirectory());
			return List.of();
		}
	}

	public abstract Path getSchematicDirectory();

	public abstract Path getSchematicDirectory(UUID id);

	public abstract void updatePlayerUsername(ServerPlayer player);

	public abstract void sendMessage(Player player, Component component);
}