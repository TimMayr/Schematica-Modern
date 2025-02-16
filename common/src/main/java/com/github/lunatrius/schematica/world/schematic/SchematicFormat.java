package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.proxy.PlatformProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicFormat {
	// LinkedHashMap to ensure defined iteration order
	public static final Map<String, SchematicFormat> FORMATS = new LinkedHashMap<>();
	public static final String FORMAT_DEFAULT;

	static {
		FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;
	}

	public static ISchematic readFromFile(Path directory, String filename, Level level) {
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

			return schematicFormat.readFromNBT(tagCompound, level);
		} catch (Exception ex) {
			Reference.logger.error("Failed to read schematic!", ex);
		}

		return null;
	}

	/**
	 * Writes the given schematic, notifying the player when finished.
	 *
	 * @param file      The file to write to
	 * @param format    The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
	 * @param schematic The schematic to write
	 * @param player    The player to notify
	 */
	public static void writeToFileAndNotify(Path file, @Nullable String format, ISchematic schematic,
	                                        @NotNull Player player) {
		boolean success = writeToFile(file, format, schematic);
		String message = success ? Names.Command.Save.Message.SAVE_SUCCESSFUL : Names.Command.Save.Message.SAVE_FAILED;
		player.displayClientMessage(Component.translatable(message, file.getFileName().toString()), false);
	}

	public abstract ISchematic readFromNBT(CompoundTag tagCompound, Level level);

	/**
	 * Writes the given schematic.
	 *
	 * @param file      The file to write to
	 * @param format    The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
	 * @param schematic The schematic to write
	 * @return True if successful
	 */
	public static boolean writeToFile(Path file, @Nullable String format, ISchematic schematic) {
		try {
			if (format == null) {
				format = FORMAT_DEFAULT;
			}

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
				Reference.proxy.addSchematic(file);
			}

			return true;
		} catch (Exception ex) {
			Reference.logger.error("Failed to write schematic!", ex);
		}

		return false;
	}

	/**
	 * Writes the given schematic.
	 *
	 * @param directory The directory to write in
	 * @param filename  The filename (including the extension) to write to
	 * @param format    The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
	 * @param schematic The schematic to write
	 * @return True if successful
	 */
	public static boolean writeToFile(Path directory, String filename, @Nullable String format, ISchematic schematic) {
		return writeToFile(directory.resolve(filename), format, schematic);
	}

	public abstract void writeToNBT(CompoundTag tagCompound, ISchematic schematic);

	/**
	 * Gets a SchematicFormat from its format ID
	 */
	public static SchematicFormat getFormatFromName(String format) {
		if (!FORMATS.containsKey(format)) {
			Reference.logger.warn("No format with id {}; returning invalid for name", format,
					new UnsupportedFormatException(format).fillInStackTrace());
			throw new UnsupportedFormatException(format);
		}
		return FORMATS.get(format);
	}

	/**
	 * Gets a schematic format name translation key for the given format ID.
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
	 * gets Format's proper name
	 */
	public abstract String getNbtName();

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
}