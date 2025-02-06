package com.github.lunatrius.schematica.client.gui.load;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class GuiSchematicEntry {
	private final String name;
	private final ItemStack itemStack;
	private final boolean isDirectory;

	public GuiSchematicEntry(String name, @NotNull ItemStack itemStack, boolean isDirectory) {
		this(name, itemStack.getItem(), isDirectory);
	}

	public GuiSchematicEntry(String name, Item item, boolean isDirectory) {
		this.name = name;
		this.isDirectory = isDirectory;
		this.itemStack = new ItemStack(item, 1);
	}

	public GuiSchematicEntry(String name, Block block, boolean isDirectory) {
		this.name = name;
		this.isDirectory = isDirectory;
		this.itemStack = new ItemStack(block, 1);
	}

	public String getName() {
		return this.name;
	}

	public Item getItem() {
		return this.itemStack.getItem();
	}

	public boolean isDirectory() {
		return this.isDirectory;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}
}