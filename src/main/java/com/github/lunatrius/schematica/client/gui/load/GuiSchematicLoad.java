package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiSchematicLoad extends ScreenBase {
	private static final FileFilterSchematic FILE_FILTER_FOLDER = new FileFilterSchematic(true);
	private static final FileFilterSchematic FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);
	protected final List<GuiSchematicEntry> schematicFiles = new ArrayList<>();
	private final String strTitle = I18n.format(Names.Gui.Load.TITLE);
	private final String strFolderInfo = I18n.format(Names.Gui.Load.FOLDER_INFO);
	private final String strNoSchematic = I18n.format(Names.Gui.Load.NO_SCHEMATIC);
	protected File currentDirectory = SchematicaClientConfig.schematicDirectory;
	private GuiSchematicLoadSlot guiSchematicLoadSlot;

	public GuiSchematicLoad(net.minecraft.client.gui.screen.Screen Screen) {
		super(Screen);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseEvent) {
		super.mouseClicked(mouseX, mouseY, mouseEvent);
		return this.guiSchematicLoadSlot.mouseClicked(mouseX, mouseY, mouseEvent);
	}

	@Override
	public void init() {
		Button btnOpenDir =
				new Button(this.width / 2 - 154, this.height - 36, 150, 20, I18n.format(Names.Gui.Load.OPEN_FOLDER),
				           (event) -> {
					           try {
						           Class<?> c = Class.forName("java.awt.Desktop");
						           Object m = c.getMethod("getDesktop").invoke(null);
						           c.getMethod("browse", URI.class)
						            .invoke(m, SchematicaClientConfig.schematicDirectory.toURI());
					           } catch (Throwable e) {
						           System.out.println("Desktop actions are not supported on this platform.");
					           }
				           });
		this.buttons.add(btnOpenDir);

		Button btnDone =
				new Button(this.width / 2 + 4, this.height - 36, 150, 20, I18n.format(Names.Gui.DONE), (event) -> {
					if (Reference.proxy.isLoadEnabled) {
						loadSchematic();
					}
					this.minecraft.displayGuiScreen(this.parentScreen);
				});
		this.buttons.add(btnDone);

		this.guiSchematicLoadSlot = new GuiSchematicLoadSlot(this);

		reloadSchematics();
	}

	protected void reloadSchematics() {
		String name;
		Item item;

		this.schematicFiles.clear();

		try {
			if (!this.currentDirectory.getCanonicalPath()
			                          .equals(SchematicaClientConfig.schematicDirectory.getCanonicalPath())) {
				this.schematicFiles.add(new GuiSchematicEntry("..", Items.LAVA_BUCKET, true));
			}
		} catch (IOException e) {
			Reference.logger.error("Failed to add GuiSchematicEntry!", e);
		}

		File[] filesFolders = this.currentDirectory.listFiles(FILE_FILTER_FOLDER);
		if (filesFolders == null) {
			Reference.logger.error("listFiles returned null (directory: {})!", this.currentDirectory);
		} else {
			Arrays.sort(filesFolders, (File a, File b) -> a.getName().compareToIgnoreCase(b.getName()));
			for (File file : filesFolders) {
				if (file == null) {
					continue;
				}

				name = file.getName();

				File[] files = file.listFiles();
				item = (files == null || files.length == 0) ? Items.BUCKET : Items.WATER_BUCKET;

				this.schematicFiles.add(new GuiSchematicEntry(name, item, file.isDirectory()));
			}
		}

		File[] filesSchematics = this.currentDirectory.listFiles(FILE_FILTER_SCHEMATIC);
		if (filesSchematics == null || filesSchematics.length == 0) {
			this.schematicFiles.add(new GuiSchematicEntry(this.strNoSchematic, Blocks.DIRT, false));
		} else {
			Arrays.sort(filesSchematics, (File a, File b) -> a.getName().compareToIgnoreCase(b.getName()));
			for (File file : filesSchematics) {
				name = file.getName();

				this.schematicFiles.add(
						new GuiSchematicEntry(name, SchematicUtil.getIconFromFile(file), file.isDirectory()));
			}
		}
	}

	private void loadSchematic() {
		int selectedIndex = this.guiSchematicLoadSlot.selectedIndex;

		try {
			if (selectedIndex >= 0 && selectedIndex < this.schematicFiles.size()) {
				GuiSchematicEntry schematicEntry = this.schematicFiles.get(selectedIndex);
				if (Reference.proxy.loadSchematic(null, this.currentDirectory, schematicEntry.getName())) {
					SchematicWorld schematic = ClientProxy.schematic;
					if (schematic != null) {
						ClientProxy.moveSchematicToPlayer(schematic);
					}
				}
			}
		} catch (Exception e) {
			Reference.logger.error("Failed to load schematic!", e);
		}
	}

	@Override
	public void render(int x, int y, float partialTicks) {
		this.guiSchematicLoadSlot.render(x, y, partialTicks);

		drawCenteredString(this.minecraft.fontRenderer, this.strTitle, this.width / 2, 4, 0x00FFFFFF);
		drawCenteredString(this.minecraft.fontRenderer, this.strFolderInfo, this.width / 2 - 78, this.height - 12,
		                   0x00808080);

		super.render(x, y, partialTicks);
	}

	protected void changeDirectory(String directory) {
		this.currentDirectory = new File(this.currentDirectory, directory);

		try {
			this.currentDirectory = this.currentDirectory.getCanonicalFile();
		} catch (IOException ioe) {
			Reference.logger.error("Failed to canonize directory!", ioe);
		}

		reloadSchematics();
	}
}
