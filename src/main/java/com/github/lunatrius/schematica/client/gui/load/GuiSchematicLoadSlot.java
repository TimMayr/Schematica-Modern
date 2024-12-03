package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.core.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import org.apache.commons.io.FilenameUtils;

public class GuiSchematicLoadSlot extends GuiSlot {
	private final Minecraft minecraft = Minecraft.getMinecraft();

	private final GuiSchematicLoad guiSchematicLoad;

	protected int selectedIndex = -1;
	private long lastClick = 0;

	public GuiSchematicLoadSlot(GuiSchematicLoad guiSchematicLoad) {
		super(Minecraft.getMinecraft(), guiSchematicLoad.width, guiSchematicLoad.height, 16,
		      guiSchematicLoad.height - 40, 24);
		this.guiSchematicLoad = guiSchematicLoad;
	}

	@Override
	protected int getSize() {
		return this.guiSchematicLoad.schematicFiles.size();
	}

	@Override
	protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
		boolean ignore = Minecraft.getSystemTime() - this.lastClick < 500;
		this.lastClick = Minecraft.getSystemTime();

		if (ignore) {
			return;
		}

		GuiSchematicEntry schematic = this.guiSchematicLoad.schematicFiles.get(slotIndex);
		if (schematic.isDirectory()) {
			this.guiSchematicLoad.changeDirectory(schematic.getName());
			this.selectedIndex = -1;
		} else {
			this.selectedIndex = slotIndex;
		}
	}

	@Override
	protected boolean isSelected(int index) {
		return index == this.selectedIndex;
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	protected void drawContainerBackground(Tessellator tessellator) {
	}

	@Override
	protected void drawSlot(int index, int x, int y, int par4, int mouseX, int mouseY, float partialTicks) {
		if (index < 0 || index >= this.guiSchematicLoad.schematicFiles.size()) {
			return;
		}

		GuiSchematicEntry schematic = this.guiSchematicLoad.schematicFiles.get(index);
		String schematicName = schematic.getName();

		if (schematic.isDirectory()) {
			schematicName += "/";
		} else {
			schematicName = FilenameUtils.getBaseName(schematicName);
		}

		GuiHelper.drawItemStackWithSlot(this.minecraft.renderEngine, schematic.getItemStack(), x, y);

		this.guiSchematicLoad.drawString(this.minecraft.fontRenderer, schematicName, x + 24, y + 6, 0x00FFFFFF);
	}
}
