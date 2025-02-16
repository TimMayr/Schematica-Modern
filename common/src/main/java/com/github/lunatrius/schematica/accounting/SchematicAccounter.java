package com.github.lunatrius.schematica.accounting;

import com.github.lunatrius.schematica.core.FileNameUtils;
import com.github.lunatrius.schematica.core.PlayerUtils;
import com.github.lunatrius.schematica.network.message.MessageAddSchematic;
import com.github.lunatrius.schematica.network.message.MessageRemoveSchematic;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
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
	private static WatchService watchService;

	public static void initWatchService() {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			for (Path schematicDirectory : Reference.proxy.getAllSchematicDirectories()) {
				schematicDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			}

			TickEvent.SERVER_POST.register(server -> {
				try {
					WatchKey key = watchService.poll(100, TimeUnit.MILLISECONDS);
					if (key != null) {
						for (WatchEvent<?> event : key.pollEvents()) {
							if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
								if (event.context() instanceof Path modifiedPath) {
									UUID owner;

									if (Platform.getEnv() == EnvType.CLIENT) {
										owner = PlayerUtils.getClientPlayer().getUUID();
									} else {
										owner = UUID.fromString(
												modifiedPath.getParent().getParent().getFileName().toString());
									}

									SchematicHolder holder = new SchematicHolder(modifiedPath, owner, new HashSet<>(),
											new HashSet<>());

									SchematicAccounter.addSchematic(holder);
								}
							} else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
								if (event.context() instanceof Path modifiedPath) {
									SchematicAccounter.removeSchematic(SchematicAccounter.getIdForFile(modifiedPath));
								}
							}
						}

						key.reset();
					}
				} catch (InterruptedException ignored) {}
			});

		} catch (IOException e) {
			Reference.logger.warn("Error creating schematic watchdog");
			if (Platform.getEnv() == EnvType.CLIENT) {
				PlayerUtils.getClientPlayer().displayClientMessage(
						Component.translatable(Names.Messages.WATCHDOG_ERROR), false);
			}
		}
	}

	/**
	 * Only whenever this side adds a schematic. Gets synced to the client/server
	 *
	 * @param holder the SchematicHolder to add
	 */
	public static void addSchematic(@NotNull SchematicHolder holder) {
		UUID id = UUID.randomUUID();
		schematics.put(id, holder);
		MessageAddSchematic message = new MessageAddSchematic(id, holder.getName(), holder.getFileSize(),
				holder.getLocationType(), holder.getOwner(), holder.getAdditionalReadPlayers(),
				holder.getAdditionalRemovePlayers());

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
			if (holder.getName().equals(name) &&
					holder.getLocationType() == location && holder.getOwner().equals(owner)) {
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
		return schematics.values().stream().sorted(Comparator.comparing(SchematicHolder::getName)).toList();
	}

	public static List<SchematicHolder> sorted(Player player) {
		return schematics.values().stream().filter(holder -> FilePermission.READ.check(player.getUUID(), holder))
				.sorted(Comparator.comparing(SchematicHolder::getName)).toList();
	}

	public static List<SchematicHolder> sorted(Player player, FilePermission permission) {
		return schematics.values().stream().filter(holder -> permission.check(player.getUUID(), holder))
				.sorted(Comparator.comparing(SchematicHolder::getName)).toList();
	}

	public static @NotNull List<String> sortedNames() {
		List<Path> paths = sortedPaths();
		return FileNameUtils.getQualifiedFileNames(paths);
	}

	private static List<Path> sortedPaths() {
		return schematics.values().stream().sorted(Comparator.comparing(SchematicHolder::getName))
				.map(SchematicHolder::getPath).toList();
	}

	public static @NotNull List<String> sortedNames(Player player) {
		List<Path> paths = sortedPaths(player);
		return FileNameUtils.getQualifiedFileNames(paths);
	}

	private static List<Path> sortedPaths(Player player) {
		return schematics.values().stream().filter(holder -> FilePermission.READ.check(player.getUUID(), holder))
				.sorted(Comparator.comparing(SchematicHolder::getName)).map(SchematicHolder::getPath).toList();
	}

	public static @NotNull List<String> sortedNames(Player player, FilePermission permission) {
		List<Path> paths = sortedPaths(player, permission);
		return FileNameUtils.getQualifiedFileNames(paths);
	}

	private static List<Path> sortedPaths(Player player, FilePermission permission) {
		return schematics.values().stream().filter(holder -> permission.check(player.getUUID(), holder))
				.sorted(Comparator.comparing(SchematicHolder::getName)).map(SchematicHolder::getPath).toList();
	}

	/**
	 * Only used when the schematic has been received via the network.
	 * Otherwise, we would keep syncing the schematic back and forth between client and server
	 *
	 * @param holder the SchematicHolder to add
	 */
	public static void syncedAddSchematic(@NotNull UUID id, SchematicHolder holder) {
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
			MessageAddSchematic message = new MessageAddSchematic(id, holder.getName(), holder.getFileSize(),
					holder.getLocationType(), holder.getOwner(), holder.getAdditionalReadPlayers(),
					holder.getAdditionalRemovePlayers());

			Dispatcher.sendToClient(message, player);
		}
	}
}
