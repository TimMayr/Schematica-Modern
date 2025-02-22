package com.github.lunatrius.schematica.world.schematic.format;

import com.github.lunatrius.schematica.accounting.FilePermission;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicDimensions;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.core.CommonNbtUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
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

	public static ISchematic readFromFile(@NotNull Path directory, String filename, Level level) {
		return readFromFile(directory.resolve(filename), level);
	}

	public static @Nullable ISchematic readFromFile(Path file, Level level) {
		try {
			CompoundTag tagCompound = SchematicUtil.readTagCompoundFromFile(file);
			String format = tagCompound.getString(Names.NBT.FORMAT);
			SchematicFormat schematicFormat = FORMATS.get(format);

			if (schematicFormat == null) {
				throw new UnsupportedFormatException(format);
			}

			return schematicFormat.readFromNbt(tagCompound, level);
		} catch (Exception ex) {
			Reference.logger.error("Failed to read schematic!", ex);
		}

		return null;
	}

	public abstract ISchematic readFromNbt(CompoundTag tagCompound, Level level);

	protected static @NotNull SchematicMetadata defaultMetaFromTag(@NotNull CompoundTag tag) {
		String name = tag.getString(Names.NBT.TITLE);
		SchematicFormat format = SchematicFormat.getFormatFromName(tag.getString(Names.NBT.FORMAT));
		UUID owner = tag.getUUID(Names.NBT.AUTHOR);
		Map<UUID, List<FilePermission>> permissions = CommonNbtUtils.deserializeMap(tag, CommonCodecs.UUID,
				CommonCodecs.LIST(CommonCodecs.ENUM(FilePermission.class)));
		int width = tag.getInt(Names.NBT.WIDTH);
		int height = tag.getInt(Names.NBT.HEIGHT);
		int length = tag.getInt(Names.NBT.LENGTH);
		boolean isPrivate = tag.getBoolean(Names.NBT.VISIBILITY);
		UUID id = tag.getUUID(Names.NBT.ID);
		ItemStack icon = CommonNbtUtils.deserializeItemStack(tag, Names.NBT.ICON);

		return new SchematicMetadata(name, owner, permissions, format, new SchematicDimensions(width, height, length),
				icon, isPrivate, id);
	}

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
		return tag;
	}

	/**
	 * gets Format's proper name
	 */
	public abstract String getNbtName();

	/**
	 * Writes the given schematic, notifying the player when finished.
	 *
	 * @param file      The file to write to
	 * @param schematic The schematic to write
	 * @param player    The player to notify
	 */
	public static void writeToFileAndNotify(Path file, ISchematic schematic, @NotNull Player player) {
		boolean success = writeToFile(file, schematic);
		String message = success ? Names.Command.Save.Message.SAVE_SUCCESSFUL : Names.Command.Save.Message.SAVE_FAILED;
		player.displayClientMessage(Component.translatable(message, file.getFileName().toString()), false);
	}

	/**
	 * Writes the given schematic.
	 *
	 * @param rawPath   The file to write to
	 * @param schematic The schematic to write
	 * @return True if successful
	 */
	public static boolean writeToFile(Path rawPath, ISchematic schematic) {
		try {
			Path normalizedFile = rawPath.toAbsolutePath().normalize();

			CommonProxy.recentlyAdded.add(normalizedFile);

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
					new GZIPOutputStream(Files.newOutputStream(normalizedFile)))) {
				tagCompound.write(dataOutputStream);
				PlatformProxy.createAndPostPostSchematicSaveEvent(normalizedFile);
			}

			Reference.proxy.addSchematic(normalizedFile);
			CommonProxy.scheduler.schedule(() -> CommonProxy.recentlyAdded.remove(normalizedFile), 200,
					TimeUnit.MILLISECONDS);

			return true;
		} catch (Exception ex) {
			Reference.logger.error("Failed to write schematic!", ex);
		}

		return false;
	}

	public abstract void writeToNBT(CompoundTag tagCompound, ISchematic schematic);

	/**
	 * Writes the given schematic.
	 *
	 * @param directory The directory to write in
	 * @param filename  The filename (including the extension) to write to
	 * @param schematic The schematic to write
	 * @return True if successful
	 */
	public static boolean writeToFile(@NotNull Path directory, String filename, ISchematic schematic) {
		return writeToFile(directory.resolve(filename), schematic);
	}

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

	public abstract SchematicMetadata readMetaFromNbt(CompoundTag tagCompound);

	public abstract void writeMetadataToNBT(@NotNull CompoundTag tagCompound, @NotNull ISchematic schematic);

	public abstract @NotNull SchematicMetadata metaFromTag(@NotNull CompoundTag tag);

	public abstract @NotNull CompoundTag metaAsTag(@NotNull SchematicMetadata metadata);
}