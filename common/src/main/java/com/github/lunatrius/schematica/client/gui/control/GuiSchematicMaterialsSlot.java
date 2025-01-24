package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.GuiHelper;
import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.reference.Names;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

class GuiSchematicMaterialsSlot extends AbstractSelectionList<GuiSchematicMaterialsSlot.ItemEntry> {
	private final Minecraft minecraft = Minecraft.getInstance();
	private final GuiSchematicMaterials guiSchematicMaterials;

	public GuiSchematicMaterialsSlot(Minecraft minecraft, int width, int height, int y, int itemHeight,
	                                 GuiSchematicMaterials guiSchematicMaterials) {
		super(minecraft, width, height, y, itemHeight);
		this.guiSchematicMaterials = guiSchematicMaterials;
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

	public Minecraft getMinecraft() {
		return minecraft;
	}

	public GuiSchematicMaterials getGuiSchematicMaterials() {
		return guiSchematicMaterials;
	}

	public static class ItemEntry extends AbstractSelectionList.Entry<ItemEntry> {
		private final GuiSchematicMaterialsSlot parent;

		private final Component strMaterialAvailable = Component.translatable(Names.Gui.Control.MATERIAL_AVAILABLE);
		private final Component strMaterialMissing = Component.translatable(Names.Gui.Control.MATERIAL_MISSING);

		public ItemEntry(GuiSchematicMaterialsSlot parent) {
			this.parent = parent;
		}

		@Override
		public void render(@NotNull GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX,
		                   int mouseY,
		                   boolean isHovered, float partialTicks) {
			BlockList.WrappedItemStack wrappedItemStack = parent.getGuiSchematicMaterials().getBlockList().get(index);
			ItemStack itemStack = wrappedItemStack.itemStack;

			String itemName = wrappedItemStack.getItemStackDisplayName().getString();
			String amount = wrappedItemStack.getFormattedAmount();
			String amountMissing = wrappedItemStack.getFormattedAmountMissing(strMaterialAvailable.getString(),
			                                                                  strMaterialMissing.getString());

			GuiHelper.drawItemStackWithSlot(parent.getMinecraft().getTextureManager(), itemStack, x, y);

			graphics.drawString(parent.getMinecraft().font, itemName, x + 24, y + 6, 0xFFFFFF);
			graphics.drawString(parent.getMinecraft().font, amount, x + 215 - parent.getMinecraft().font.width(amount), y + 1,
			                    0xFFFFFF);
			graphics.drawString(parent.getMinecraft().font, amountMissing,
			                    x + 215 - parent.getMinecraft().font.width(amountMissing), y + 11, 0xFFFFFF);

			if (mouseX > x && mouseY > y && mouseX <= x + 18 && mouseY <= y + 18) {
				parent.getGuiSchematicMaterials().setTooltipForNextRenderPass(
						Screen.getTooltipFromItem(parent.getMinecraft(), itemStack)
						      .stream()
						      .reduce(Component.empty(), MutableComponent::append, MutableComponent::append));
				RenderSystem.setupGuiFlatDiffuseLighting(new Vector3f(), new Vector3f());
			}
		}
	}
}