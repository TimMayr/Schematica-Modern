package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.core.GuiHelper;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SchematicMaterialsList extends ObjectSelectionList<SchematicMaterialsList.Entry> {
	private final Minecraft minecraft = Minecraft.getInstance();
	private final SchematicMaterialsScreen parent;

	public SchematicMaterialsList(@NotNull SchematicMaterialsScreen parent) {
		super(Minecraft.getInstance(), parent.width, parent.height - 56, 16, 25, 0);
		this.parent = parent;
	}

	protected int getItemCount() {
		return this.getParent().getBlockList().size();
	}

	public SchematicMaterialsScreen getParent() {
		return parent;
	}

	public Minecraft getMinecraft() {
		return minecraft;
	}

	public void syncEntries() {
		this.clearEntries();
		for (BlockList.WrappedItemStack wrappedItemStack : parent.getBlockList()) {
			this.addEntry(new Entry(this, wrappedItemStack));
		}
	}

	@MethodsReturnNonnullByDefault
	public static class Entry extends ObjectSelectionList.Entry<Entry> {
		private final SchematicMaterialsList parent;

		private final Component strMaterialAvailable = Component.translatable(Names.Gui.Control.MATERIAL_AVAILABLE);
		private final Component strMaterialMissing = Component.translatable(Names.Gui.Control.MATERIAL_MISSING);
		private final BlockList.WrappedItemStack wrappedItemStack;

		public Entry(SchematicMaterialsList parent, BlockList.WrappedItemStack itemStack) {
			this.parent = parent;
			this.wrappedItemStack = itemStack;
		}

		@Override
		public void render(@NotNull GuiGraphics guiGraphics, int index, int left, int top, int width, int height,
		                   int mouseX,
		                   int mouseY, boolean isHovered, float partialTicks) {
			ItemStack itemStack = this.wrappedItemStack.itemStack;
			String itemName = this.wrappedItemStack.getItemStackDisplayName().getString();
			String amount = this.wrappedItemStack.getFormattedAmount();
			String amountMissing = this.wrappedItemStack.getFormattedAmountMissing(strMaterialAvailable.getString(),
					strMaterialMissing.getString());

			GuiHelper.drawItemStackWithSlot(guiGraphics, itemStack, top, left);

			guiGraphics.drawString(parent.getMinecraft().font, itemName, top + 24, left + 6, 0xFFFFFF);
			guiGraphics.drawString(parent.getMinecraft().font,
					amount,
					top + 215 - parent.getMinecraft().font.width(amount),
					left + 1,
					0xFFFFFF);
			guiGraphics.drawString(parent.getMinecraft().font,
					amountMissing,
					top + 215 - parent.getMinecraft().font.width(amountMissing),
					left + 11,
					0xFFFFFF);

			if (mouseX > top && mouseY > left && mouseX <= top + 18 && mouseY <= left + 18) {
				parent.getParent()
						.setTooltipForNextRenderPass(Screen.getTooltipFromItem(parent.getMinecraft(), itemStack)
								.stream()
								.reduce(Component.empty(),
										MutableComponent::append,
										MutableComponent::append));
			}
		}

		@Override
		public Component getNarration() {
			String itemName = this.wrappedItemStack.getItemStackDisplayName().getString();
			String amount = this.wrappedItemStack.getFormattedAmount();
			String amountMissing = this.wrappedItemStack.getFormattedAmountMissing(strMaterialAvailable.getString(),
					strMaterialMissing.getString());

			return Component.translatable(Names.Gui.Control.Narration.MATERIAL_LIST_ENTRY, itemName, amount,
					amountMissing);
		}
	}
}