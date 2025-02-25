package com.github.lunatrius.schematica.client.gui.load;

import com.github.lunatrius.schematica.config.client.SchematicaClientConfig;
import com.github.lunatrius.schematica.core.BaseScreen;
import com.github.lunatrius.schematica.core.FileUtils;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.util.FileFilterSchematic;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SchematicLoadScreen extends BaseScreen {
	private static final DirectoryStream.Filter<Path> FILE_FILTER_FOLDER = new FileFilterSchematic(true);
	private static final DirectoryStream.Filter<Path> FILE_FILTER_SCHEMATIC = new FileFilterSchematic(false);
	private final List<SchematicLoadList.Entry> schematicListSlots = new ArrayList<>();
	private final Component strTitle = Component.translatable(Names.Gui.Load.TITLE);
	private final Component strFolderInfo = Component.translatable(Names.Gui.Load.FOLDER_INFO);
	private final Component strNoSchematic = Component.translatable(Names.Gui.Load.NO_SCHEMATIC);
	protected Path currentDirectory = SchematicaClientConfig.schematicDirectory;
	private SchematicLoadList schematicLoadList;

	public SchematicLoadScreen(Screen parent) {
		super(parent);
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
				Util.getPlatform().openPath(SchematicaClientConfig.schematicDirectory);
			} catch (Throwable e) {
				System.out.println("Desktop actions are not supported on this platform.");
			}
		}).size(150, 20).pos(this.width / 2 - 154, this.height - 36).build();
		this.addRenderableWidget(buttonOpenDir);


		Button buttonDone = Button.builder(Component.translatable(Names.Gui.DONE), (event) -> {
			if (Reference.proxy.isLoadEnabled) {
				loadSchematic();
			}
			this.minecraft.setScreen(this.parent);
		}).bounds(this.width / 2 + 4, this.height - 36, 150, 20).build();
		this.addRenderableWidget(buttonDone);

		reloadSchematics();
	}

	private void loadSchematic() {
		SchematicLoadList.Entry entry = this.schematicLoadList.getSelected();

		try {
			if (Reference.proxy.loadSchematic(Minecraft.getInstance().player, entry.getMetadata())) {
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
			if (!this.currentDirectory.toRealPath().normalize()
					.equals(SchematicaClientConfig.schematicDirectory.toRealPath().normalize())) {
				this.getSchematicListSlots().add(new SchematicLoadList.Entry(null, "..",
						true, Items.LAVA_BUCKET, this.schematicLoadList));
			}
		} catch (IOException e) {
			Reference.logger.error("Failed to add GuiSchematicEntry!", e);
		}

		List<Path> filesFolders = FileUtils.getAllFilesInDirectory(this.currentDirectory, FILE_FILTER_FOLDER);

		filesFolders.sort((Path a, Path b) -> a.getFileName().toString()
				.compareToIgnoreCase(b.getFileName().toString()));
		for (Path file : filesFolders) {
			if (file == null) {
				continue;
			}

			name = file.getFileName().toString();

			List<Path> files = FileUtils.getAllFilesInDirectory(file);
			item = files.isEmpty() ? Items.BUCKET : Items.WATER_BUCKET;

			this.getSchematicListSlots().add(new SchematicLoadList.Entry(SchematicFormat.readMetaFromFile(file),
					name, Files.isDirectory(file), item, this.schematicLoadList));

		}

		List<Path> filesSchematics = FileUtils.getAllFilesInDirectory(this.currentDirectory, FILE_FILTER_SCHEMATIC);
		if (filesSchematics.isEmpty()) {
			this.getSchematicListSlots().add(new SchematicLoadList.Entry(null, this.strNoSchematic.getString(),
					Blocks.DIRT, this.schematicLoadList, false));
		} else {
			filesSchematics.sort((Path a, Path b) -> a.getFileName().toString()
					.compareToIgnoreCase(b.getFileName().toString()));
			for (Path file : filesSchematics) {
				name = file.getFileName().toString();

				this.getSchematicListSlots().add(new SchematicLoadList.Entry(SchematicFormat.readMetaFromFile(file),
						null, Files.isDirectory(file), null,
						this.schematicLoadList));
			}
		}

		schematicLoadList.syncEntries();
	}

	public List<SchematicLoadList.Entry> getSchematicListSlots() {
		return schematicListSlots;
	}

	protected void changeDirectory(String directory) {
		this.currentDirectory = this.currentDirectory.resolve(directory);

		try {
			this.currentDirectory = this.currentDirectory.toRealPath().normalize();
		} catch (IOException ioe) {
			Reference.logger.error("Failed to canonize directory!", ioe);
		}

		reloadSchematics();
	}
}