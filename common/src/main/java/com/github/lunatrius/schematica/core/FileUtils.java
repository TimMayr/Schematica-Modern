package com.github.lunatrius.schematica.core;

import com.github.lunatrius.schematica.reference.Reference;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("LoggingSimilarMessage")
public class FileUtils {
	public static @NotNull List<Path> getAllFilesInDirectory(Path directory, DirectoryStream.Filter<Path> filter) {
		List<Path> fileList = new ArrayList<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, filter)) {
			for (Path entry : stream) {
				fileList.add(entry);
			}
		} catch (IOException ignored) {
			Reference.logger.warn("Error getting files in folder [{}]", directory.getFileName().toString());
		}

		return fileList;
	}

	public static @NotNull List<Path> getAllFilesInDirectory(Path directory) {
		List<Path> fileList = new ArrayList<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path entry : stream) {
				fileList.add(entry);
			}
		} catch (IOException ignored) {
			Reference.logger.warn("Error getting files in folder [{}]", directory.getFileName().toString());
		}

		return fileList;
	}
}