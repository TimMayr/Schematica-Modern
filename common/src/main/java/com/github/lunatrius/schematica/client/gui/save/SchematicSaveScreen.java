package com.github.lunatrius.schematica.client.gui.save;

import com.github.lunatrius.schematica.core.BaseScreen;
import com.github.lunatrius.schematica.core.NumericFieldWidget;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class SchematicSaveScreen extends BaseScreen {
	private final Component strSaveSelection = Component.translatable(Names.Gui.Save.SAVE_SELECTION);
	private final Component strX = Component.translatable(Names.Gui.X);
	private final Component strY = Component.translatable(Names.Gui.Y);
	private final Component strZ = Component.translatable(Names.Gui.Z);
	private int centerX = 0;
	private int centerY = 0;
	private NumericFieldWidget numericAX = null;
	private NumericFieldWidget numericAY = null;
	private NumericFieldWidget numericAZ = null;
	private NumericFieldWidget numericBX = null;
	private NumericFieldWidget numericBY = null;
	private NumericFieldWidget numericBZ = null;
	private CycleButton<SchematicFormat> buttonFormat = null;
	private EditBox editBoxFilename = null;
	private String filename = "";
	private boolean isSavePrivate = true;

	public SchematicSaveScreen(Screen parent) {
		super(parent);
	}

	@Override
	public boolean charTyped(char character, int code) {
		this.filename = this.editBoxFilename.getValue();
		return super.charTyped(character, code);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		guiGraphics.drawString(this.minecraft.font, this.strSaveSelection, this.width - 205, this.height - 70,
				0xFFFFFF);

		guiGraphics.drawString(this.minecraft.font, this.strX, this.centerX - 145, this.centerY - 24, 0xFFFFFF);
		guiGraphics.drawString(this.minecraft.font, Integer.toString(ClientProxy.pointA.x), this.centerX - 25,
				this.centerY - 24, 0xFFFFFF);

		guiGraphics.drawString(this.minecraft.font, this.strY, this.centerX - 145, this.centerY + 1, 0xFFFFFF);
		guiGraphics.drawString(this.minecraft.font, Integer.toString(ClientProxy.pointA.y), this.centerX - 25,
				this.centerY + 1, 0xFFFFFF);

		guiGraphics.drawString(this.minecraft.font, this.strZ, this.centerX - 145, this.centerY + 26, 0xFFFFFF);
		guiGraphics.drawString(this.minecraft.font, Integer.toString(ClientProxy.pointA.z), this.centerX - 25,
				this.centerY + 26, 0xFFFFFF);

		guiGraphics.drawString(this.minecraft.font, this.strX, this.centerX + 15, this.centerY - 24, 0xFFFFFF);
		guiGraphics.drawString(this.minecraft.font, Integer.toString(ClientProxy.pointB.x), this.centerX + 135,
				this.centerY - 24, 0xFFFFFF);

		guiGraphics.drawString(this.minecraft.font, this.strY, this.centerX + 15, this.centerY + 1, 0xFFFFFF);
		guiGraphics.drawString(this.minecraft.font, Integer.toString(ClientProxy.pointB.y), this.centerX + 135,
				this.centerY + 1, 0xFFFFFF);

		guiGraphics.drawString(this.minecraft.font, this.strZ, this.centerX + 15, this.centerY + 26, 0xFFFFFF);
		guiGraphics.drawString(this.minecraft.font, Integer.toString(ClientProxy.pointB.z), this.centerX + 135,
				this.centerY + 26, 0xFFFFFF);
	}

	@Override
	public boolean keyPressed(int character, int code, int modifiers) {
		this.filename = this.editBoxFilename.getValue();
		return super.keyPressed(character, code, modifiers);
	}

	@Override
	public void init() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.clearWidgets();

		Button buttonPointA = Button.builder(Component.translatable(Names.Gui.Save.POINT_RED), (button) -> {
			ClientProxy.movePointToPlayer(ClientProxy.pointA);
			ClientProxy.updatePoints();
			setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
		}).bounds(this.centerX - 130, this.centerY - 55, 100, 20).build();
		this.addRenderableWidget(buttonPointA);


		this.numericAX = new NumericFieldWidget(this.centerX - 130, this.centerY - 30, (button) -> {
			ClientProxy.pointA.x = this.numericAX.getValue();
			ClientProxy.updatePoints();
		});
		this.addRenderableWidget(this.numericAX);


		this.numericAY = new NumericFieldWidget(this.centerX - 130, this.centerY - 5, (button) -> {
			ClientProxy.pointA.y = this.numericAY.getValue();
			ClientProxy.updatePoints();
		});
		this.addRenderableWidget(this.numericAY);


		this.numericAZ = new NumericFieldWidget(this.centerX - 130, this.centerY + 20, (button) -> {
			ClientProxy.pointA.z = this.numericAZ.getValue();
			ClientProxy.updatePoints();
		});
		this.addRenderableWidget(this.numericAZ);


		Button buttonPointB = Button.builder(Component.translatable(Names.Gui.Save.POINT_BLUE), (button) -> {
			ClientProxy.movePointToPlayer(ClientProxy.pointB);
			ClientProxy.updatePoints();
			setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
		}).bounds(this.centerX + 30, this.centerY - 55, 100, 20).build();
		this.addRenderableWidget(buttonPointB);


		this.numericBX = new NumericFieldWidget(this.centerX + 30, this.centerY - 30, (button) -> {
			ClientProxy.pointB.x = this.numericBX.getValue();
			ClientProxy.updatePoints();
		});
		this.addRenderableWidget(this.numericBX);


		this.numericBY = new NumericFieldWidget(this.centerX + 30, this.centerY - 5, (button) -> {
			ClientProxy.pointB.y = this.numericBY.getValue();
			ClientProxy.updatePoints();
		});
		this.addRenderableWidget(this.numericBY);


		this.numericBZ = new NumericFieldWidget(this.centerX + 30, this.centerY + 20, (button) -> {
			ClientProxy.pointB.z = this.numericBZ.getValue();
			ClientProxy.updatePoints();
		});
		this.addRenderableWidget(this.numericBZ);

		this.editBoxFilename = new EditBox(this.minecraft.font, this.width - 205, this.height - 30, 150, 20,
				Component.empty());
		this.addRenderableWidget(this.editBoxFilename);


		Button buttonSave = Button.builder(Component.translatable(Names.Gui.Save.SAVE), (button) -> {
			this.filename = this.editBoxFilename.getValue();
			String filename = this.editBoxFilename.getValue() + SchematicFormat.getExtension(this.getFormatName());

			Path directory = Reference.proxy.getSchematicDirectory();
			Path file = directory.resolve(filename);

			if (!Files.exists(file)) {
				if (Reference.proxy.saveSchematic(this.minecraft.player, filename, this.minecraft.level,
						this.getFormatName(), ClientProxy.pointMin, ClientProxy.pointMax, this.isSavePrivate, "")) {
					this.minecraft.setScreen(this.parent);
				}
			} else {
				this.minecraft.setScreen(new SchematicSaveConfirmScreen(this, this.parent, filename,
						this.getFormatName(), this.isSavePrivate));
			}

		}).bounds(this.width - 50, this.height - 30, 40, 20).build();
		this.addRenderableWidget(buttonSave);


		this.buttonFormat = new CycleButton.Builder<SchematicFormat>(o -> Component.translatable(o.getName()))
				.withInitialValue(SchematicFormat.getFormatFromName(SchematicFormat.FORMAT_DEFAULT))
				.withValues(SchematicFormat.FORMATS.values())
				.create(this.width - 150, this.height - 55, 140, 20, Component.translatable(Names.Gui.Save.FORMAT));
		this.addRenderableWidget(this.buttonFormat);


		Button buttonVisibility = Button.builder(Component.translatable(Names.Gui.Save.PRIVATE), (button) -> {
			button.setMessage(this.isSavePrivate ?
					Component.translatable(Names.Gui.Save.PUBLIC) : Component.translatable(Names.Gui.Save.PRIVATE));
			this.isSavePrivate = !this.isSavePrivate;
		}).bounds(this.width - 205, this.height - 55, 50, 20).build();
		this.addRenderableWidget(buttonVisibility);


		this.editBoxFilename.setMaxLength(1024);
		this.editBoxFilename.setValue(this.filename);

		setMinMax(this.numericAX);
		setMinMax(this.numericAY);
		setMinMax(this.numericAZ);
		setMinMax(this.numericBX);
		setMinMax(this.numericBY);
		setMinMax(this.numericBZ);

		setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
		setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
	}

	private void setPoint(@NotNull NumericFieldWidget numX, @NotNull NumericFieldWidget numY,
	                      @NotNull NumericFieldWidget numZ, @NotNull BlockPos point) {
		numX.setValue(point.getX());
		numY.setValue(point.getY());
		numZ.setValue(point.getZ());
	}

	private @Nullable String getFormatName() {
		return this.buttonFormat.getValue().getNbtName();
	}

	private void setMinMax(@NotNull NumericFieldWidget numericField) {
		numericField.setMinimum(Constants.Level.MINIMUM_COORD);
		numericField.setMaximum(Constants.Level.MAXIMUM_COORD);
	}
}