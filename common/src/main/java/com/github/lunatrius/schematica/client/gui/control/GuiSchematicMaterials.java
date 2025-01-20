package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.api.ISchematic;
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
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.List;

public class GuiSchematicMaterials extends ScreenBase {
	protected final List<BlockList.WrappedItemStack> blockList;
	private final Component strMaterialName = Component.translatable(Names.Gui.Control.MATERIAL_NAME);
	private final Component strMaterialAmount = Component.translatable(Names.Gui.Control.MATERIAL_AMOUNT);
	private GuiSchematicMaterialsSlot guiSchematicMaterialsSlot;
	private ItemStackSortType sortType = SchematicaConfig.CLIENT.sortType.get();
	private Button btnSort = null;

	public GuiSchematicMaterials(Screen guiScreen) {
		super(guiScreen);
		Minecraft minecraft = Minecraft.getInstance();
		ISchematic schematic = ClientProxy.schematic;
		this.blockList = new BlockList().getList(minecraft.player, FakeLevel.of(schematic), minecraft.level);
		this.sortType.sort(this.blockList);
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.guiSchematicMaterialsSlot.render(graphics, mouseX, mouseY, partialTicks);

		graphics.drawString(this.minecraft.font, this.strMaterialName, this.width / 2 - 108, 4, 0x00FFFFFF);
		graphics.drawString(this.minecraft.font, this.strMaterialAmount,
		                    this.width / 2 + 108 - this.minecraft.font.width(this.strMaterialAmount), 4, 0x00FFFFFF);
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void init() {
		this.btnSort = new PlainTextButton(this.width / 2 - 154, this.height - 30, 100, 20,
		                                   Component.translatable(Names.Gui.Control.SORT_PREFIX + this.sortType.label)
		                                            .append(" " + this.sortType.glyph), (button) -> {
			this.sortType = this.sortType.next();
			this.sortType.sort(this.blockList);
			this.btnSort.setMessage(Component.translatable(Names.Gui.Control.SORT_PREFIX + this.sortType.label)
			                                 .append(" " + this.sortType.glyph));
		}, this.font);
		this.addRenderableWidget(this.btnSort);

		Button btnDump = new PlainTextButton(this.width / 2 - 50, this.height - 30, 100, 20,
		                                     Component.translatable(Names.Gui.Control.DUMP),
		                                     (button) -> dumpMaterialList(this.blockList), this.font);
		this.addRenderableWidget(btnDump);

		Button btnDone = new PlainTextButton(this.width / 2 + 54, this.height - 30, 100, 20,
		                                     Component.translatable(Names.Gui.DONE),
		                                     (button) -> this.minecraft.setScreen(this.parentScreen), this.font);
		this.addRenderableWidget(btnDone);

		this.guiSchematicMaterialsSlot = new GuiSchematicMaterialsSlot(this.minecraft, 800, 1000, 0, 50, this);
	}

	private void dumpMaterialList(List<BlockList.WrappedItemStack> blockList) {
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

	private static @NotNull StringBuilder formatBockList(List<BlockList.WrappedItemStack> blockList, int maxSize,
	                                                     int maxLengthName) {
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
		this.guiSchematicMaterialsSlot.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
}