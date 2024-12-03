package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.NumericFieldWidget;
import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.client.util.FlipHelper;
import com.github.lunatrius.schematica.client.util.RotationHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.client.world.SchematicWorld.LayerMode;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class GuiSchematicControl extends ScreenBase {
	private final SchematicWorld schematic;
	private final SchematicPrinter printer;
	private final String strMoveSchematic = I18n.format(Names.Gui.Control.MOVE_SCHEMATIC);
	private final String strOperations = I18n.format(Names.Gui.Control.OPERATIONS);
	private final String strUnload = I18n.format(Names.Gui.Control.UNLOAD);
	private final String strMaterials = I18n.format(Names.Gui.Control.MATERIALS);
	private final String strPrinter = I18n.format(Names.Gui.Control.PRINTER);
	private final String strHide = I18n.format(Names.Gui.Control.HIDE);
	private final String strShow = I18n.format(Names.Gui.Control.SHOW);
	private final String strX = I18n.format(Names.Gui.X);
	private final String strY = I18n.format(Names.Gui.Y);
	private final String strZ = I18n.format(Names.Gui.Z);
	private final String strOn = I18n.format(Names.Gui.ON);
	private final String strOff = I18n.format(Names.Gui.OFF);
	private int centerX = 0;
	private int centerY = 0;
	private NumericFieldWidget numericX = null;
	private NumericFieldWidget numericY = null;
	private NumericFieldWidget numericZ = null;
	private Button btnUnload = null;
	private Button btnLayerMode = null;
	private NumericFieldWidget nfLayer = null;
	private Button btnHide = null;
	private Button btnMove = null;
	private Button btnFlipDirection = null;
	private Button btnFlip = null;
	private Button btnRotateDirection = null;
	private Button btnRotate = null;
	private Button btnMaterials = null;
	private Button btnPrint = null;

	public GuiSchematicControl(Screen Screen) {
		super(Screen);
		this.schematic = ClientProxy.schematic;
		this.printer = SchematicPrinter.INSTANCE;
	}

	@Override
	public void initGui() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.buttonList.clear();

		int id = 0;

		this.numericX = new NumericFieldWidget(this.fontRenderer, id++, this.centerX - 50, this.centerY - 30, 100, 20);
		this.buttonList.add(this.numericX);

		this.numericY = new NumericFieldWidget(this.fontRenderer, id++, this.centerX - 50, this.centerY - 5, 100, 20);
		this.buttonList.add(this.numericY);

		this.numericZ = new NumericFieldWidget(this.fontRenderer, id++, this.centerX - 50, this.centerY + 20, 100, 20);
		this.buttonList.add(this.numericZ);

		this.btnUnload = new Button(id++, this.width - 90, this.height - 200, 80, 20, this.strUnload);
		this.buttonList.add(this.btnUnload);

		this.btnLayerMode = new Button(id++, this.width - 90, this.height - 150 - 25, 80, 20, I18n.format(
				(this.schematic != null ? this.schematic.layerMode : LayerMode.ALL).name));
		this.buttonList.add(this.btnLayerMode);

		this.nfLayer = new NumericFieldWidget(this.fontRenderer, id++, this.width - 90, this.height - 150, 80, 20);
		this.buttonList.add(this.nfLayer);

		this.btnHide = new Button(id++, this.width - 90, this.height - 105, 80, 20,
		                          this.schematic != null && this.schematic.isRendering ? this.strHide : this.strShow);
		this.buttonList.add(this.btnHide);

		this.btnMove =
				new Button(id++, this.width - 90, this.height - 80, 80, 20, I18n.format(Names.Gui.Control.MOVE_HERE));
		this.buttonList.add(this.btnMove);

		this.btnFlipDirection = new Button(id++, this.width - 180, this.height - 55, 80, 20, I18n.format(
				Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisFlip.getName()));
		this.buttonList.add(this.btnFlipDirection);

		this.btnFlip = new GuiUnicodeGlyphButton(id++, this.width - 90, this.height - 55, 80, 20,
		                                         " " + I18n.format(Names.Gui.Control.FLIP), "\u2194", 2.0f);
		this.buttonList.add(this.btnFlip);

		this.btnRotateDirection = new Button(id++, this.width - 180, this.height - 30, 80, 20, I18n.format(
				Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisRotation.getName()));
		this.buttonList.add(this.btnRotateDirection);

		this.btnRotate = new GuiUnicodeGlyphButton(id++, this.width - 90, this.height - 30, 80, 20,
		                                           " " + I18n.format(Names.Gui.Control.ROTATE), "\u21bb", 2.0f);
		this.buttonList.add(this.btnRotate);

		this.btnMaterials = new Button(id++, 10, this.height - 70, 80, 20, this.strMaterials);
		this.buttonList.add(this.btnMaterials);

		this.btnPrint =
				new Button(id++, 10, this.height - 30, 80, 20, this.printer.isPrinting() ? this.strOn : this.strOff);
		this.buttonList.add(this.btnPrint);

		this.numericX.setEnabled(this.schematic != null);
		this.numericY.setEnabled(this.schematic != null);
		this.numericZ.setEnabled(this.schematic != null);

		this.btnUnload.enabled = this.schematic != null;
		this.btnLayerMode.enabled = this.schematic != null;
		this.nfLayer.setEnabled(this.schematic != null && this.schematic.layerMode != LayerMode.ALL);

		this.btnHide.enabled = this.schematic != null;
		this.btnMove.enabled = this.schematic != null;
		this.btnFlipDirection.enabled = this.schematic != null;
		this.btnFlip.enabled = this.schematic != null;
		this.btnRotateDirection.enabled = this.schematic != null;
		this.btnRotate.enabled = this.schematic != null;
		this.btnMaterials.enabled = this.schematic != null;
		this.btnPrint.enabled = this.schematic != null && this.printer.isEnabled();

		setMinMax(this.numericX);
		setMinMax(this.numericY);
		setMinMax(this.numericZ);

		if (this.schematic != null) {
			setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
		}

		this.nfLayer.setMinimum(0);
		this.nfLayer.setMaximum(this.schematic != null ? this.schematic.getHeight() - 1 : 0);
		if (this.schematic != null) {
			this.nfLayer.setValue(this.schematic.renderingLayer);
		}
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

	@Override
	protected void actionPerformed(Button Button) {
		if (Button.enabled) {
			if (this.schematic == null) {
				return;
			}

			if (Button.id == this.numericX.id) {
				this.schematic.position.x = this.numericX.getValue();
				RenderSchematic.INSTANCE.refresh();
			} else if (Button.id == this.numericY.id) {
				this.schematic.position.y = this.numericY.getValue();
				RenderSchematic.INSTANCE.refresh();
			} else if (Button.id == this.numericZ.id) {
				this.schematic.position.z = this.numericZ.getValue();
				RenderSchematic.INSTANCE.refresh();
			} else if (Button.id == this.btnUnload.id) {
				Schematica.proxy.unloadSchematic();
				this.mc.displayGuiScreen(this.parentScreen);
			} else if (Button.id == this.btnLayerMode.id) {
				this.schematic.layerMode = LayerMode.next(this.schematic.layerMode);
				this.btnLayerMode.displayString = I18n.format(this.schematic.layerMode.name);
				this.nfLayer.setEnabled(this.schematic.layerMode != LayerMode.ALL);
				RenderSchematic.INSTANCE.refresh();
			} else if (Button.id == this.nfLayer.id) {
				this.schematic.renderingLayer = this.nfLayer.getValue();
				RenderSchematic.INSTANCE.refresh();
			} else if (Button.id == this.btnHide.id) {
				this.btnHide.displayString = this.schematic.toggleRendering() ? this.strHide : this.strShow;
			} else if (Button.id == this.btnMove.id) {
				ClientProxy.moveSchematicToPlayer(this.schematic);
				RenderSchematic.INSTANCE.refresh();
				setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
			} else if (Button.id == this.btnFlipDirection.id) {
				EnumFacing[] values = EnumFacing.VALUES;
				ClientProxy.axisFlip = values[((ClientProxy.axisFlip.ordinal() + 2) % values.length)];
				Button.displayString =
                        I18n.format(Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisFlip.getName());
			} else if (Button.id == this.btnFlip.id) {
				if (FlipHelper.INSTANCE.flip(this.schematic, ClientProxy.axisFlip, isShiftKeyDown())) {
					RenderSchematic.INSTANCE.refresh();
					SchematicPrinter.INSTANCE.refresh();
				}
			} else if (Button.id == this.btnRotateDirection.id) {
				EnumFacing[] values = EnumFacing.VALUES;
				ClientProxy.axisRotation = values[((ClientProxy.axisRotation.ordinal() + 1) % values.length)];
				Button.displayString =
						I18n.format(Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisRotation.getName());
			} else if (Button.id == this.btnRotate.id) {
				if (RotationHelper.INSTANCE.rotate(this.schematic, ClientProxy.axisRotation, isShiftKeyDown())) {
					setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
					RenderSchematic.INSTANCE.refresh();
					SchematicPrinter.INSTANCE.refresh();
				}
			} else if (Button.id == this.btnMaterials.id) {
				this.mc.displayGuiScreen(new GuiSchematicMaterials(this));
			} else if (Button.id == this.btnPrint.id && this.printer.isEnabled()) {
				boolean isPrinting = this.printer.togglePrinting();
				this.btnPrint.displayString = isPrinting ? this.strOn : this.strOff;
			}
		}
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		super.handleKeyboardInput();

		if (this.btnFlip.enabled) {
			this.btnFlip.packedFGColour = isShiftKeyDown() ? 0xFF0000 : 0x000000;
		}

		if (this.btnRotate.enabled) {
			this.btnRotate.packedFGColour = isShiftKeyDown() ? 0xFF0000 : 0x000000;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// drawDefaultBackground();

		drawCenteredString(this.fontRenderer, this.strMoveSchematic, this.centerX, this.centerY - 45, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, this.strMaterials, 50, this.height - 85, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, this.strPrinter, 50, this.height - 45, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, this.strOperations, this.width - 50, this.height - 120, 0xFFFFFF);

		drawString(this.fontRenderer, this.strX, this.centerX - 65, this.centerY - 24, 0xFFFFFF);
		drawString(this.fontRenderer, this.strY, this.centerX - 65, this.centerY + 1, 0xFFFFFF);
		drawString(this.fontRenderer, this.strZ, this.centerX - 65, this.centerY + 26, 0xFFFFFF);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
