package com.github.lunatrius.schematica.util;

import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class FileFilterSchematic implements DirectoryStream.Filter<Path> {
	private final boolean acceptDirectory;

	public FileFilterSchematic(boolean dir) {
		this.acceptDirectory = dir;
	}

	@Override
	public boolean accept(Path file) {
		if (this.acceptDirectory) {
			return Files.isDirectory(file);
		}

		String extension = "." + FilenameUtils.getExtension(file.getFileName().toString().toLowerCase(Locale.ROOT));
		for (SchematicFormat format : SchematicFormat.FORMATS.values()) {
			if (format.getExtension().equals(extension)) {
				return true;
			}
		}
		return false;
	}
}
