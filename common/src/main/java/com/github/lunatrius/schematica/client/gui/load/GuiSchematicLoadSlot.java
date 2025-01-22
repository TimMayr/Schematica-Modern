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

public class GuiSchematicLoadSlot extends AbstractSelectionList<GuiSchematicLoadSlot.SchematicListEntry> {
	private final Minecraft minecraft = Minecraft.getInstance();

	private final GuiSchematicLoad guiSchematicLoad;

	private int selectedIndex = -1;
	private long lastClick = 0;

	public GuiSchematicLoadSlot(@NotNull GuiSchematicLoad guiSchematicLoad) {
		super(Minecraft.getInstance(), guiSchematicLoad.width, guiSchematicLoad.height, 16,
		      guiSchematicLoad.height - 40, 24);
		this.guiSchematicLoad = guiSchematicLoad;
	}

	@Override
	protected int getItemCount() {
		return this.getGuiSchematicLoad().getUnmodifiableSchematicFiles().size();
	}

	@Override
	protected boolean isSelectedItem(int index) {
		return index == this.getSelectedIndex();
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public GuiSchematicLoad getGuiSchematicLoad() {
		return guiSchematicLoad;
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

		GuiSchematicEntry schematic = this.getGuiSchematicLoad().getUnmodifiableSchematicFiles().get(index);
		if (schematic.isDirectory()) {
			this.getGuiSchematicLoad().changeDirectory(schematic.getName());
			this.selectedIndex = -1;
		} else {
			this.selectedIndex = index;
		}

		return true;
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

	public static class SchematicListEntry
			extends AbstractSelectionList.Entry<GuiSchematicLoadSlot.SchematicListEntry> {
		private final GuiSchematicLoadSlot parent;
		private final Component strMaterialAvailable = Component.translatable(Names.Gui.Control.MATERIAL_AVAILABLE);
		private final Component strMaterialMissing = Component.translatable(Names.Gui.Control.MATERIAL_MISSING);

		public SchematicListEntry(GuiSchematicLoadSlot parent) {
			this.parent = parent;
		}

		@Override
		public void render(@NotNull GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX,
		                   int mouseY, boolean isHovered, float partialTicks) {
			if (index < 0 || index >= parent.getGuiSchematicLoad().getUnmodifiableSchematicFiles().size()) {
				return;
			}

			GuiSchematicEntry schematic = parent.getGuiSchematicLoad().getUnmodifiableSchematicFiles().get(index);
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