package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.core.BaseScreen;
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

public class SchematicLoadScreen extends BaseScreen {
	private static final FileFilter FILE_FILTER_FOLDER = new FileFilterSchematic(true);
	private static final FileFilter FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);
	private final List<SchematicLoadList.Entry> schematicListSlots = new ArrayList<>();
	private final Component strTitle = Component.translatable(Names.Gui.Load.TITLE);
	private final Component strFolderInfo = Component.translatable(Names.Gui.Load.FOLDER_INFO);
	private final Component strNoSchematic = Component.translatable(Names.Gui.Load.NO_SCHEMATIC);
	protected File currentDirectory = SchematicaClientConfig.schematicDirectory;
	private SchematicLoadList schematicLoadList;

	public SchematicLoadScreen(Screen parentScreen) {
		super(parentScreen);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		return this.schematicLoadList.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!schematicListSlots.isEmpty()) {
			super.render(guiGraphics, mouseX, mouseY, partialTicks);
			guiGraphics.drawCenteredString(this.minecraft.font, this.strTitle, this.width / 2, 4, 0x00FFFFFF);
			guiGraphics.drawCenteredString(this.minecraft.font,
					this.strFolderInfo,
					this.width / 2 - 79,
					this.height - 12,
					0x00A0A0A0);
		}
	}

	@Override
	public void init() {
		this.schematicLoadList = new SchematicLoadList(this);
		this.addRenderableWidget(schematicLoadList);

		Button buttonOpenDir = Button.builder(Component.translatable(Names.Gui.Load.OPEN_FOLDER), (event) -> {
			try {
				Util.getPlatform().openFile(SchematicaClientConfig.schematicDirectory);
			} catch (Throwable e) {
				System.out.println("Desktop actions are not supported on this platform.");
			}
		}).size(150, 20).pos(this.width / 2 - 154, this.height - 36).build();
		this.addRenderableWidget(buttonOpenDir);


		Button buttonDone = Button.builder(Component.translatable(Names.Gui.DONE), (event) -> {
			if (Reference.proxy.isLoadEnabled) {
				loadSchematic();
			}
			this.minecraft.setScreen(this.parentScreen);
		}).bounds(this.width / 2 + 4, this.height - 36, 150, 20).build();
		this.addRenderableWidget(buttonDone);

		reloadSchematics();
	}

	private void loadSchematic() {
		SchematicLoadList.Entry entry = this.schematicLoadList.getSelected();

		try {
			if (Reference.proxy.loadSchematic(Minecraft.getInstance().player, this.currentDirectory,
					entry.getName())) {
				FakeLevel level = ClientProxy.schematic;
				if (level != null) {
					ClientProxy.moveSchematicToPlayer(level);
				}
			}
		} catch (Exception e) {
			Reference.logger.error("Failed to load schematic!", e);
		}
	}

	protected void reloadSchematics() {
		String name;
		Item item;

		this.getSchematicListSlots().clear();

		try {
			if (!this.currentDirectory.getCanonicalPath()
					.equals(SchematicaClientConfig.schematicDirectory.getCanonicalPath())) {
				this.getSchematicListSlots().add(new SchematicLoadList.Entry("..",
						Items.LAVA_BUCKET,
						true,
						this.schematicLoadList));
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

				this.getSchematicListSlots().add(new SchematicLoadList.Entry(name,
						item,
						file.isDirectory(),
						this.schematicLoadList));
			}
		}

		File[] filesSchematics = this.currentDirectory.listFiles(FILE_FILTER_SCHEMATIC);
		if (filesSchematics == null || filesSchematics.length == 0) {
			this.getSchematicListSlots().add(new SchematicLoadList.Entry(this.strNoSchematic.getString(),
					Blocks.DIRT,
					false,
					this.schematicLoadList));
		} else {
			Arrays.sort(filesSchematics, (File a, File b) -> a.getName().compareToIgnoreCase(b.getName()));
			for (File file : filesSchematics) {
				name = file.getName();

				this.getSchematicListSlots().add(new SchematicLoadList.Entry(name, SchematicUtil.getIconFromFile(file),
						file.isDirectory(),
						this.schematicLoadList));
			}
		}

		schematicLoadList.syncEntries();
	}

	public List<SchematicLoadList.Entry> getSchematicListSlots() {
		return schematicListSlots;
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