package com.github.lunatrius.schematica.client.gui.load;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.NotNull;

public class GuiSchematicLoadList extends ObjectSelectionList<GuiSchematicLoadListEntry> {
	private final Minecraft minecraft = Minecraft.getInstance();

	private final GuiSchematicLoad parent;

	private long lastClick = 0;

	public GuiSchematicLoadList(@NotNull GuiSchematicLoad parent) {
		//int width, int height, int y, int itemHeight, int headerHeight
		super(Minecraft.getInstance(), parent.width, parent.height - 56, 16, 22, 0);
		this.parent = parent;
	}

	@Override
	protected int getItemCount() {
		return this.getParent().getSchematicListSlots().size();
	}

	@Override
	public int getRowTop(int index) {
		return super.getRowTop(index);
	}

	public GuiSchematicLoad getParent() {
		return parent;
	}

	public Minecraft getMinecraft() {
		return minecraft;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		boolean ignore = System.nanoTime() - this.lastClick < 500;
		this.lastClick = System.nanoTime();
		GuiSchematicLoadListEntry entry = this.getSelected();

		if (ignore || entry == null) {
			return true;
		}

		if (entry.isDirectory()) {
			this.getParent().changeDirectory(entry.getName());
			this.setSelectedIndex(-1);
		} else {
			this.setSelected(entry);
		}

		return true;
	}

	public void syncEntries(@NotNull GuiSchematicLoad parent) {
		this.clearEntries();
		for (GuiSchematicLoadListEntry schematicLoadListEntry : parent.getSchematicListSlots()) {
			this.addEntry(schematicLoadListEntry);
		}
	}
}