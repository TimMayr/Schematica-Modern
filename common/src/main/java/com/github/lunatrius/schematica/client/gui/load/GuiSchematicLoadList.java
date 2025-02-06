package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.core.client.gui.GuiHelper;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public class GuiSchematicLoadList extends AbstractSelectionList<GuiSchematicLoadList.SchematicLoadListSlot> {
	private final Minecraft minecraft = Minecraft.getInstance();

	private final GuiSchematicLoad parent;

	private int selectedIndex = -1;
	private long lastClick = 0;

	public GuiSchematicLoadList(@NotNull GuiSchematicLoad parent) {
		super(Minecraft.getInstance(), parent.width, parent.height, 16,
		      parent.height - 40, 24);
		this.parent = parent;
		for (int i = 0; i < this.getItemCount(); i++) {
			this.addEntry(new SchematicLoadListSlot(this));
		}
	}

	@Override
	protected int getItemCount() {
		return this.getParent().getSchematicFiles().size();
	}

	@Override
	protected boolean isSelectedItem(int index) {
		return index == this.getSelectedIndex();
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public GuiSchematicLoad getParent() {
		return parent;
	}

	public Minecraft getMinecraft() {
		return minecraft;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		super.onClick(mouseX, mouseY);
		boolean ignore = System.nanoTime() - this.lastClick < 500;
		this.lastClick = System.nanoTime();
		int index = this.getSelectedIndex();

		if (ignore || index == -1) {
			return true;
		}

		GuiSchematicEntry schematic = this.getParent().getSchematicFiles().get(index);
		if (schematic.isDirectory()) {
			this.getParent().changeDirectory(schematic.getName());
			this.selectedIndex = -1;
		} else {
			this.selectedIndex = index;
		}

		return true;
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

	public static class SchematicLoadListSlot
			extends AbstractSelectionList.Entry<SchematicLoadListSlot> {
		private final GuiSchematicLoadList parent;

		public SchematicLoadListSlot(GuiSchematicLoadList parent) {
			this.parent = parent;
		}

		@Override
		public void render(@NotNull GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX,
		                   int mouseY, boolean isHovered, float partialTicks) {
			if (index < 0 || index >= parent.getParent().getSchematicFiles().size()) {
				return;
			}

			GuiSchematicEntry schematic = parent.getParent().getSchematicFiles().get(index);
			String schematicName = schematic.getName();

			if (schematic.isDirectory()) {
				schematicName += "/";
			} else {
				schematicName = FilenameUtils.getBaseName(schematicName);
			}

			GuiHelper.drawItemStackWithSlot(parent.getMinecraft().getTextureManager(), schematic.getItemStack(), x, y);

			graphics.drawString(parent.getMinecraft().font, schematicName, x + 24, y + 6, 0x00FFFFFF);
		}
	}
}