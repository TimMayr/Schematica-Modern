package com.github.lunatrius.schematica.api;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SchematicMetadata(String name, UUID owner, Map<UUID, List<FilePermission>> permissions,
                                SchematicFormat schematicFormat, SchematicDimensions dimensions, ItemStack icon,
                                UUID id, long filesize, Instant lastEdited, boolean isPrivate) {
	public static final StreamCodec<RegistryFriendlyByteBuf, SchematicMetadata> STREAM_CODEC =
			StreamCodec.of(
					(buf, meta) -> {
						buf.writeUtf(meta.name());
						buf.writeUUID(meta.owner());
						CommonCodecs.MAP(CommonCodecs.UUID,
								CommonCodecs.LIST(CommonCodecs.ENUM(FilePermission.class))).encode(buf,
								meta.permissions);
						SchematicFormat.STREAM_CODEC.encode(buf, meta.schematicFormat());
						SchematicDimensions.STREAM_CODEC.encode(buf, meta.dimensions());
						ItemStack.STREAM_CODEC.encode(buf, meta.icon());
						buf.writeUUID(meta.id());
						buf.writeLong(meta.filesize());
						buf.writeInstant(meta.lastEdited());
						buf.writeBoolean(meta.isPrivate);
					}, buf -> {
						String name = buf.readUtf();
						UUID owner = buf.readUUID();
						Map<UUID, List<FilePermission>> map = CommonCodecs.MAP(CommonCodecs.UUID,
								CommonCodecs.LIST(CommonCodecs.ENUM(FilePermission.class))).decode(buf);
						SchematicFormat format = SchematicFormat.STREAM_CODEC.decode(buf);
						SchematicDimensions dimensions = SchematicDimensions.STREAM_CODEC.decode(buf);
						ItemStack icon = ItemStack.STREAM_CODEC.decode(buf);
						UUID id = buf.readUUID();
						long filesize = buf.readLong();
						Instant lastEdited = buf.readInstant();
						boolean isPrivate = buf.readBoolean();

						return new SchematicMetadata(name, owner, map, format, dimensions, icon, id,
								filesize, lastEdited, isPrivate);
					});

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withIcon(ItemStack newIcon) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat, this.dimensions,
				newIcon, this.id, this.filesize, this.lastEdited, this.isPrivate);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withOwner(UUID newOwner) {
		return new SchematicMetadata(this.name, newOwner, this.permissions, this.schematicFormat, this.dimensions,
				this.icon, this.id, this.filesize, this.lastEdited, this.isPrivate);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withFormat(SchematicFormat newFormat) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, newFormat, this.dimensions, this.icon,
				this.id, this.filesize, this.lastEdited, this.isPrivate);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withDimensions(SchematicDimensions newDimensions) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat,
				newDimensions, this.icon, this.id, this.filesize, this.lastEdited, this.isPrivate);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withFilesize(long newFileSize) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat, this.dimensions,
				this.icon, this.id, newFileSize, this.lastEdited, this.isPrivate);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withEdited(Instant newLastEdited) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat,
				this.dimensions, this.icon, this.id, this.filesize, newLastEdited, this.isPrivate);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withId(UUID newId) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat,
				this.dimensions, this.icon, newId, this.filesize, this.lastEdited, this.isPrivate);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata isPrivate(boolean isPrivate) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat,
				this.dimensions, this.icon, this.id, this.filesize, this.lastEdited, isPrivate);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withName(String newName) {
		return new SchematicMetadata(newName, this.owner, this.permissions, this.schematicFormat,
				this.dimensions, this.icon, this.id, this.filesize, this.lastEdited, this.isPrivate);
	}}