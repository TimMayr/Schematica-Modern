package com.github.lunatrius.schematica.accounting;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class SchematicHolder {
	private final String name;
	private final SchematicLocation locationType;
	private final Set<UUID> additionalReadPlayers;
	private final Set<UUID> additionalRemovePlayers;
	private final UUID owner;
	private long fileSize = -1;

	public SchematicHolder(@NotNull Path rawFile, @NotNull UUID owner, Set<UUID> additionalReadPlayers,
	                       Set<UUID> additionalRemovePlayers) {
		Path file = rawFile.toAbsolutePath().normalize();
		this.name = file.getFileName().toString();
		this.locationType = file.getParent().getParent().getFileName().toString().equals(owner.toString()) ?
				file.getParent().getFileName().toString().equals("public") ?
						SchematicLocation.PUBLIC :
						SchematicLocation.PRIVATE
				: SchematicLocation.LOCAL;
		this.additionalReadPlayers = new HashSet<>(additionalReadPlayers);
		this.additionalRemovePlayers = new HashSet<>(additionalRemovePlayers);
		this.owner = owner;
		try {
			this.fileSize = Files.size(file);
		} catch (IOException ignored) {}
	}

	public SchematicHolder(@NotNull String name, long fileSize, SchematicLocation locationType, UUID owner,
	                       Set<UUID> additionalReadPlayers, Set<UUID> additionalRemovePlayers) {
		this.name = name;
		this.fileSize = fileSize;
		this.locationType = locationType;
		this.additionalReadPlayers = new HashSet<>(additionalReadPlayers);
		this.additionalRemovePlayers = new HashSet<>(additionalRemovePlayers);
		this.owner = owner;
	}

	public long getFileSize() {
		return fileSize;
	}

	public Set<UUID> getAdditionalReadPlayers() {
		return additionalReadPlayers;
	}

	public Set<UUID> getAdditionalRemovePlayers() {
		return additionalRemovePlayers;
	}

	public @NotNull Path getPath() {
		if (this.getLocationType() == SchematicLocation.LOCAL) {
			return Path.of(this.getName());
		} else {
			return Path.of(this.getOwner().toString(),
					this.getLocationType().toString().toLowerCase(Locale.ROOT)).resolve(this.getName());
		}
	}

	public SchematicLocation getLocationType() {
		return locationType;
	}

	public String getName() {
		return name;
	}

	public UUID getOwner() {
		return owner;
	}
}
