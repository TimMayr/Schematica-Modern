package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiSchematicLoad extends ScreenBase {
	private static final FileFilter FILE_FILTER_FOLDER = new FileFilterSchematic(true);
	private static final FileFilter FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);
	private final List<GuiSchematicEntry> schematicFiles = new ArrayList<>();
	private final Component strTitle = Component.translatable(Names.Gui.Load.TITLE);
	private final Component strFolderInfo = Component.translatable(Names.Gui.Load.FOLDER_INFO);
	private final Component strNoSchematic = Component.translatable(Names.Gui.Load.NO_SCHEMATIC);
	protected File currentDirectory = SchematicaClientConfig.schematicDirectory;
	private GuiSchematicLoadList guiSchematicLoadList;

	public GuiSchematicLoad(Screen Screen) {
		super(Screen);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		return this.guiSchematicLoadList.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (!schematicFiles.isEmpty()) {
			this.guiSchematicLoadList.render(graphics, mouseX, mouseY, partialTicks);

			graphics.drawCenteredString(this.minecraft.font, this.strTitle, this.width / 2, 4, 0x00FFFFFF);
			graphics.drawCenteredString(this.minecraft.font, this.strFolderInfo, this.width / 2 - 78, this.height - 12,
			                            0x00808080);

			super.render(graphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void init() {
		reloadSchematics();

		Button btnOpenDir = Button.builder(Component.translatable(Names.Gui.Load.OPEN_FOLDER), (event) -> {
			try {
				Util.getPlatform().openFile(SchematicaClientConfig.schematicDirectory);
			} catch (Throwable e) {
				System.out.println("Desktop actions are not supported on this platform.");
			}
		}).size(150, 20).pos(this.width / 2 - 154, this.height - 36).build();
		this.addRenderableWidget(btnOpenDir);

		Button btnDone = Button.builder(Component.translatable(Names.Gui.DONE), (event) -> {
			if (Reference.proxy.isLoadEnabled) {
				loadSchematic();
			}
			this.minecraft.setScreen(this.parentScreen);
		}).bounds(this.width / 2 + 4, this.height - 36, 150, 20).build();
		this.addRenderableWidget(btnDone);

		this.guiSchematicLoadList = new GuiSchematicLoadList(this);
	}

	protected void reloadSchematics() {
		String name;
		Item item;

		this.getSchematicFiles().clear();

		try {
			if (!this.currentDirectory.getCanonicalPath()
			                          .equals(SchematicaClientConfig.schematicDirectory.getCanonicalPath())) {
				this.getSchematicFiles().add(new GuiSchematicEntry("..", Items.LAVA_BUCKET, true));
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

				this.getSchematicFiles().add(new GuiSchematicEntry(name, item, file.isDirectory()));
			}
		}

		File[] filesSchematics = this.currentDirectory.listFiles(FILE_FILTER_SCHEMATIC);
		if (filesSchematics == null || filesSchematics.length == 0) {
			this.getSchematicFiles().add(new GuiSchematicEntry(this.strNoSchematic.getString(), Blocks.DIRT, false));
		} else {
			Arrays.sort(filesSchematics, (File a, File b) -> a.getName().compareToIgnoreCase(b.getName()));
			for (File file : filesSchematics) {
				name = file.getName();

				this.getSchematicFiles()
				    .add(new GuiSchematicEntry(name, SchematicUtil.getIconFromFile(file), file.isDirectory()));
			}
		}
	}

	private void loadSchematic() {
		int selectedIndex = this.guiSchematicLoadList.getSelectedIndex();

		try {
			if (selectedIndex >= 0 && selectedIndex < this.getSchematicFiles().size()) {
				GuiSchematicEntry schematicEntry = this.getSchematicFiles().get(selectedIndex);
				if (Reference.proxy.loadSchematic(Minecraft.getInstance().player, this.currentDirectory,
				                                  schematicEntry.getName())) {
					FakeLevel level = ClientProxy.schematic;
					if (level != null) {
						ClientProxy.moveSchematicToPlayer(level);
					}
				}
			}
		} catch (Exception e) {
			Reference.logger.error("Failed to load schematic!", e);
		}
	}

	public List<GuiSchematicEntry> getSchematicFiles() {
		return schematicFiles;
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