package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.GuiHelper;
import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.reference.Names;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.SlotGui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

class GuiSchematicMaterialsSlot extends SlotGui {
	private final Minecraft minecraft = Minecraft.getInstance();

	private final GuiSchematicMaterials guiSchematicMaterials;

	private final String strMaterialAvailable = I18n.format(Names.Gui.Control.MATERIAL_AVAILABLE);
	private final String strMaterialMissing = I18n.format(Names.Gui.Control.MATERIAL_MISSING);

	protected final int selectedIndex;

	public GuiSchematicMaterialsSlot(GuiSchematicMaterials parent) {
		super(Minecraft.getInstance(), parent.width, parent.height, 16, parent.height - 34, 24);
		this.guiSchematicMaterials = parent;
		this.selectedIndex = -1;
	}

	@Override
	protected int getItemCount() {
		return this.guiSchematicMaterials.blockList.size();
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
		BlockList.WrappedItemStack wrappedItemStack = this.guiSchematicMaterials.blockList.get(index);
		ItemStack itemStack = wrappedItemStack.itemStack;

		String itemName = String.valueOf(wrappedItemStack.getItemStackDisplayName());
		String amount = wrappedItemStack.getFormattedAmount();
		String amountMissing = wrappedItemStack.getFormattedAmountMissing(strMaterialAvailable, strMaterialMissing);

		GuiHelper.drawItemStackWithSlot(this.minecraft.getTextureManager(), itemStack, x, y);

		this.guiSchematicMaterials.drawString(this.minecraft.fontRenderer, itemName, x + 24, y + 6, 0xFFFFFF);
		this.guiSchematicMaterials.drawString(this.minecraft.fontRenderer, amount,
		                                      x + 215 - this.minecraft.fontRenderer.getStringWidth(amount), y + 1,
		                                      0xFFFFFF);
		this.guiSchematicMaterials.drawString(this.minecraft.fontRenderer, amountMissing,
		                                      x + 215 - this.minecraft.fontRenderer.getStringWidth(amountMissing),
		                                      y + 11, 0xFFFFFF);

		if (mouseX > x && mouseY > y && mouseX <= x + 18 && mouseY <= y + 18) {
			this.guiSchematicMaterials.renderTooltip(this.guiSchematicMaterials.getTooltipFromItem(itemStack), x, y);
			RenderSystem.disableLighting();
		}
	}

	@Override
	protected void drawContainerBackground(@Nonnull Tessellator tessellator) {
	}
}
