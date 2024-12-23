package com.github.lunatrius.schematica.client.gui.save;

import com.github.lunatrius.core.client.gui.NumericFieldWidget;
import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.Iterator;

public class GuiSchematicSave extends ScreenBase {
	private final String strSaveSelection = I18n.format(Names.Gui.Save.SAVE_SELECTION);
	private final String strX = I18n.format(Names.Gui.X);
	private final String strY = I18n.format(Names.Gui.Y);
	private final String strZ = I18n.format(Names.Gui.Z);
	private final String strOn = I18n.format(Names.Gui.ON);
	private final String strOff = I18n.format(Names.Gui.OFF);
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
	private TextFieldWidget tfFilename = null;
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

	public GuiSchematicSave(Screen guiScreen) {
		super(guiScreen);
		this.format = nextFormat();
	}

	/**
	 * Advances the format iterator, reseting it as needed.
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
	public void render(int mouseX, int mouseY, float partialTicks) {
		drawString(this.minecraft.fontRenderer, this.strSaveSelection, this.width - 205, this.height - 70, 0xFFFFFF);

		drawString(this.minecraft.fontRenderer, this.strX, this.centerX - 145, this.centerY - 24, 0xFFFFFF);
		drawString(this.minecraft.fontRenderer, Integer.toString(ClientProxy.pointA.x), this.centerX - 25,
		           this.centerY - 24, 0xFFFFFF);

		drawString(this.minecraft.fontRenderer, this.strY, this.centerX - 145, this.centerY + 1, 0xFFFFFF);
		drawString(this.minecraft.fontRenderer, Integer.toString(ClientProxy.pointA.y), this.centerX - 25,
		           this.centerY + 1, 0xFFFFFF);

		drawString(this.minecraft.fontRenderer, this.strZ, this.centerX - 145, this.centerY + 26, 0xFFFFFF);
		drawString(this.minecraft.fontRenderer, Integer.toString(ClientProxy.pointA.z), this.centerX - 25,
		           this.centerY + 26, 0xFFFFFF);

		drawString(this.minecraft.fontRenderer, this.strX, this.centerX + 15, this.centerY - 24, 0xFFFFFF);
		drawString(this.minecraft.fontRenderer, Integer.toString(ClientProxy.pointB.x), this.centerX + 135,
		           this.centerY - 24, 0xFFFFFF);

		drawString(this.minecraft.fontRenderer, this.strY, this.centerX + 15, this.centerY + 1, 0xFFFFFF);
		drawString(this.minecraft.fontRenderer, Integer.toString(ClientProxy.pointB.y), this.centerX + 135,
		           this.centerY + 1, 0xFFFFFF);

		drawString(this.minecraft.fontRenderer, this.strZ, this.centerX + 15, this.centerY + 26, 0xFFFFFF);
		drawString(this.minecraft.fontRenderer, Integer.toString(ClientProxy.pointB.z), this.centerX + 135,
		           this.centerY + 26, 0xFFFFFF);

		super.render(mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean charTyped(char character, int code) {
		this.filename = this.tfFilename.getText();
		return super.charTyped(character, code);
	}

	@Override
	public boolean keyPressed(int character, int code, int modifiers) {
		this.filename = this.tfFilename.getText();
		return super.keyPressed(character, code, modifiers);
	}

	@Override
	public void init() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.buttons.clear();

		Button btnPointA =
				new Button(this.centerX - 130, this.centerY - 55, 100, 20, I18n.format(Names.Gui.Save.POINT_RED),
				           (button) -> {
					           ClientProxy.movePointToPlayer(ClientProxy.pointA);
					           ClientProxy.updatePoints();
					           setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
				           });
		this.buttons.add(btnPointA);

		this.numericAX =
				new NumericFieldWidget(this.minecraft.fontRenderer, this.centerX - 130, this.centerY - 30,
				                       (button) -> {
					ClientProxy.pointA.x = this.numericAX.getValue();
					ClientProxy.updatePoints();
				});
		this.buttons.add(this.numericAX);

		this.numericAY =
				new NumericFieldWidget(this.minecraft.fontRenderer, this.centerX - 130, this.centerY - 5, (button) -> {
					ClientProxy.pointA.y = this.numericAY.getValue();
					ClientProxy.updatePoints();
				});
		this.buttons.add(this.numericAY);

		this.numericAZ =
				new NumericFieldWidget(this.minecraft.fontRenderer, this.centerX - 130, this.centerY + 20,
				                       (button) -> {
					ClientProxy.pointA.z = this.numericAZ.getValue();
					ClientProxy.updatePoints();
				});
		this.buttons.add(this.numericAZ);

		Button btnPointB =
				new Button(this.centerX + 30, this.centerY - 55, 100, 20, I18n.format(Names.Gui.Save.POINT_BLUE),
				           (button) -> {
					           ClientProxy.movePointToPlayer(ClientProxy.pointB);
					           ClientProxy.updatePoints();
					           setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
				           });
		this.buttons.add(btnPointB);

		this.numericBX =
				new NumericFieldWidget(this.minecraft.fontRenderer, this.centerX + 30, this.centerY - 30, (button) -> {
					ClientProxy.pointB.x = this.numericBX.getValue();
					ClientProxy.updatePoints();
				});
		this.buttons.add(this.numericBX);

		this.numericBY =
				new NumericFieldWidget(this.minecraft.fontRenderer, this.centerX + 30, this.centerY - 5, (button) -> {
					ClientProxy.pointB.y = this.numericBY.getValue();
					ClientProxy.updatePoints();
				});
		this.buttons.add(this.numericBY);

		this.numericBZ =
				new NumericFieldWidget(this.minecraft.fontRenderer, this.centerX + 30, this.centerY + 20, (button) -> {
					ClientProxy.pointB.z = this.numericBZ.getValue();
					ClientProxy.updatePoints();
				});
		this.buttons.add(this.numericBZ);

		this.btnEnable = new Button(this.width - 210, this.height - 55, 50, 20,
		                            ClientProxy.isRenderingGuide && Reference.proxy.isSaveEnabled
		                            ? this.strOn
		                            : this.strOff, (button) -> {
			ClientProxy.isRenderingGuide = !ClientProxy.isRenderingGuide && Reference.proxy.isSaveEnabled;
			this.btnEnable.setMessage(ClientProxy.isRenderingGuide ? this.strOn : this.strOff);
			this.btnSave.active = ClientProxy.isRenderingGuide || ClientProxy.schematic != null;
			this.btnFormat.active = ClientProxy.isRenderingGuide || ClientProxy.schematic != null;
		});
		this.buttons.add(this.btnEnable);

		this.tfFilename =
				new TextFieldWidget(this.minecraft.fontRenderer, this.width - 209, this.height - 29, 153, 18, "");
		this.textFields.add(this.tfFilename);

		this.btnSave =
				new Button(this.width - 50, this.height - 30, 40, 20, I18n.format(Names.Gui.Save.SAVE), (button) -> {
					String path = this.tfFilename.getText() + SchematicFormat.getExtension(this.format);
					if (ClientProxy.isRenderingGuide) {
						if (Reference.proxy.saveSchematic(this.minecraft.player,
						                                  SchematicaClientConfig.schematicDirectory, path,
						                                  this.minecraft.world, this.format, ClientProxy.pointMin,
						                                  ClientProxy.pointMax)) {
							this.tfFilename.setText(this.filename);
							this.minecraft.displayGuiScreen(this.parentScreen);
						}
					} else {
						SchematicFormat.writeToFileAndNotify(new File(SchematicaClientConfig.schematicDirectory, path),
						                                     this.format, ClientProxy.schematic.getSchematic(),
						                                     this.minecraft.player);
					}
				});
		this.btnSave.active =
				ClientProxy.isRenderingGuide && Reference.proxy.isSaveEnabled || ClientProxy.schematic != null;
		this.buttons.add(this.btnSave);

		this.btnFormat = new Button(this.width - 155, this.height - 55, 145, 20, I18n.format(Names.Gui.Save.FORMAT,
		                                                                                     I18n.format(
				                                                                                     SchematicFormat.getFormatName(
						                                                                                     this.format))),
		                            (button) -> {
			                            this.format = nextFormat();
			                            this.btnFormat.setMessage(I18n.format(Names.Gui.Save.FORMAT, I18n.format(
					                            SchematicFormat.getFormatName(this.format))));
		                            });
		this.btnFormat.active =
				ClientProxy.isRenderingGuide && Reference.proxy.isSaveEnabled || ClientProxy.schematic != null;
		this.buttons.add(this.btnFormat);

		this.tfFilename.setMaxStringLength(1024);
		this.tfFilename.setText(this.filename);

		setMinMax(this.numericAX);
		setMinMax(this.numericAY);
		setMinMax(this.numericAZ);
		setMinMax(this.numericBX);
		setMinMax(this.numericBY);
		setMinMax(this.numericBZ);

		setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
		setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
	}

	private void setMinMax(NumericFieldWidget numericField) {
		numericField.setMinimum(Constants.World.MINIMUM_COORD);
		numericField.setMaximum(Constants.World.MAXIMUM_COORD);
	}

	private void setPoint(NumericFieldWidget numX, NumericFieldWidget numY, NumericFieldWidget numZ, BlockPos point) {
		numX.setValue(point.getX());
		numY.setValue(point.getY());
		numZ.setValue(point.getZ());
	}
}