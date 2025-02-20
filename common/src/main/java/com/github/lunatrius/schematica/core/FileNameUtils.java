package com.github.lunatrius.schematica.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class FileNameUtils {
	public static @NotNull List<String> getQualifiedFileNames(@NotNull Collection<Path> files) {
		return getUniqueReadableStringForFile(files).keySet().stream().sorted().toList();
	}

	/**
	 * Used to make sure that two files with the same name can still be distinguished in guis, by prepending their
	 * parent directory
	 */
	public static @NotNull Map<String, Path> getUniqueReadableStringForFile(@NotNull Collection<Path> files) {
		return getUniqueReadableStringForFile(files, path -> path);
	}

	public static <T> @NotNull Map<String, T> getUniqueReadableStringForFile(@NotNull Collection<T> fileHolders,
	                                                                         Function<T, Path> keyExtractor) {
		Map<String, Integer> nameCount = new HashMap<>();
		Map<String, T> result = new TreeMap<>();

		for (T fileHolder : fileHolders) {
			Path file = keyExtractor.apply(fileHolder);
			String name = file.getFileName().toString();
			nameCount.put(name, nameCount.getOrDefault(name, 0) + 1);
		}

		for (T fileHolder : fileHolders) {
			Path file = keyExtractor.apply(fileHolder).toAbsolutePath().normalize();
			String name = file.getFileName().toString();
			if (nameCount.get(name) > 1) {
				result.put(file.getParent().getFileName() + "/" + name, fileHolder);
			} else {
				result.put(name, fileHolder);
			}
		}

		return result;
	}

	public static @Nullable Path getFileByName(@NotNull List<Path> files, String filename) {
		return getUniqueReadableStringForFile(files).get(filename);
	}
}