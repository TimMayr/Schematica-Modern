package com.github.lunatrius.schematica.client.gui.save;

import com.github.lunatrius.core.client.gui.NumericFieldWidget;
import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Iterator;

public class GuiSchematicSave extends ScreenBase {
	private final Component strSaveSelection = Component.translatable(Names.Gui.Save.SAVE_SELECTION);
	private final Component strX = Component.translatable(Names.Gui.X);
	private final Component strY = Component.translatable(Names.Gui.Y);
	private final Component strZ = Component.translatable(Names.Gui.Z);
	private final Component strOn = Component.translatable(Names.Gui.ON);
	private final Component strOff = Component.translatable(Names.Gui.OFF);
	private int centerX = 0;
	private int centerY = 0;
	private NumericFieldWidget numericAX = null;
	private NumericFieldWidget numericAY = null;
	private NumericFieldWidget numericAZ = null;
	private NumericFieldWidget numericBX = null;
	private NumericFieldWidget numericBY = null;
	private NumericFieldWidget numericBZ = null;
	private Button btnEnable = null;
	private Button btnFormat = null;
	private Button btnSave = null;
	private EditBox editBox = null;
	private String filename = "";
	/**
	 * The currently selected format
	 */
	private String format;
	/**
	 * An iterator that gets new formats from {@link SchematicFormat#FORMATS}.
	 * <p>
	 * Is reset after it no longer has new elements.
	 */
	private Iterator<String> formatIterator = null;

	public GuiSchematicSave(Screen screen) {
		super(screen);
		this.format = nextFormat();
	}

	/**
	 * Advances the format iterator, resetting it as needed.
	 * If the format iterator is null, initializes it to the default format.
	 *
	 * @return The next format value
	 */
	private String nextFormat() {
		if (this.formatIterator == null) {
			// First time; prime it so that it just returned the default value
			assert !SchematicFormat.FORMATS.isEmpty() : "No formats are defined!";
			assert SchematicFormat.FORMATS.containsKey(SchematicFormat.FORMAT_DEFAULT) :
					"The default format does not exist!";

			this.formatIterator = SchematicFormat.FORMATS.keySet().iterator();
		}

		if (!this.formatIterator.hasNext()) {
			this.formatIterator = SchematicFormat.FORMATS.keySet().iterator();
		}

		return this.formatIterator.next();
	}

	@Override
	public boolean charTyped(char character, int code) {
		this.filename = this.editBox.getValue();
		return super.charTyped(character, code);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
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

		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean keyPressed(int character, int code, int modifiers) {
		this.filename = this.editBox.getValue();
		return super.keyPressed(character, code, modifiers);
	}

	@Override
	public void init() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.clearWidgets();

		Button btnPointA = new PlainTextButton(this.centerX - 130, this.centerY - 55, 100, 20,
		                                       Component.translatable(Names.Gui.Save.POINT_RED), (button) -> {
			ClientProxy.movePointToPlayer(ClientProxy.pointA);
			ClientProxy.updatePoints();
			setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
		}, this.font);
		this.addRenderableWidget(btnPointA);

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

		Button btnPointB = new PlainTextButton(this.centerX + 30, this.centerY - 55, 100, 20,
		                                       Component.translatable(Names.Gui.Save.POINT_BLUE), (button) -> {
			ClientProxy.movePointToPlayer(ClientProxy.pointB);
			ClientProxy.updatePoints();
			setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
		}, this.font);
		this.addRenderableWidget(btnPointB);

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

		this.btnEnable = new PlainTextButton(this.width - 210, this.height - 55, 50, 20,
		                                     ClientProxy.isRenderingGuide && Reference.proxy.isSaveEnabled
		                                     ? this.strOn
		                                     : this.strOff, (button) -> {
			ClientProxy.isRenderingGuide = !ClientProxy.isRenderingGuide && Reference.proxy.isSaveEnabled;
			this.btnEnable.setMessage(ClientProxy.isRenderingGuide ? this.strOn : this.strOff);
			this.btnSave.active = ClientProxy.isRenderingGuide || ClientProxy.schematic != null;
			this.btnFormat.active = ClientProxy.isRenderingGuide || ClientProxy.schematic != null;
		}, this.font);
		this.addRenderableWidget(this.btnEnable);

		this.editBox = new EditBox(this.minecraft.font, this.width - 209, this.height - 29, 153, 18,
		                           Component.empty());
		this.addRenderableWidget(this.editBox);

		this.btnSave = new PlainTextButton(this.width - 50, this.height - 30, 40, 20,
		                                   Component.translatable(Names.Gui.Save.SAVE), (button) -> {
			String path = this.editBox.getValue() + SchematicFormat.getExtension(this.format);
			if (ClientProxy.isRenderingGuide) {
				if (Reference.proxy.saveSchematic(this.minecraft.player, SchematicaClientConfig.schematicDirectory,
				                                  path, this.minecraft.level, this.format, ClientProxy.pointMin,
				                                  ClientProxy.pointMax)) {
					this.editBox.setValue(this.filename);
					this.minecraft.setScreen(this.parentScreen);
				}
			} else {
				SchematicFormat.writeToFileAndNotify(new File(SchematicaClientConfig.schematicDirectory, path),
				                                     this.format, ClientProxy.schematic.getLevelSource(),
				                                     this.minecraft.player);
			}
		}, this.font);
		this.btnSave.active =
				ClientProxy.isRenderingGuide && Reference.proxy.isSaveEnabled || ClientProxy.schematic != null;
		this.addRenderableWidget(this.btnSave);

		this.btnFormat = new PlainTextButton(this.width - 155, this.height - 55, 145, 20,
		                                     Component.translatable(Names.Gui.Save.FORMAT, I18n.get(
				                                     SchematicFormat.getFormatName(this.format))), (button) -> {
			this.format = nextFormat();
			this.btnFormat.setMessage(Component.translatable(Names.Gui.Save.FORMAT,
			                                                 I18n.get(SchematicFormat.getFormatName(this.format))));
		}, this.font);
		this.btnFormat.active =
				ClientProxy.isRenderingGuide && Reference.proxy.isSaveEnabled || ClientProxy.schematic != null;
		this.addRenderableWidget(this.btnFormat);

		this.editBox.setMaxLength(1024);
		this.editBox.setValue(this.filename);

		setMinMax(this.numericAX);
		setMinMax(this.numericAY);
		setMinMax(this.numericAZ);
		setMinMax(this.numericBX);
		setMinMax(this.numericBY);
		setMinMax(this.numericBZ);

		setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
		setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
	}

	private void setMinMax(@NotNull NumericFieldWidget numericField) {
		numericField.setMinimum(Constants.Level.MINIMUM_COORD);
		numericField.setMaximum(Constants.Level.MAXIMUM_COORD);
	}

	private void setPoint(@NotNull NumericFieldWidget numX, @NotNull NumericFieldWidget numY,
	                      @NotNull NumericFieldWidget numZ, @NotNull BlockPos point) {
		numX.setValue(point.getX());
		numY.setValue(point.getY());
		numZ.setValue(point.getZ());
	}
}