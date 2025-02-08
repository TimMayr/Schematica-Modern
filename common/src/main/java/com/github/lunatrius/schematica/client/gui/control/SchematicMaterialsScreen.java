package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.schematica.client.gui.core.BaseScreen;
import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.ItemStackSortType;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.List;

public class SchematicMaterialsScreen extends BaseScreen {
	private final List<BlockList.WrappedItemStack> blockList;
	private final Component strMaterialName = Component.translatable(Names.Gui.Control.MATERIAL_NAME);
	private final Component strMaterialAmount = Component.translatable(Names.Gui.Control.MATERIAL_AMOUNT);
	private SchematicMaterialsSlot schematicMaterialsSlot;
	private ItemStackSortType sortType = SchematicaConfig.CLIENT.sortType.get();
	private Button buttonSort = null;

	public SchematicMaterialsScreen(Screen parentScreen) {
		super(parentScreen);
		Minecraft minecraft = Minecraft.getInstance();
		FakeLevel level = ClientProxy.schematic;
		this.blockList = new BlockList().getList(minecraft.player, level, minecraft.level);
		this.sortType.sort(this.getBlockList());
	}

	public List<BlockList.WrappedItemStack> getBlockList() {
		return blockList;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.schematicMaterialsSlot.render(guiGraphics, mouseX, mouseY, partialTicks);

		guiGraphics.drawString(this.minecraft.font, this.strMaterialName, this.width / 2 - 108, 4, 0x00FFFFFF);
		guiGraphics.drawString(this.minecraft.font, this.strMaterialAmount,
		                    this.width / 2 + 108 - this.minecraft.font.width(this.strMaterialAmount), 4, 0x00FFFFFF);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void init() {
		this.buttonSort = Button.builder(Component.translatable(Names.Gui.Control.SORT_PREFIX + this.sortType.label)
		                                       .append(" " + this.sortType.glyph), (button) -> {
			this.sortType = this.sortType.next();
			this.sortType.sort(this.getBlockList());
			this.buttonSort.setMessage(Component.translatable(Names.Gui.Control.SORT_PREFIX + this.sortType.label)
			                                 .append(" " + this.sortType.glyph));
		}).bounds(this.width / 2 - 154, this.height - 30, 100, 20).build();
		this.addRenderableWidget(this.buttonSort);

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

		this.schematicMaterialsSlot = new SchematicMaterialsSlot(this.minecraft, 800, 1000, 0, 50, this);
	}

	private void dumpMaterialList(@NotNull List<BlockList.WrappedItemStack> blockList) {
		if (blockList.isEmpty()) {
			return;
		}

		int maxLengthName = 0;
		int maxSize = 0;
		for (BlockList.WrappedItemStack wrappedItemStack : blockList) {
			maxLengthName = Math.max(maxLengthName, wrappedItemStack.getItemStackDisplayName().getString().length());
			maxSize = Math.max(maxSize, wrappedItemStack.total);
		}

		StringBuilder stringBuilder = formatBockList(blockList, maxSize, maxLengthName);

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

	private static @NotNull StringBuilder formatBockList(@NotNull List<BlockList.WrappedItemStack> blockList,
	                                                     int maxSize, int maxLengthName) {
		int maxLengthSize = String.valueOf(maxSize).length();
		String formatName = "%-" + maxLengthName + "s";
		String formatSize = "%" + maxLengthSize + "d";

		StringBuilder stringBuilder = new StringBuilder((maxLengthName + 1 + maxLengthSize) * blockList.size());
		Formatter formatter = new Formatter(stringBuilder);
		for (BlockList.WrappedItemStack wrappedItemStack : blockList) {
			formatter.format(formatName, wrappedItemStack.getItemStackDisplayName().getString());
			stringBuilder.append(" ");
			formatter.format(formatSize, wrappedItemStack.total);
			stringBuilder.append(System.lineSeparator());
		}
		return stringBuilder;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		this.schematicMaterialsSlot.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
}