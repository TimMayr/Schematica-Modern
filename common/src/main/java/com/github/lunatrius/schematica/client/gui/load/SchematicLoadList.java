package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.core.GuiHelper;
import com.github.lunatrius.schematica.reference.Names;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SchematicLoadList extends ObjectSelectionList<SchematicLoadList.Entry> {
	private final Minecraft minecraft = Minecraft.getInstance();

	private final SchematicLoadScreen parent;

	private long lastClick = 0;

	public SchematicLoadList(@NotNull SchematicLoadScreen parent) {
		//int width, int height, int y, int itemHeight, int headerHeight
		super(Minecraft.getInstance(), parent.width, parent.height - 56, 16, 22, 0);
		this.parent = parent;
	}

	@Override
	protected int getItemCount() {
		return this.getParent().getSchematicListSlots().size();
	}

	public SchematicLoadScreen getParent() {
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
		Entry entry = this.getSelected();

		if (ignore || entry == null) {
			return true;
		}

		this.setSelected(entry);

		return true;
	}

	public void syncEntries() {
		this.clearEntries();
		for (Entry entry : parent.getSchematicListSlots()) {
			this.addEntry(entry);
		}
	}

	@MethodsReturnNonnullByDefault
	public static class Entry extends ObjectSelectionList.Entry<Entry> {
		private final SchematicMetadata metadata;
		private final SchematicLoadList parent;
		private final ItemStack itemStack;
		private final String name;

		//The Constructor param positions are all out of whack intentionally. Java doesn't know which to call if you
		// pass to many nulls otherwise
		public Entry(SchematicLoadList parent, @Nullable SchematicMetadata metadata, @Nullable String name,
		             @Nullable ItemStack itemStack) {
			this(parent, metadata, name, itemStack.getItem());
		}

		public Entry(SchematicLoadList parent, @Nullable SchematicMetadata metadata, @Nullable String name,
		             @Nullable Item item) {
			this.metadata = metadata;
			this.parent = parent;

			if (metadata != null) {
				this.name = metadata.name();
				this.itemStack = metadata.icon();
			} else {
				this.itemStack = new ItemStack(item, 1);
				this.name = name;
			}
		}

		public Entry(SchematicLoadList parent, @Nullable SchematicMetadata metadata, @Nullable String name,
		             @Nullable Block block) {
			this(parent, metadata, name, block.asItem());
		}

		@Override
		public void render(@NotNull GuiGraphics guiGraphics, int index, int left, int top, int width, int height,
		                   int mouseX, int mouseY, boolean isHovered, float partialTicks) {
			String schematicName = FilenameUtils.getBaseName(name);
			GuiHelper.drawItemStackWithSlot(guiGraphics, getItemStack(), top, left);
			guiGraphics.drawString(parent.getMinecraft().font, schematicName, top + 24, left + 6, 0x00FFFFFF);
		}

		public ItemStack getItemStack() {
			return this.itemStack;
		}

		public SchematicMetadata getMetadata() {
			return this.metadata;
		}

		public Item getItem() {
			return this.itemStack.getItem();
		}

		@Override
		public Component getNarration() {
			return this.name.equals("..") ? Component.translatable(Names.Gui.Load.PARENT_DIR) :
					Component.literal(this.name);
		}
	}
}