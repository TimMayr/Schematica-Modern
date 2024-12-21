package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.NumericFieldWidget;
import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.util.FlipHelper;
import com.github.lunatrius.schematica.client.util.RotationHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.client.world.SchematicWorld.LayerMode;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

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
	private final Screen screen = this;
	private int centerX = 0;
	private int centerY = 0;
	private NumericFieldWidget numericX = null;
	private NumericFieldWidget numericY = null;
	private NumericFieldWidget numericZ = null;
	private Button btnLayerMode = null;
	private NumericFieldWidget nfLayer = null;
	private Button btnHide = null;
	private Button btnFlip = null;
	private Button btnRotate = null;
	private Button btnPrint = null;

	public GuiSchematicControl(Screen Screen) {
		super(Screen);
		this.schematic = ClientProxy.schematic;
		this.printer = SchematicPrinter.INSTANCE;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		// drawDefaultBackground();

		drawCenteredString(this.font, this.strMoveSchematic, this.centerX, this.centerY - 45, 0xFFFFFF);
		drawCenteredString(this.font, this.strMaterials, 50, this.height - 85, 0xFFFFFF);
		drawCenteredString(this.font, this.strPrinter, 50, this.height - 45, 0xFFFFFF);
		drawCenteredString(this.font, this.strOperations, this.width - 50, this.height - 120, 0xFFFFFF);

		drawString(this.font, this.strX, this.centerX - 65, this.centerY - 24, 0xFFFFFF);
		drawString(this.font, this.strY, this.centerX - 65, this.centerY + 1, 0xFFFFFF);
		drawString(this.font, this.strZ, this.centerX - 65, this.centerY + 26, 0xFFFFFF);

		super.render(mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean keyReleased(int character, int code, int modifiers) {
		boolean b = super.keyReleased(character, code, modifiers);

		if (this.btnFlip.active) {
			this.btnFlip.setFGColor(hasShiftDown() ? 0xFF0000 : 0xFFFFFF);
		}

		if (this.btnRotate.active) {
			this.btnRotate.setFGColor(hasShiftDown() ? 0xFF0000 : 0xFFFFFF);
		}

		return b;
	}

	@Override
	public boolean keyPressed(int character, int code, int modifiers) {
		boolean b = super.keyPressed(character, code, modifiers);

		if (this.btnFlip.active) {
			this.btnFlip.setFGColor(hasShiftDown() ? 0xFF0000 : 0xFFFFFF);
		}

		if (this.btnRotate.active) {
			this.btnRotate.setFGColor(hasShiftDown() ? 0xFF0000 : 0xFFFFFF);
		}

		return b;
	}

	@Override
	public void init() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.buttons.clear();

		this.numericX = new NumericFieldWidget(this.font, this.centerX - 50, this.centerY - 30, 100, 20, (button) -> {
			this.schematic.position.x = this.numericX.getValue();
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.buttons.add(this.numericX);

		this.numericY = new NumericFieldWidget(this.font, this.centerX - 50, this.centerY - 5, 100, 20, (button) -> {
			this.schematic.position.y = this.numericY.getValue();
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.buttons.add(this.numericY);

		this.numericZ = new NumericFieldWidget(this.font, this.centerX - 50, this.centerY + 20, 100, 20, (button) -> {
			this.schematic.position.z = this.numericZ.getValue();
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.buttons.add(this.numericZ);

		Button btnUnload = new Button(this.width - 90, this.height - 200, 80, 20, strUnload, (button) -> {
			Reference.proxy.unloadSchematic();
			this.minecraft.displayGuiScreen(this.parentScreen);
		});
		this.buttons.add(btnUnload);

		this.btnLayerMode = new Button(this.width - 90, this.height - 150 - 25, 80, 20, I18n.format(
				(this.schematic != null ? this.schematic.layerMode : LayerMode.ALL).name), (button) -> {
			this.schematic.layerMode = LayerMode.next(this.schematic.layerMode);
			this.btnLayerMode.setMessage(I18n.format(this.schematic.layerMode.name));
			this.nfLayer.setActive(this.schematic.layerMode != LayerMode.ALL);
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.buttons.add(this.btnLayerMode);

		this.nfLayer = new NumericFieldWidget(this.font, this.width - 90, this.height - 150, 80, 20, (button) -> {
			this.schematic.renderingLayer = this.nfLayer.getValue();
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.buttons.add(this.nfLayer);

		this.btnHide = new Button(this.width - 90, this.height - 105, 80, 20,
		                          this.schematic != null && this.schematic.isRendering ? this.strHide : this.strShow,
		                          (button) -> this.btnHide.setMessage(
				                          this.schematic.toggleRendering() ? this.strHide : this.strShow));
		this.buttons.add(this.btnHide);

		Button btnMove = new Button(this.width - 90, this.height - 80, 80, 20,
		                            I18n.format(Names.Gui.Control.MOVE_HERE),
		                            (button) -> {
			                            ClientProxy.moveSchematicToPlayer(this.schematic);
			                            //			                            RenderSchematic.getINSTANCE()
			                            //			                            .refresh();
			                            setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
		                            });
		this.buttons.add(btnMove);

		Button btnFlipDirection = new Button(this.width - 180, this.height - 55, 80, 20, I18n.format(
				Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisFlip.getName()), (button) -> {
			Direction[] values = Direction.values();
			ClientProxy.axisFlip = values[((ClientProxy.axisFlip.ordinal() + 2) % values.length)];
			button.setMessage(I18n.format(Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisFlip.getName()));
		});
		this.buttons.add(btnFlipDirection);

		this.btnFlip =
				new Button(this.width - 90, this.height - 55, 80, 20, "↔ " + " " + I18n.format(Names.Gui.Control.FLIP),
				           (button) -> {
					           if (FlipHelper.INSTANCE.flip(this.schematic, ClientProxy.axisFlip, hasShiftDown())) {
						           //				RenderSchematic.getINSTANCE().refresh();
						           SchematicPrinter.INSTANCE.refresh();
					           }
				           });
		this.buttons.add(this.btnFlip);

		Button btnRotateDirection = new Button(this.width - 180, this.height - 30, 80, 20, I18n.format(
				Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisRotation.getName()), (button) -> {
			Direction[] values = Direction.values();
			ClientProxy.axisRotation = values[((ClientProxy.axisRotation.ordinal() + 1) % values.length)];
			button.setMessage(I18n.format(Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisRotation.getName()));
		});
		this.buttons.add(btnRotateDirection);

		this.btnRotate = new Button(this.width - 90, this.height - 30, 80, 20,
		                            "↻ " + " " + I18n.format(Names.Gui.Control.ROTATE), (button) -> {
			if (RotationHelper.INSTANCE.rotate(this.schematic, ClientProxy.axisRotation, hasShiftDown())) {
				setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
				//				RenderSchematic.getINSTANCE().refresh();
				SchematicPrinter.INSTANCE.refresh();
			}
		});
		this.buttons.add(this.btnRotate);

		Button btnMaterials = new Button(10, this.height - 70, 80, 20, this.strMaterials,
		                                 (button) -> this.minecraft.displayGuiScreen(
				                                 new GuiSchematicMaterials(screen)));
		this.buttons.add(btnMaterials);

		this.btnPrint = new Button(10, this.height - 30, 80, 20, this.printer.isPrinting() ? this.strOn : this.strOff,
		                           (button) -> {
			                           boolean isPrinting = this.printer.togglePrinting();
			                           this.btnPrint.setMessage(isPrinting ? this.strOn : this.strOff);
		                           });
		this.buttons.add(this.btnPrint);

		this.numericX.setActive(this.schematic != null);
		this.numericY.setActive(this.schematic != null);
		this.numericZ.setActive(this.schematic != null);

		btnUnload.active = this.schematic != null;
		this.btnLayerMode.active = this.schematic != null;
		this.nfLayer.setActive(this.schematic != null && this.schematic.layerMode != LayerMode.ALL);

		this.btnHide.active = this.schematic != null;
		btnMove.active = this.schematic != null;
		btnFlipDirection.active = this.schematic != null;
		this.btnFlip.active = this.schematic != null;
		btnRotateDirection.active = this.schematic != null;
		this.btnRotate.active = this.schematic != null;
		btnMaterials.active = this.schematic != null;
		this.btnPrint.active = this.schematic != null && this.printer.isEnabled();

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
}