package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.ItemStackSortType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.List;

public class GuiSchematicMaterials extends ScreenBase {
	protected final List<BlockList.WrappedItemStack> blockList;
	private final String strMaterialName = I18n.format(Names.Gui.Control.MATERIAL_NAME);
	private final String strMaterialAmount = I18n.format(Names.Gui.Control.MATERIAL_AMOUNT);
	private GuiSchematicMaterialsSlot guiSchematicMaterialsSlot;
	private ItemStackSortType sortType = SchematicaConfig.CLIENT.sortType.get();
	private Button btnSort = null;

	public GuiSchematicMaterials(Screen guiScreen) {
		super(guiScreen);
		Minecraft minecraft = Minecraft.getInstance();
		SchematicWorld schematic = ClientProxy.schematic;
		this.blockList = new BlockList().getList(minecraft.player, schematic, minecraft.world);
		this.sortType.sort(this.blockList);
	}

	@Override
	public void render(int x, int y, float partialTicks) {
		this.guiSchematicMaterialsSlot.render(x, y, partialTicks);

		drawString(this.getMinecraft().fontRenderer, this.strMaterialName, this.width / 2 - 108, 4, 0x00FFFFFF);
		drawString(this.getMinecraft().fontRenderer, this.strMaterialAmount,
		           this.width / 2 + 108 - this.getMinecraft().fontRenderer.getStringWidth(this.strMaterialAmount), 4,
		           0x00FFFFFF);
		super.render(x, y, partialTicks);
	}

	@Override
	public void init() {
		this.btnSort = new Button(this.width / 2 - 154, this.height - 30, 100, 20,
		                          I18n.format(Names.Gui.Control.SORT_PREFIX + this.sortType.label)
				                          + " "
				                          + this.sortType.glyph, (button) -> {
			this.sortType = this.sortType.next();
			this.sortType.sort(this.blockList);
			this.btnSort.setMessage(
					I18n.format(Names.Gui.Control.SORT_PREFIX + this.sortType.label) + " " + this.sortType.glyph);
		});
		this.buttons.add(this.btnSort);

		Button btnDump = new Button(this.width / 2 - 50, this.height - 30, 100, 20,
		                            I18n.format(Names.Gui.Control.DUMP),
		                            (button) -> dumpMaterialList(this.blockList));
		this.buttons.add(btnDump);

		Button btnDone = new Button(this.width / 2 + 54, this.height - 30, 100, 20, I18n.format(Names.Gui.DONE),
		                            (button) -> this.minecraft.displayGuiScreen(this.parentScreen));
		this.buttons.add(btnDone);

		this.guiSchematicMaterialsSlot = new GuiSchematicMaterialsSlot(this);
	}

	private void dumpMaterialList(List<BlockList.WrappedItemStack> blockList) {
		if (blockList.isEmpty()) {
			return;
		}

		int maxLengthName = 0;
		int maxSize = 0;
		for (BlockList.WrappedItemStack wrappedItemStack : blockList) {
			maxLengthName =
					Math.max(maxLengthName, wrappedItemStack.getItemStackDisplayName().getFormattedText().length());
			maxSize = Math.max(maxSize, wrappedItemStack.total);
		}

		int maxLengthSize = String.valueOf(maxSize).length();
		String formatName = "%-" + maxLengthName + "s";
		String formatSize = "%" + maxLengthSize + "d";

		StringBuilder stringBuilder = new StringBuilder((maxLengthName + 1 + maxLengthSize) * blockList.size());
		Formatter formatter = new Formatter(stringBuilder);
		for (BlockList.WrappedItemStack wrappedItemStack : blockList) {
			formatter.format(formatName, wrappedItemStack.getItemStackDisplayName().getFormattedText());
			stringBuilder.append(" ");
			formatter.format(formatSize, wrappedItemStack.total);
			stringBuilder.append(System.lineSeparator());
		}

		File dumps = Reference.proxy.getDirectory("dumps");
		try {
			try (FileOutputStream outputStream = new FileOutputStream(
					new File(dumps, Reference.MODID + "-materials.txt"))) {
				IOUtils.write(stringBuilder.toString(), outputStream, StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			Reference.logger.error("Could not dump the material list!", e);
		}
	}

	@Override
	public boolean mouseScrolled(double d1, double d2, double direction) {
		this.guiSchematicMaterialsSlot.scroll((int) (d2 * direction * -1));
		return super.mouseScrolled(d1, d2, direction);
	}
}