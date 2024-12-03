package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.GuiHelper;
import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

class GuiSchematicMaterialsSlot extends GuiSlot {
	private final Minecraft minecraft = Minecraft.getMinecraft();

	private final GuiSchematicMaterials guiSchematicMaterials;

	private final String strMaterialAvailable = I18n.format(Names.Gui.Control.MATERIAL_AVAILABLE);
	private final String strMaterialMissing = I18n.format(Names.Gui.Control.MATERIAL_MISSING);

	protected int selectedIndex = -1;

	public GuiSchematicMaterialsSlot(GuiSchematicMaterials parent) {
		super(Minecraft.getMinecraft(), parent.width, parent.height, 16, parent.height - 34, 24);
		this.guiSchematicMaterials = parent;
		this.selectedIndex = -1;
	}

	@Override
	protected int getSize() {
		return this.guiSchematicMaterials.blockList.size();
	}

	@Override
	protected void elementClicked(int index, boolean par2, int par3, int par4) {
		this.selectedIndex = index;
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
	protected int getScrollBarX() {
		return this.width / 2 + getListWidth() / 2 + 2;
	}

	@Override
	protected void drawSlot(int index, int x, int y, int par4, int mouseX, int mouseY, float partialTicks) {
		BlockList.WrappedItemStack wrappedItemStack = this.guiSchematicMaterials.blockList.get(index);
		ItemStack itemStack = wrappedItemStack.itemStack;

		String itemName = wrappedItemStack.getItemStackDisplayName();
		String amount = wrappedItemStack.getFormattedAmount();
		String amountMissing = wrappedItemStack.getFormattedAmountMissing(strMaterialAvailable, strMaterialMissing);

		GuiHelper.drawItemStackWithSlot(this.minecraft.renderEngine, itemStack, x, y);

		this.guiSchematicMaterials.drawString(this.minecraft.fontRenderer, itemName, x + 24, y + 6, 0xFFFFFF);
		this.guiSchematicMaterials.drawString(this.minecraft.fontRenderer, amount,
		                                      x + 215 - this.minecraft.fontRenderer.getStringWidth(amount), y + 1,
		                                      0xFFFFFF);
		this.guiSchematicMaterials.drawString(this.minecraft.fontRenderer, amountMissing,
		                                      x + 215 - this.minecraft.fontRenderer.getStringWidth(amountMissing),
		                                      y + 11, 0xFFFFFF);

		if (mouseX > x && mouseY > y && mouseX <= x + 18 && mouseY <= y + 18) {
			this.guiSchematicMaterials.renderToolTip(itemStack, mouseX, mouseY);
			GlStateManager.disableLighting();
		}
	}
}
