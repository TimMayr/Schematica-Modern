package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.core.BaseScreen;
import com.github.lunatrius.schematica.core.ItemStackSortType;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SchematicMaterialsScreen extends BaseScreen {
	private final List<BlockList.WrappedItemStack> blockList;
	private final Component strMaterialName = Component.translatable(Names.Gui.Control.MATERIAL_NAME);
	private final Component strMaterialAmount = Component.translatable(Names.Gui.Control.MATERIAL_AMOUNT);
	private SchematicMaterialsList schematicMaterialsList;
	private ItemStackSortType sortType = ItemStackSortType.SIZE_DESC;

	public SchematicMaterialsScreen(Screen parentScreen) {
		super(parentScreen);
		Minecraft minecraft = Minecraft.getInstance();
		FakeLevel level = ClientProxy.schematic;
		this.blockList = new BlockList().getList(minecraft.player, level, minecraft.level);
		this.sortType.sort(this.getBlockList(), BlockList.WrappedItemStack::getItemStack);
	}

	public List<BlockList.WrappedItemStack> getBlockList() {
		return blockList;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		guiGraphics.drawString(this.minecraft.font, this.strMaterialName, this.width / 2 - 108, 4, 0x00FFFFFF);
		guiGraphics.drawString(this.minecraft.font, this.strMaterialAmount,
				this.width / 2 + 108 - this.minecraft.font.width(this.strMaterialAmount), 4, 0x00FFFFFF);
	}

	@Override
	public void init() {
		CycleButton<ItemStackSortType> buttonSort = new CycleButton.Builder<ItemStackSortType>((s) ->
				Component.translatable(s.toString()))
				.withInitialValue(sortType)
				.withValues(ItemStackSortType.values()).create(this.width / 2 - 154, this.height - 30, 100, 20,
						Component.translatable(Names.Gui.Control.SORT_PREFIX),
						(button, sortType) -> this.setSortType(sortType));
		this.addRenderableWidget(buttonSort);


		Button btnDump = Button.builder(Component.translatable(Names.Gui.Control.DUMP),
						(button) -> dumpMaterialList(this.getBlockList()))
				.bounds(this.width / 2 - 50, this.height - 30, 100, 20)
				.build();
		this.addRenderableWidget(btnDump);


		Button btnDone = Button.builder(Component.translatable(Names.Gui.DONE),
						(button) -> this.minecraft.setScreen(this.parentScreen))
				.bounds(this.width / 2 + 54, this.height - 30, 100, 20)
				.build();
		this.addRenderableWidget(btnDone);

		this.schematicMaterialsList = new SchematicMaterialsList(this);
		this.addRenderableWidget(schematicMaterialsList);
		schematicMaterialsList.syncEntries();
	}

	private void setSortType(ItemStackSortType sortType) {
		this.sortType = sortType;
		this.sortType.sort(this.getBlockList(), BlockList.WrappedItemStack::getItemStack);
		schematicMaterialsList.syncEntries();
	}

	private void dumpMaterialList(@NotNull List<BlockList.WrappedItemStack> blockList) {
		if (blockList.isEmpty()) {
			return;
		}

		int maxLengthName = 0;
		int maxSize = 0;
		for (BlockList.WrappedItemStack wrappedItemStack : blockList) {
			maxLengthName = Math.max(maxLengthName, wrappedItemStack.getItemStackDisplayName().getString().length());
			maxSize = Math.max(maxSize, wrappedItemStack.itemStack.getCount());
		}

		StringBuilder stringBuilder = formatBlockList(blockList, maxSize, maxLengthName);

		File dumps = Reference.proxy.getDirectory("dumps");
		try {
			try (FileOutputStream outputStream = new FileOutputStream(
					new File(dumps, Reference.MOD_ID + "-materials.txt"))) {
				IOUtils.write(stringBuilder.toString(), outputStream, StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			Reference.logger.error("Could not dump the material list!", e);
		}
	}

	private static @NotNull StringBuilder formatBlockList(@NotNull List<BlockList.WrappedItemStack> blockList,
	                                                      int maxSize, int maxLengthName) {
		int maxLengthSize = String.valueOf(maxSize).length();
		String formatName = "%-" + maxLengthName + "s";
		String formatSize = "%" + maxLengthSize + "d";

		StringBuilder stringBuilder = new StringBuilder((maxLengthName + 1 + maxLengthSize) * blockList.size());
		Formatter formatter = new Formatter(stringBuilder);
		for (BlockList.WrappedItemStack wrappedItemStack : blockList) {
			formatter.format(formatName, wrappedItemStack.getItemStackDisplayName().getString());
			stringBuilder.append(" ");
			formatter.format(formatSize, wrappedItemStack.itemStack.getCount());
			stringBuilder.append(System.lineSeparator());
		}
		return stringBuilder;
	}
}