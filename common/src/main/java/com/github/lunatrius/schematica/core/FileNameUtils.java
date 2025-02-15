package com.github.lunatrius.schematica.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileNameUtils {
	public static @NotNull List<String> getFileNamesWithDirectories(@NotNull List<File> files) {
		Map<String, Integer> nameCount = new HashMap<>();
		List<String> result = new ArrayList<>();

		for (File file : files) {
			String name = file.getName();
			nameCount.put(name, nameCount.getOrDefault(name, 0) + 1);
		}

		for (File file : files) {
			String name = file.getName();
			if (nameCount.get(name) > 1) {
				result.add(file.getParent() + File.separator + name);
			} else {
				result.add(name);
			}
		}

		return result;
	}

	public static @Nullable File getFileByName(@NotNull List<File> files, String filename) {
		Map<String, Integer> nameCount = new HashMap<>();
		Map<String, File> result = new HashMap<>();

		for (File file : files) {
			String name = file.getName();
			nameCount.put(name, nameCount.getOrDefault(name, 0) + 1);
		}

		for (File file : files) {
			String name = file.getName();
			if (nameCount.get(name) > 1) {
				result.put(file.getParent() + File.separator + name, file);
			} else {
				result.put(name, file);
			}
		}

		return result.get(filename);
	}
}