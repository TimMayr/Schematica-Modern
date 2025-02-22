package com.github.lunatrius.schematica.api;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SchematicMetadata(String name, UUID owner, Map<UUID, List<FilePermission>> permissions,
                                SchematicFormat schematicFormat, SchematicDimensions dimensions, ItemStack icon,
                                boolean isPrivate, UUID id) {
	public static final StreamCodec<RegistryFriendlyByteBuf, SchematicMetadata> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, SchematicMetadata::name,
					CommonCodecs.UUID, SchematicMetadata::owner,
					CommonCodecs.MAP(CommonCodecs.UUID, CommonCodecs.LIST(CommonCodecs.ENUM(FilePermission.class))),
					SchematicMetadata::permissions,
					SchematicFormat.STREAM_CODEC, SchematicMetadata::schematicFormat,
					SchematicDimensions.STREAM_CODEC, SchematicMetadata::dimensions,
					ItemStack.STREAM_CODEC, SchematicMetadata::icon,
					ByteBufCodecs.BOOL, SchematicMetadata::isPrivate,
					CommonCodecs.UUID, SchematicMetadata::id,
					SchematicMetadata::new);

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withIcon(ItemStack newIcon) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat, this.dimensions,
				newIcon, this.isPrivate, this.id);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withOwner(UUID newOwner) {
		return new SchematicMetadata(this.name, newOwner, this.permissions, this.schematicFormat, this.dimensions,
				this.icon, this.isPrivate, this.id);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withFormat(SchematicFormat newFormat) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, newFormat, this.dimensions, this.icon,
				this.isPrivate, this.id);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata setPrivate(boolean isPrivate) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat, this.dimensions,
				this.icon, isPrivate, this.id);
	}

	@Contract("_ -> new")
	public @NotNull SchematicMetadata withDimensions(SchematicDimensions schematicDimensions) {
		return new SchematicMetadata(this.name, this.owner, this.permissions, this.schematicFormat,
				schematicDimensions, this.icon, isPrivate, this.id);
	}
}
