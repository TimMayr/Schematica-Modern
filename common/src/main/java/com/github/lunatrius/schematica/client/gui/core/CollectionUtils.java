package com.github.lunatrius.schematica.client.gui.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class CollectionUtils {
	public static <E> @NotNull Set<E> setFromArray(E[] array) {
		Set<E> set = new HashSet<>();
		Collections.addAll(set, array);
		return set;
	}

	public static <E> @Unmodifiable @NotNull Set<E> unmodifiableSetFromArray(E[] array) {
		Set<E> set = new HashSet<>();
		Collections.addAll(set, array);
		return Set.copyOf(set);
	}
}
