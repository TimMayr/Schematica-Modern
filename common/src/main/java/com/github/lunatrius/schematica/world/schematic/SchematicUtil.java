package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.core.CommonNbtUtils;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SchematicUtil {

	public static @NotNull ItemStack getIconFromName(@Nullable String iconName) {
		if (iconName == null) {
			Constants.Schematic.DEFAULT_ICON.copy();
		}

		ResourceLocation rl = null;

		String[] parts = iconName.split(",");
		if (parts.length >= 1) {
			rl = ResourceLocation.parse(parts[0]);
		}

		if (rl == null) {
			return Constants.Schematic.DEFAULT_ICON.copy();
		}

		ItemStack block = new ItemStack(BuiltInRegistries.BLOCK.getValue(rl), 1);
		if (!block.isEmpty()) {
			return block;
		}

		ItemStack item = new ItemStack(BuiltInRegistries.ITEM.getValue(rl), 1);
		if (!item.isEmpty()) {
			return item;
		}

		return Constants.Schematic.DEFAULT_ICON.copy();
	}

	public static ItemStack getIconFromFile(Path file) {
		try {
			return CommonNbtUtils.deserializeItemStack(readTagCompoundFromFile(file), Names.NBT.ICON);
		} catch (Exception e) {
			Reference.logger.error("Failed to read schematic icon!", e);
		}

		return Constants.Schematic.DEFAULT_ICON.copy();
	}

	public static CompoundTag readTagCompoundFromFile(Path file) throws IOException {
		try {
			return NbtIo.readCompressed(Files.newInputStream(file), NbtAccounter.unlimitedHeap());
		} catch (Exception ex) {
			Reference.logger.warn("Failed compressed read, trying normal read...", ex);
			return NbtIo.read(new DataInputStream(Files.newInputStream(file)), NbtAccounter.unlimitedHeap());
		}
	}
}