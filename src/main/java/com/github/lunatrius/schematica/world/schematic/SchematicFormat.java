package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.event.PostSchematicCaptureEvent;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicFormat {
	// LinkedHashMap to ensure defined iteration order
	public static final Map<String, SchematicFormat> FORMATS = new LinkedHashMap<>();
	public static final String FORMAT_DEFAULT;

	static {
		FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());
		//noinspection StaticInitializerReferencesSubClass
		FORMATS.put(Names.NBT.FORMAT_STRUCTURE, new SchematicStructure());

		FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;
	}

	public static ISchematic readFromFile(File directory, String filename) {
		return readFromFile(new File(directory, filename));
	}

	public static ISchematic readFromFile(File file) {
		try {
			CompoundNBT tagCompound = SchematicUtil.readTagCompoundFromFile(file);
			String format = tagCompound.getString(Names.NBT.FORMAT);
			SchematicFormat schematicFormat = FORMATS.get(format);

			if (schematicFormat == null) {
				throw new UnsupportedFormatException(format);
			}

			return schematicFormat.readFromNBT(tagCompound);
		} catch (Exception ex) {
			Reference.logger.error("Failed to read schematic!", ex);
		}

		return null;
	}

	public abstract ISchematic readFromNBT(CompoundNBT tagCompound);

	/**
	 * Writes the given schematic.
	 *
	 * @param directory
	 * 		The directory to write in
	 * @param filename
	 * 		The filename (including the extension) to write to
	 * @param format
	 * 		The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
	 * @param schematic
	 * 		The schematic to write
	 *
	 * @return True if successful
	 */
	public static boolean writeToFile(File directory, String filename, @Nullable String format, ISchematic schematic) {
		return writeToFile(new File(directory, filename), format, schematic);
	}

	/**
	 * Writes the given schematic.
	 *
	 * @param file
	 * 		The file to write to
	 * @param format
	 * 		The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
	 * @param schematic
	 * 		The schematic to write
	 *
	 * @return True if successful
	 */
	public static boolean writeToFile(File file, @Nullable String format, ISchematic schematic) {
		try {
			if (format == null) {
				format = FORMAT_DEFAULT;
			}

			if (!FORMATS.containsKey(format)) {
				throw new UnsupportedFormatException(format);
			}

			PostSchematicCaptureEvent event = new PostSchematicCaptureEvent(schematic);
			NeoForge.EVENT_BUS.post(event);

			CompoundNBT tagCompound = new CompoundNBT();

			FORMATS.get(format).writeToNBT(tagCompound, schematic);

			try (DataOutputStream dataOutputStream = new DataOutputStream(
					new GZIPOutputStream(Files.newOutputStream(file.toPath())))) {
				tagCompound.write(dataOutputStream);
			}

			return true;
		} catch (Exception ex) {
			Reference.logger.error("Failed to write schematic!", ex);
		}

		return false;
	}

	public abstract void writeToNBT(CompoundNBT tagCompound, ISchematic schematic);

	/**
	 * Writes the given schematic, notifying the player when finished.
	 *
	 * @param file
	 * 		The file to write to
	 * @param format
	 * 		The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
	 * @param schematic
	 * 		The schematic to write
	 * @param player
	 * 		The player to notify
	 */
	public static void writeToFileAndNotify(File file, @Nullable String format, ISchematic schematic,
	                                        PlayerEntity player) {
		boolean success = writeToFile(file, format, schematic);
		String message = success ? Names.Command.Save.Message.SAVE_SUCCESSFUL : Names.Command.Save.Message.SAVE_FAILED;
		player.sendMessage(new TranslationTextComponent(message, file.getName()));
	}

	/**
	 * Gets a schematic format name translation key for the given format ID.
	 * <p>
	 * If an invalid format is chosen, logs a warning and returns a key stating
	 * that it's invalid.
	 *
	 * @param format
	 * 		The format.
	 */
	public static String getFormatName(String format) {
		if (!FORMATS.containsKey(format)) {
			Reference.logger.warn("No format with id {}; returning invalid for name", format,
			                      new UnsupportedFormatException(format).fillInStackTrace());
			return Names.Formats.INVALID;
		}
		return FORMATS.get(format).getName();
	}

	/**
	 * Gets the translation key used for this format.
	 */
	public abstract String getName();

	/**
	 * Gets the extension used by the given format.
	 * <p>
	 * If the format is invalid, returns the default format's extension.
	 *
	 * @param format
	 * 		The format (or null to use {@link #FORMAT_DEFAULT the default}).
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