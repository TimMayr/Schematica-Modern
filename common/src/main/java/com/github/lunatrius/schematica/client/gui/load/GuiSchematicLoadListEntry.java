package com.github.lunatrius.schematica.client.gui.load;


import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public class GuiSchematicLoadListEntry extends ObjectSelectionList.Entry<GuiSchematicLoadListEntry> {
	private final String name;
	private final ItemStack itemStack;
	private final boolean isDirectory;
	private final GuiSchematicLoadList parent;

	public GuiSchematicLoadListEntry(String name, @NotNull ItemStack itemStack, boolean isDirectory,
	                                 GuiSchematicLoadList parent) {
		this(name, itemStack.getItem(), isDirectory, parent);
	}

	public GuiSchematicLoadListEntry(String name, Item item, boolean isDirectory, GuiSchematicLoadList parent) {
		this.name = name;
		this.isDirectory = isDirectory;
		this.itemStack = new ItemStack(item, 1);
		this.parent = parent;
	}

	public GuiSchematicLoadListEntry(String name, @NotNull Block block, boolean isDirectory,
	                                 GuiSchematicLoadList parent) {
		this(name, block.asItem(), isDirectory, parent);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int index, int left, int top, int width, int height,
	                   int mouseX, int mouseY, boolean isHovered, float partialTicks) {
		String schematicName = name;

		if (isDirectory()) {
			schematicName += "/";
		} else {
			schematicName = FilenameUtils.getBaseName(schematicName);
		}

		GuiHelperTest.drawItemStackWithSlot(guiGraphics, getItemStack(), top, left);
		guiGraphics.drawString(parent.getMinecraft().font, schematicName, top + 24, left + 6, 0x00FFFFFF);
	}

	public boolean isDirectory() {
		return this.isDirectory;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}

	public String getName() {
		return this.name;
	}

	public Item getItem() {
		return this.itemStack.getItem();
	}

	@Override
	public Component getNarration() {
		return Component.literal(name);
	}
}