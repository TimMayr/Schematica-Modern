package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.core.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.SlotGui;
import net.minecraft.client.renderer.Tessellator;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;

public class GuiSchematicLoadSlot extends SlotGui {
	private final Minecraft minecraft = Minecraft.getInstance();

	private final GuiSchematicLoad guiSchematicLoad;

	protected int selectedIndex = -1;
	private long lastClick = 0;

	public GuiSchematicLoadSlot(GuiSchematicLoad guiSchematicLoad) {
		super(Minecraft.getInstance(), guiSchematicLoad.width, guiSchematicLoad.height, 16,
		      guiSchematicLoad.height - 40, 24);
		this.guiSchematicLoad = guiSchematicLoad;
	}

	@Override
	protected int getItemCount() {
		return this.guiSchematicLoad.schematicFiles.size();
	}

	@Override
	protected boolean isSelectedItem(int index) {
		return index == this.selectedIndex;
	}

	@Override
	protected void renderBackground() {
	}

	@Override
	protected void renderItem(int index, int x, int y, int par4, int mouseX, int mouseY, float partialTicks) {
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

		GuiHelper.drawItemStackWithSlot(this.minecraft.textureManager, schematic.getItemStack(), x, y);

		this.guiSchematicLoad.drawString(this.minecraft.fontRenderer, schematicName, x + 24, y + 6, 0x00FFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean ignore = System.nanoTime() - this.lastClick < 500;
		this.lastClick = System.nanoTime();
		int index = this.getItemAtPosition(mouseX, mouseY);

		if (ignore || index == -1) {
			return true;
		}

		GuiSchematicEntry schematic = this.guiSchematicLoad.schematicFiles.get(index);
		if (schematic.isDirectory()) {
			this.guiSchematicLoad.changeDirectory(schematic.getName());
			this.selectedIndex = -1;
		} else {
			this.selectedIndex = index;
		}

		return true;
	}

	@Override
	protected void drawContainerBackground(@Nonnull Tessellator tessellator) {
	}
}