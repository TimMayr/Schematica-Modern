package com.github.lunatrius.schematica.accounting;

import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.core.PlatformUtils;
import com.github.lunatrius.schematica.core.PlayerUtils;
import com.github.lunatrius.schematica.network.message.accounting.MessageAddSchematic;
import com.github.lunatrius.schematica.network.message.accounting.MessageRemoveSchematic;
import com.github.lunatrius.schematica.proxy.CommonProxy;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import commonnetwork.api.Dispatcher;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class is responsible for keeping the client and the server synced when it comes to Schematic Metadata.
 * Without it, the server wouldn't know which schematics the client has locally, and the client would be able to load
 * schematics stored on the server.
 */
public class SchematicAccounter {
	private static final Map<UUID, SchematicHolder> schematics = new HashMap<>();
	private static final Map<Path, SchematicHolder> localSchematics = new HashMap<>();
	private static final FileFilterSchematic FILTER_SCHEMATIC = new FileFilterSchematic(false);
	private static final Map<WatchKey, Path> keyToPathMap = new HashMap<>();
	private static WatchService watchService;
	private static final TickEvent.Server listener = (server) -> {
		try {
			WatchKey key = watchService.poll(100, TimeUnit.MILLISECONDS);
			if (key != null) {
				Path schematicDir = keyToPathMap.get(key);

				for (WatchEvent<?> event : key.pollEvents()) {
					if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
						if (event.context() instanceof Path relativePath) {
							Path fullPath = schematicDir.resolve(relativePath).toAbsolutePath().normalize();

							if (Files.isDirectory(fullPath)) {
								try {
									registerAll(fullPath);
								} catch (IOException e) {
									Reference.logger.warn("Unable to create watchservice for new folder [{}]",
											fullPath);
								}
							}

							if (FILTER_SCHEMATIC.accept(fullPath)) {
								//This check makes sure that the discovered schematic isn't actively getting saved by
								//some other method, as that would duplicate the entry
								if (!CommonProxy.recentlyAdded.contains(fullPath)) {
									try {
										SchematicMetadata metadata = SchematicFormat.readMetaFromFile(fullPath);
										Reference.logger.info("Schematic [{}] was discovered by watchService",
												metadata.name());
										SchematicAccounter.addSchematic(new SchematicHolder(metadata,
												SchematicLocation.LOCAL), false);
									} catch (Exception e) {
										Reference.logger.error("A file was created in the schematic directory, but " +
												"something went wrong");
									}
								}
							}
						}
					} else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
						if (event.context() instanceof Path relativePath) {
							Path fullPath = schematicDir.resolve(relativePath).toAbsolutePath().normalize();

							//This check makes sure that the discovered schematic isn't actively getting removed by
							//some other method, as that would remove the entry twice
							if (!CommonProxy.recentlyRemoved.contains(fullPath)) {
								try {
									SchematicAccounter.removeSchematic(localSchematics.get(fullPath).metadata().id(),
											false);
								} catch (Exception e) {
									Reference.logger.error("A file was deleted in the schematic directory, but " +
											"something went wrong");
								}
							}
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

		for (Path schematic : Reference.proxy.getAllLocalSchematics()) {
			Reference.proxy.addSchematic(schematic);
		}
	}

	public static void initWatchService() {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			registerAll(Reference.proxy.getSchematicDirectory());
			TickEvent.SERVER_POST.register(listener);
		} catch (IOException e) {
			Reference.logger.warn("Error creating schematic watchdog");
			if (PlatformUtils.isPlatformClient()) {
				PlayerUtils.getClientPlayer().displayClientMessage(
						Component.translatable(Names.Messages.WATCHDOG_ERROR), false);
			}
		}
	}

	private static void registerAll(Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<>() {
			@Override
			public @NotNull FileVisitResult preVisitDirectory(Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void register(@NotNull Path dir) throws IOException {
		WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		keyToPathMap.put(key, dir);
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

	public static void addSchematic(@NotNull SchematicHolder holder, boolean synced) {
		Reference.logger.info("Schematic [{}] was added", holder.metadata().name());
		schematics.put(holder.metadata().id(), holder);
		localSchematics.put(getPathForSchematic(holder), holder);

		if (!synced) {
			MessageAddSchematic message = new MessageAddSchematic(holder.metadata(), true);

			if (PlatformUtils.isPlatformClient()) {
				Dispatcher.sendToServer(message);
			} else {
				Dispatcher.sendToAllClients(message, ServerProxy.serverWeakReference.get());
			}
		}
	}

	private static @Nullable Path getPathForSchematic(@NotNull SchematicHolder holder) {
		if (holder.location() == SchematicLocation.LOCAL) {
			List<Path> localPaths = Reference.proxy.getAllLocalSchematics();
			Map<SchematicHolder, Path> localHolderPaths = localPaths.stream()
					.collect(Collectors.toMap(
							path -> SchematicAccounter.get(SchematicFormat.readMetaFromFile(path).id()),
							p -> p, (x, y) -> y,
							LinkedHashMap::new));

			return localHolderPaths.get(holder);
		} else {
			return null;
		}
	}

	public static SchematicHolder get(UUID id) {
		return schematics.get(id);
	}

	public static void removeSchematic(@NotNull UUID id, boolean synced) {
		schematics.remove(id);

		if (!synced) {
			MessageRemoveSchematic message = new MessageRemoveSchematic(id, true);

			if (PlatformUtils.isPlatformClient()) {
				Dispatcher.sendToServer(message);
			} else {
				Dispatcher.sendToAllClients(message, ServerProxy.serverWeakReference.get());
			}
		}
	}

	public static @Nullable UUID getIdForFile(@NotNull Path path) {
		return SchematicFormat.readMetaFromFile(path).id();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull @Unmodifiable Map<UUID, SchematicHolder> getSchematics() {
		return Map.copyOf(schematics);
	}

	public static List<SchematicHolder> sorted(Player player) {
		return schematics.values().stream().filter(h -> h.checkPermission(player.getUUID(), FilePermission.READ))
				.sorted(Comparator.comparing(h -> h.metadata().name())).toList();
	}

	public static @NotNull List<String> sortedNames() {
		return sorted().stream().map(h -> h.metadata().name()).toList();
	}

	public static List<SchematicHolder> sorted() {
		return schematics.values().stream().sorted(Comparator.comparing(h -> h.metadata().name())).toList();
	}

	public static @NotNull List<String> sortedNames(Player player) {
		return sortedNames(player, FilePermission.READ);
	}

	public static @NotNull List<String> sortedNames(Player player, FilePermission permission) {
		return sorted(player, permission).stream().map(h -> h.metadata().name()).toList();
	}

	public static List<SchematicHolder> sorted(Player player, FilePermission permission) {
		return schematics.values().stream().filter(h -> h.checkPermission(player.getUUID(), permission))
				.sorted(Comparator.comparing(h -> h.metadata().name())).toList();
	}

	public static @NotNull Map<String, SchematicHolder> sortedSchematics(Player player) {
		return sortedSchematics(player, FilePermission.READ);
	}

	public static @NotNull Map<String, SchematicHolder> sortedSchematics(Player player, FilePermission permission) {
		List<SchematicHolder> entries = schematics.values().stream().filter(h ->
						h.checkPermission(player.getUUID(), permission))
				.sorted(Comparator.comparing(h -> h.metadata().name())).toList();
		return SchematicHolder.qualify(entries);
	}

	public static void syncAllToPlayer(ServerPlayer player) {
		for (Map.Entry<UUID, SchematicHolder> entry : schematics.entrySet()) {
			SchematicHolder holder = entry.getValue();
			MessageAddSchematic message = new MessageAddSchematic(holder.metadata(), true);

			Dispatcher.sendToClient(message, player);
		}
	}

	public static boolean hasLocal(SchematicHolder holder) {
		for (SchematicHolder value : schematics.values()) {
			if (value.location() == SchematicLocation.LOCAL && value.metadata().id().equals(holder.metadata().id())) {
				return true;
			}
		}

		return false;
	}
}
