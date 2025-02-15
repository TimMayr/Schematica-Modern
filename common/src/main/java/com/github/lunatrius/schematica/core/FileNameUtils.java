package com.github.lunatrius.schematica.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileNameUtils {
	public static @NotNull List<String> getFileNamesWithDirectories(@NotNull List<File> files) {
		return getUniqueReadableStringForFile(files).keySet().stream().sorted().toList();
	}

	/*
	Used to make sure that two files with the same name can still be distinguished in guis, by prepending their parent
	 directory
	 */
	public static @NotNull Map<String, File> getUniqueReadableStringForFile(@NotNull List<File> files) {
		Map<String, Integer> nameCount = new HashMap<>();
		Map<String, File> result = new TreeMap<>();

		for (File file : files) {
			String name = file.getName();
			nameCount.put(name, nameCount.getOrDefault(name, 0) + 1);
		}

		for (File file : files) {
			String name = file.getName();
			if (nameCount.get(name) > 1) {
				result.put(file.getParentFile().getName() + "/" + name, file);
			} else {
				result.put(name, file);
			}
		}

		return result;
	}

	public static @Nullable File getFileByName(@NotNull List<File> files, String filename) {
		return getUniqueReadableStringForFile(files).get(filename);
	}
}