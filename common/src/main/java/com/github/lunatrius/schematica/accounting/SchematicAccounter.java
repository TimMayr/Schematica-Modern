package com.github.lunatrius.schematica.accounting;

import com.github.lunatrius.schematica.core.FileNameUtils;
import com.github.lunatrius.schematica.core.PlayerUtils;
import com.github.lunatrius.schematica.network.message.accounting.MessageAddSchematic;
import com.github.lunatrius.schematica.network.message.accounting.MessageRemoveSchematic;
import com.github.lunatrius.schematica.proxy.CommonProxy;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import commonnetwork.api.Dispatcher;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for keeping the client and the server synced when it comes to Schematic Metadata.
 * Without it, the server wouldn't know which schematics the client has locally, and the client would be able to load
 * schematics stored on the server.
 */
public class SchematicAccounter {
	private static final Map<UUID, SchematicHolder> schematics = new HashMap<>();
	private static final FileFilterSchematic FILTER_SCHEMATIC = new FileFilterSchematic(false);
	private static WatchService watchService;
	private static final Map<WatchKey, Path> keyToPathMap = new HashMap<>();
	private static final TickEvent.Server listener = (server) -> {
		try {
			WatchKey key = watchService.poll(100, TimeUnit.MILLISECONDS);
			if (key != null) {
				Path schematicDir = keyToPathMap.get(key);

				for (WatchEvent<?> event : key.pollEvents()) {
					if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
						if (event.context() instanceof Path relativePath) {
							Path fullPath = schematicDir.resolve(relativePath).toAbsolutePath().normalize();
							if (FILTER_SCHEMATIC.accept(fullPath)) {
								//This check makes sure that the discovered schematic isn't actively getting saved by
								//some other method, as that would duplicate the entry
								if (!CommonProxy.recentlyAdded.contains(fullPath)) {
									UUID owner;

									if (Platform.getEnv() == EnvType.CLIENT) {
										owner = PlayerUtils.getClientPlayer().getUUID();
									} else {
										owner = UUID.fromString(
												fullPath.getParent().getParent().getFileName().toString());
									}

									SchematicHolder holder = new SchematicHolder(fullPath, owner, new HashSet<>(),
											new HashSet<>());

									Reference.logger.info("Schematic [{}] was discovered by watchService",
											holder.name());
									SchematicAccounter.addSchematic(holder);
								}
							}
						}
					} else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
						if (event.context() instanceof Path modifiedPath) {
							SchematicAccounter.removeSchematic(SchematicAccounter.getIdForFile(modifiedPath));
						}
					}
				}

				key.reset();
			}
		} catch (InterruptedException e) {
			Reference.logger.error("Error polling watchService", e);
		}
	};

	public static void init() {
		initWatchService();

		for (Path schematic : Reference.proxy.getAllSchematics()) {
			Reference.proxy.addSchematic(schematic);
		}
	}

	public static void initWatchService() {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			for (Path schematicDirectory : Reference.proxy.getAllSchematicDirectories()) {
				WatchKey key = schematicDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
				keyToPathMap.put(key, schematicDirectory);
			}

			TickEvent.SERVER_POST.register(listener);

		} catch (IOException e) {
			Reference.logger.warn("Error creating schematic watchdog");
			if (Platform.getEnv() == EnvType.CLIENT) {
				PlayerUtils.getClientPlayer().displayClientMessage(
						Component.translatable(Names.Messages.WATCHDOG_ERROR), false);
			}
		}
	}

	public static void reset() {
		if (watchService != null) {
			try {
				TickEvent.SERVER_POST.unregister(listener);
				watchService.close();
				watchService = null;
			} catch (IOException e) {
				Reference.logger.error("Error shutting down watchService");
			}
		}

		schematics.clear();
		keyToPathMap.clear();
	}

	/**
	 * Only whenever this side adds a schematic. Gets synced to the client/server
	 *
	 * @param holder the SchematicHolder to add
	 */
	public static void addSchematic(@NotNull SchematicHolder holder) {
		Reference.logger.info("Schematic [{}] was added", holder.name());

		UUID id = UUID.randomUUID();
		schematics.put(id, holder);
		MessageAddSchematic message = new MessageAddSchematic(id, holder.name(), holder.getFileSize(),
				holder.locationType(), holder.owner(), holder.additionalReadPlayers(),
				holder.additionalRemovePlayers());

		if (Platform.getEnv() == EnvType.CLIENT) {
			Dispatcher.sendToServer(message);
		} else {
			Dispatcher.sendToAllClients(message, ServerProxy.serverWeakReference.get());
		}
	}

	/**
	 * Only used whenever this side removes a schematic. Gets synced to the client/server
	 *
	 * @param id the SchematicHolder to remove
	 */
	public static void removeSchematic(@NotNull UUID id) {
		schematics.remove(id);
		MessageRemoveSchematic message = new MessageRemoveSchematic(id);

		if (Platform.getEnv() == EnvType.CLIENT) {
			Dispatcher.sendToServer(message);
		} else {
			Dispatcher.sendToAllClients(message, ServerProxy.serverWeakReference.get());
		}
	}

	public static @Nullable UUID getIdForFile(@NotNull Path file) {
		String name = file.getFileName().toString();
		SchematicLocation location;
		UUID owner;
		if (Platform.getEnv() == EnvType.CLIENT) {
			location = SchematicLocation.LOCAL;
			owner = PlayerUtils.getClientPlayer().getUUID();
		} else {
			Path playerSubDir = file.getParent();
			location = playerSubDir.getFileName().toString().equals("public") ? SchematicLocation.PUBLIC :
					SchematicLocation.PRIVATE;
			owner = UUID.fromString(playerSubDir.getParent().getFileName().toString());
		}

		for (Map.Entry<UUID, SchematicHolder> entry : schematics.entrySet()) {
			SchematicHolder holder = entry.getValue();
			if (holder.name().equals(name) &&
					holder.locationType() == location && holder.owner().equals(owner)) {
				return entry.getKey();
			}
		}

		SchematicHolder holder = new SchematicHolder(file, owner, new HashSet<>(), new HashSet<>());
		SchematicAccounter.addSchematic(holder);

		return getIdForFile(file);
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull @Unmodifiable Map<UUID, SchematicHolder> getSchematics() {
		return Map.copyOf(schematics);
	}

	public static SchematicHolder get(UUID id) {
		return schematics.get(id);
	}

	public static List<SchematicHolder> sorted() {
		return schematics.values().stream().sorted(Comparator.comparing(SchematicHolder::name)).toList();
	}

	public static List<SchematicHolder> sorted(Player player) {
		return schematics.values().stream().filter(holder -> FilePermission.READ.check(player.getUUID(), holder))
				.sorted(Comparator.comparing(SchematicHolder::name)).toList();
	}

	public static List<SchematicHolder> sorted(Player player, FilePermission permission) {
		return schematics.values().stream().filter(holder -> permission.check(player.getUUID(), holder))
				.sorted(Comparator.comparing(SchematicHolder::name)).toList();
	}

	public static @NotNull List<String> sortedNames() {
		List<Path> paths = sortedPaths();
		return FileNameUtils.getQualifiedFileNames(paths);
	}

	public static List<Path> sortedPaths() {
		return schematics.values().stream().sorted(Comparator.comparing(SchematicHolder::name))
				.map(SchematicHolder::getPath).toList();
	}

	public static @NotNull List<String> sortedNames(Player player) {
		List<Path> paths = sortedPaths(player);
		return FileNameUtils.getQualifiedFileNames(paths);
	}

	public static List<Path> sortedPaths(Player player) {
		return schematics.values().stream().filter(holder -> FilePermission.READ.check(player.getUUID(), holder))
				.sorted(Comparator.comparing(SchematicHolder::name)).map(SchematicHolder::getPath).toList();
	}

	public static @NotNull List<String> sortedNames(Player player, FilePermission permission) {
		List<Path> paths = sortedPaths(player, permission);
		return FileNameUtils.getQualifiedFileNames(paths);
	}

	public static List<Path> sortedPaths(Player player, FilePermission permission) {
		return schematics.values().stream().filter(holder -> permission.check(player.getUUID(), holder))
				.sorted(Comparator.comparing(SchematicHolder::name)).map(SchematicHolder::getPath).toList();
	}

	public static @NotNull Map<String, SchematicHolder> sortedSchematics(Player player) {
		return sortedSchematics(player, FilePermission.READ);
	}

	public static @NotNull Map<String, SchematicHolder> sortedSchematics(Player player, FilePermission permission) {
		List<SchematicHolder> entries = schematics.values().stream().filter(entry ->
						permission.check(player.getUUID(), entry))
				.sorted(Comparator.comparing(SchematicHolder::name)).toList();
		return FileNameUtils.getUniqueReadableStringForFile(entries, SchematicHolder::getPath);
	}

	/**
	 * Only used when the schematic has been received via the network.
	 * Otherwise, we would keep syncing the schematic back and forth between client and server
	 *
	 * @param holder the SchematicHolder to add
	 */
	public static void syncedAddSchematic(@NotNull UUID id, @NotNull SchematicHolder holder) {
		Reference.logger.info("Schematic [{}] has been received from remote", holder.name());
		schematics.put(id, holder);
	}

	/**
	 * Only used when the remove notification has been received via the network.
	 * Otherwise, we would keep syncing the notification back and forth between client and server
	 **/
	public static void syncedRemoveSchematic(@NotNull UUID id) {
		schematics.remove(id);
	}

	public static void syncAllToPlayer(ServerPlayer player) {
		for (Map.Entry<UUID, SchematicHolder> entry : schematics.entrySet()) {
			UUID id = entry.getKey();
			SchematicHolder holder = entry.getValue();
			MessageAddSchematic message = new MessageAddSchematic(id, holder.name(), holder.getFileSize(),
					holder.locationType(), holder.owner(), holder.additionalReadPlayers(),
					holder.additionalRemovePlayers());

			Dispatcher.sendToClient(message, player);
		}
	}
}
