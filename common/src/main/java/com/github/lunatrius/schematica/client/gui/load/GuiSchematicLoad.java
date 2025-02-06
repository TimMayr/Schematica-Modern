package com.github.lunatrius.schematica.client.gui.load;

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

public class GuiSchematicLoad extends ScreenBaseTest {
	private static final FileFilter FILE_FILTER_FOLDER = new FileFilterSchematic(true);
	private static final FileFilter FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);
	private final List<GuiSchematicLoadListEntry> schematicListSlots = new ArrayList<>();
	private final Component strTitle = Component.translatable(Names.Gui.Load.TITLE);
	private final Component strFolderInfo = Component.translatable(Names.Gui.Load.FOLDER_INFO);
	private final Component strNoSchematic = Component.translatable(Names.Gui.Load.NO_SCHEMATIC);
	private Button btnOpenDir;
	private Button btnDone;
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
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!schematicListSlots.isEmpty()) {
			super.render(guiGraphics, mouseX, mouseY, partialTicks);

			guiGraphics.drawCenteredString(this.minecraft.font, this.strTitle, this.width / 2, 4, 0x00FFFFFF);
			guiGraphics.drawCenteredString(this.minecraft.font,
			                               this.strFolderInfo,
			                               this.width / 2 - 79,
			                               this.height - 12,
			                            0x00808080);

			guiSchematicLoadList.render(guiGraphics, mouseX, mouseY, partialTicks);

			btnOpenDir.render(guiGraphics, mouseX, mouseY, partialTicks);
			btnDone.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void init() {
		this.guiSchematicLoadList = new GuiSchematicLoadList(this);

		btnOpenDir = Button.builder(Component.translatable(Names.Gui.Load.OPEN_FOLDER), (event) -> {
			try {
				Util.getPlatform().openFile(SchematicaClientConfig.schematicDirectory);
			} catch (Throwable e) {
				System.out.println("Desktop actions are not supported on this platform.");
			}
		}).size(150, 20).pos(this.width / 2 - 154, this.height - 36).build();
		this.addRenderableWidget(btnOpenDir);

		btnDone = Button.builder(Component.translatable(Names.Gui.DONE), (event) -> {
			if (Reference.proxy.isLoadEnabled) {
				loadSchematic();
			}
			this.minecraft.setScreen(this.parentScreen);
		}).bounds(this.width / 2 + 4, this.height - 36, 150, 20).build();
		this.addRenderableWidget(btnDone);

		reloadSchematics();
	}

	protected void reloadSchematics() {
		String name;
		Item item;

		this.getSchematicListSlots().clear();

		try {
			if (!this.currentDirectory.getCanonicalPath()
			                          .equals(SchematicaClientConfig.schematicDirectory.getCanonicalPath())) {
				this.getSchematicListSlots().add(new GuiSchematicLoadListEntry("..",
				                                                               Items.LAVA_BUCKET,
				                                                               true,
				                                                               this.guiSchematicLoadList));
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

				this.getSchematicListSlots().add(new GuiSchematicLoadListEntry(name,
				                                                               item,
				                                                               file.isDirectory(),
				                                                               this.guiSchematicLoadList));
			}
		}

		File[] filesSchematics = this.currentDirectory.listFiles(FILE_FILTER_SCHEMATIC);
		if (filesSchematics == null || filesSchematics.length == 0) {
			this.getSchematicListSlots().add(new GuiSchematicLoadListEntry(this.strNoSchematic.getString(),
			                                                               Blocks.DIRT,
			                                                               false,
			                                                               this.guiSchematicLoadList));
		} else {
			Arrays.sort(filesSchematics, (File a, File b) -> a.getName().compareToIgnoreCase(b.getName()));
			for (File file : filesSchematics) {
				name = file.getName();

				this.getSchematicListSlots().add(new GuiSchematicLoadListEntry(name,
				                                                               SchematicUtil.getIconFromFile(file),
				                                                               file.isDirectory(),
				                                                               this.guiSchematicLoadList));
			}
		}

		guiSchematicLoadList.syncEntries(this);
	}

	public List<GuiSchematicLoadListEntry> getSchematicListSlots() {
		return schematicListSlots;
	}

	private void loadSchematic() {
		GuiSchematicLoadListEntry entry = this.guiSchematicLoadList.getSelected();

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