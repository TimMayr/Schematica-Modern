package com.github.lunatrius.schematica.world.schematic.format;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicDimensions;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.core.CommonNbtUtils;
import com.github.lunatrius.schematica.core.PlatformUtils;
import com.github.lunatrius.schematica.proxy.CommonProxy;
import com.github.lunatrius.schematica.proxy.PlatformProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import com.github.lunatrius.schematica.world.schematic.UnsupportedFormatException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicFormat {
	// LinkedHashMap to ensure defined iteration order
	public static final Map<String, SchematicFormat> FORMATS = new LinkedHashMap<>();
	public static final StreamCodec<FriendlyByteBuf, SchematicFormat> STREAM_CODEC =
			StreamCodec.of((buf, format) -> buf.writeUtf(format.getNbtName()),
					(buf) -> SchematicFormat.getFormatFromName(buf.readUtf()));
	public static final String FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;

	public static @Nullable ISchematic readSchematic(SchematicMetadata meta, Level level) {
		try {
			Path path = Reference.proxy.resolveSchematic(meta);

			CompoundTag tag = SchematicUtil.readTagCompoundFromFile(path);
			SchematicFormat format = SchematicFormat.getFormatFromNbt(tag);

			if (format == null) {
				throw new UnsupportedFormatException(format.getNbtName());
			}

			return format.readFromNbt(tag, level);
		} catch (Exception ex) {
			Reference.logger.error("Failed to read schematic!", ex);
		}

		return null;
	}

	public static @Nullable SchematicFormat getFormatFromNbt(@NotNull CompoundTag tag) {
		if (tag.contains(Names.NBT.METADATA)) {
			CompoundTag meta = tag.getCompound(Names.NBT.METADATA);
			if (meta.contains(Names.NBT.FORMAT)) {
				return SchematicFormat.getFormatFromName(meta.getString(Names.NBT.FORMAT));
			}
		}

		return null;
	}

	/**
	 * gets Format's proper name
	 */
	public abstract String getNbtName();

	public abstract ISchematic readFromNbt(CompoundTag tagCompound, Level level);

	/**
	 * Gets a SchematicFormat from its proper name
	 */
	public static SchematicFormat getFormatFromName(String format) {
		if (!FORMATS.containsKey(format)) {
			Reference.logger.warn("No format with id {}; returning invalid for name", format,
					new UnsupportedFormatException(format).fillInStackTrace());
			throw new UnsupportedFormatException(format);
		}
		return FORMATS.get(format);
	}

	protected static @NotNull SchematicMetadata defaultMetaFromTag(@NotNull CompoundTag tag) {
		String name = tag.getString(Names.NBT.TITLE);
		SchematicFormat format = SchematicFormat.getFormatFromName(tag.getString(Names.NBT.FORMAT));
		UUID owner = tag.getUUID(Names.NBT.AUTHOR);
		Map<UUID, List<FilePermission>> permissions = CommonNbtUtils.deserializeMap(tag, CommonCodecs.UUID,
				CommonCodecs.LIST(CommonCodecs.ENUM(FilePermission.class)));

		CompoundTag dimensions = tag.getCompound(Names.NBT.DIMENSIONS);
		int width = dimensions.getInt(Names.NBT.WIDTH);
		int height = dimensions.getInt(Names.NBT.HEIGHT);
		int length = dimensions.getInt(Names.NBT.LENGTH);

		boolean isPrivate = tag.getBoolean(Names.NBT.VISIBILITY);
		UUID id = tag.getUUID(Names.NBT.ID);
		ItemStack icon = CommonNbtUtils.deserializeItemStack(tag, Names.NBT.ICON);
		long filesize = tag.getLong(Names.NBT.FILESIZE);
		Instant lastEdited = CommonNbtUtils.deserializeInstant(tag);

		return new SchematicMetadata(name, owner, permissions, format,
				new SchematicDimensions(width, height, length),
				icon, id, filesize, lastEdited, isPrivate);
	}

	protected static @NotNull CompoundTag defaultMetaAsTag(@NotNull SchematicMetadata metadata) {
		CompoundTag tag = new CompoundTag();
		tag.putString(Names.NBT.TITLE, metadata.name());
		tag.putString(Names.NBT.FORMAT, metadata.schematicFormat().getNbtName());
		tag.putUUID(Names.NBT.AUTHOR, metadata.owner());
		tag.put(Names.NBT.PLAYER_PERMISSIONS, CommonNbtUtils.serializeMap(metadata.permissions(),
				CommonCodecs.UUID, CommonCodecs.LIST(CommonCodecs.ENUM(FilePermission.class))));

		CompoundTag dimensions = new CompoundTag();
		dimensions.putInt(Names.NBT.WIDTH, metadata.dimensions().width());
		dimensions.putInt(Names.NBT.HEIGHT, metadata.dimensions().height());
		dimensions.putInt(Names.NBT.LENGTH, metadata.dimensions().length());
		tag.put(Names.NBT.DIMENSIONS, dimensions);

		CompoundTag icon = new CompoundTag();
		metadata.icon().save(Reference.proxy.getRegistryAccess(), icon);
		tag.put(Names.NBT.ICON, icon);

		tag.putBoolean(Names.NBT.VISIBILITY, metadata.isPrivate());
		tag.putUUID(Names.NBT.ID, metadata.id());

		tag.putLong(Names.NBT.FILESIZE, metadata.filesize());
		tag.put(Names.NBT.LAST_EDITED, CommonNbtUtils.serializeInstant(metadata.lastEdited()));
		return tag;
	}

	/**
	 * Writes the given schematic, notifying the player when finished.
	 *
	 * @param schematic The schematic to write
	 * @param player    The player to notify
	 */
	public static void writeToFileAndNotify(ISchematic schematic, @NotNull Player player) {
		boolean success = writeToFile(schematic);
		String message = success ? Names.Command.Save.Message.SAVE_SUCCESSFUL : Names.Command.Save.Message.SAVE_FAILED;
		player.displayClientMessage(Component.translatable(message, schematic.getMetadata().name()), false);
	}

	/**
	 * Writes the given schematic.
	 *
	 * @param schematic The schematic to write
	 * @return True if successful
	 */
	public static boolean writeToFile(ISchematic schematic) {
		try {
			Path file = Reference.proxy.getSchematicDirectory();

			if (PlatformUtils.isPlatformServer()) {
				file = file.resolve(schematic.getMetadata().owner().toString());
			}

			file = file.resolve(schematic.getName());
			CommonProxy.recentlyAdded.add(file);

			if (schematic.getMetadata().schematicFormat() == null) {
				schematic.setMetadata(schematic.getMetadata().withFormat(SchematicFormat.getFormatFromName(FORMAT_DEFAULT)));
			}

			String format = schematic.getMetadata().schematicFormat().getNbtName();

			if (!FORMATS.containsKey(format)) {
				throw new UnsupportedFormatException(format);
			}

			PlatformProxy.createAndPostPostSchematicCaptureEvent(schematic);

			CompoundTag tagCompound = new CompoundTag();

			FORMATS.get(format).writeToNBT(tagCompound, schematic);

			try (DataOutputStream dataOutputStream = new DataOutputStream(
					new GZIPOutputStream(Files.newOutputStream(file)))) {
				tagCompound.write(dataOutputStream);
				PlatformProxy.createAndPostPostSchematicSaveEvent(file);
			}

			SchematicFormat.writeMetaToFile(file, schematic.getMetadata().withFilesize(Files.size(file)));
			Reference.proxy.addSchematic(file);
			Path finalFile = file;
			CommonProxy.scheduler.schedule(() -> CommonProxy.recentlyAdded.remove(finalFile), 200,
					TimeUnit.MILLISECONDS);

			return true;
		} catch (Exception ex) {
			Reference.logger.error("Failed to write schematic!", ex);
		}

		return false;
	}

	public abstract void writeToNBT(CompoundTag tagCompound, ISchematic schematic);

	public static void writeMetaToFile(Path path, SchematicMetadata meta) {
		try {
			CompoundTag tag = SchematicUtil.readTagCompoundFromFile(path);
			SchematicFormat format = SchematicFormat.getFormatFromNbt(tag);
			format.writeMetadataToNBT(tag, meta);
			CompoundTag wrapper = new CompoundTag();
			wrapper.put(Names.NBT.ROOT, tag);

			try (DataOutputStream dataOutputStream = new DataOutputStream(
					new GZIPOutputStream(Files.newOutputStream(path)))) {
				wrapper.write(dataOutputStream);
			}
		} catch (IOException e) {
			Reference.logger.error("Error writing metadata to file");
		}
	}

	public abstract void writeMetadataToNBT(@NotNull CompoundTag tagCompound, @NotNull SchematicMetadata metadata);

	/**
	 * Gets a schematic format name translation key for the given proper format name.
	 * <p>
	 * If an invalid format is chosen, logs a warning and returns a key stating
	 * that it's invalid.
	 *
	 * @param format The format.
	 */
	public static String getFormatName(String format) {
		if (!FORMATS.containsKey(format)) {
			Reference.logger.warn("No format with id {}; returning invalid for name", format,
					new UnsupportedFormatException(format).fillInStackTrace());
			return Names.Formats.INVALID;
		}

		return FORMATS.get(format).getName();
	}

	public abstract String getName();

	/**
	 * Gets the extension used by the given format.
	 * <p>
	 * If the format is invalid, returns the default format's extension.
	 *
	 * @param format The format (or null to use {@link #FORMAT_DEFAULT the default}).
	 */
	public static String getExtension(@Nullable String format) {
		if (format == null) {
			format = FORMAT_DEFAULT;
		}

		if (!FORMATS.containsKey(format)) {
			Reference.logger.warn("No format with id {}; returning default extension", format,
					new UnsupportedFormatException(format).fillInStackTrace());
			format = FORMAT_DEFAULT;
		}

		return FORMATS.get(format).getExtension();
	}

	/**
	 * Gets the file extension used for this format, including the leading dot.
	 */
	public abstract String getExtension();

	public static @Nullable SchematicMetadata readMetaFromFile(Path path) {
		try {
			CompoundTag tag = SchematicUtil.readTagCompoundFromFile(path);
			SchematicFormat format = SchematicFormat.getFormatFromNbt(tag);
			return format.readMetaFromNbt(tag);
		} catch (IOException e) {
			return null;
		}
	}

	public abstract SchematicMetadata readMetaFromNbt(CompoundTag tagCompound);

	public abstract @NotNull SchematicMetadata metaFromTag(@NotNull CompoundTag tag);

	public abstract @NotNull CompoundTag metaAsTag(@NotNull SchematicMetadata metadata);
}