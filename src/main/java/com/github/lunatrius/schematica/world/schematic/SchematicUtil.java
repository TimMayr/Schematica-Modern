package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class SchematicUtil {
	public static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.GRASS);

	public static ItemStack getIconFromName(final String iconName) {
		ResourceLocation rl = null;

		final String[] parts = iconName.split(",");
		if (parts.length >= 1) {
			rl = new ResourceLocation(parts[0]);
		}

		if (rl == null) {
			return DEFAULT_ICON.copy();
		}

		final ItemStack block = new ItemStack(ForgeRegistries.BLOCKS.getValue(rl), 1, null);
		if (!block.isEmpty()) {
			return block;
		}

		final ItemStack item = new ItemStack(ForgeRegistries.ITEMS.getValue(rl), 1, null);
		if (!item.isEmpty()) {
			return item;
		}

		return DEFAULT_ICON.copy();
	}

	public static ItemStack getIconFromFile(final File file) {
		try {
			return getIconFromNBT(readTagCompoundFromFile(file));
		} catch (final Exception e) {
			Reference.logger.error("Failed to read schematic icon!", e);
		}

		return DEFAULT_ICON.copy();
	}

	public static CompoundNBT readTagCompoundFromFile(final File file) throws IOException {
		try {
			return CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
		} catch (final Exception ex) {
			Reference.logger.warn("Failed compressed read, trying normal read...", ex);
			return CompressedStreamTools.read(file);
		}
	}

	public static ItemStack getIconFromNBT(final CompoundNBT tagCompound) {
		ItemStack icon = DEFAULT_ICON.copy();

		if (tagCompound != null && tagCompound.hasUniqueId(Names.NBT.ICON)) {
			icon.deserializeNBT(tagCompound.getCompound(Names.NBT.ICON));

			if (icon.isEmpty()) {
				icon = DEFAULT_ICON.copy();
			}
		}

		return icon;
	}
}
