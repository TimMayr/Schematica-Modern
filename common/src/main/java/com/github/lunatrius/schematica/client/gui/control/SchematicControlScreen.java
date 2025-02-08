package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.core.client.gui.NumericFieldWidget;
import com.github.lunatrius.schematica.client.gui.core.BaseScreen;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.util.FlipHelper;
import com.github.lunatrius.schematica.client.util.RotationHelper;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SchematicControlScreen extends BaseScreen {
	private final FakeLevel schematic;
	private final SchematicPrinter printer;
	private final Component strMoveSchematic = Component.translatable(Names.Gui.Control.MOVE_SCHEMATIC);
	private final Component strOperations = Component.translatable(Names.Gui.Control.OPERATIONS);
	private final Component strUnload = Component.translatable(Names.Gui.Control.UNLOAD);
	private final Component strMaterials = Component.translatable(Names.Gui.Control.MATERIALS);
	private final Component strPrinter = Component.translatable(Names.Gui.Control.PRINTER);
	private final Component strHide = Component.translatable(Names.Gui.Control.HIDE);
	private final Component strShow = Component.translatable(Names.Gui.Control.SHOW);
	private final Component strX = Component.translatable(Names.Gui.X);
	private final Component strY = Component.translatable(Names.Gui.Y);
	private final Component strZ = Component.translatable(Names.Gui.Z);
	private final Component strOn = Component.translatable(Names.Gui.ON);
	private final Component strOff = Component.translatable(Names.Gui.OFF);
	private final Screen screen = this;
	private int centerX = 0;
	private int centerY = 0;
	private NumericFieldWidget numericX = null;
	private NumericFieldWidget numericY = null;
	private NumericFieldWidget numericZ = null;
	private Button buttonLayerMode = null;
	private NumericFieldWidget numericLayer = null;
	private Button buttonHide = null;
	private Button buttonPrint = null;

	public SchematicControlScreen(Screen parentScreen) {
		super(parentScreen);
		this.schematic = ClientProxy.schematic;
		this.printer = SchematicPrinter.INSTANCE;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		// drawDefaultBackground();

		guiGraphics.drawCenteredString(this.font, this.strMoveSchematic, this.centerX, this.centerY - 45, 0xFFFFFF);
		guiGraphics.drawCenteredString(this.font, this.strMaterials, 50, this.height - 85, 0xFFFFFF);
		guiGraphics.drawCenteredString(this.font, this.strPrinter, 50, this.height - 45, 0xFFFFFF);
		guiGraphics.drawCenteredString(this.font, this.strOperations, this.width - 50, this.height - 120, 0xFFFFFF);

		guiGraphics.drawString(this.font, this.strX, this.centerX - 65, this.centerY - 24, 0xFFFFFF);
		guiGraphics.drawString(this.font, this.strY, this.centerX - 65, this.centerY + 1, 0xFFFFFF);
		guiGraphics.drawString(this.font, this.strZ, this.centerX - 65, this.centerY + 26, 0xFFFFFF);

		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void init() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.clearWidgets();

		this.numericX = new NumericFieldWidget(this.centerX - 50, this.centerY - 30, 100, 20, (button) -> {
			this.schematic.getWorldPos().offset(this.numericX.getValue(), 0, 0);
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.addRenderableWidget(numericX);

		this.numericY = new NumericFieldWidget(this.centerX - 50, this.centerY - 5, 100, 20, (button) -> {
			this.schematic.getWorldPos().offset(0, this.numericY.getValue(), 0);
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.addRenderableWidget(this.numericY);

		this.numericZ = new NumericFieldWidget(this.centerX - 50, this.centerY + 20, 100, 20, (button) -> {
			this.schematic.getWorldPos().offset(0, 0, this.numericZ.getValue());
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.addRenderableWidget(this.numericZ);

		Button btnUnload = Button.builder(strUnload, (button) -> {
			Reference.proxy.unloadSchematic();
			this.minecraft.setScreen(this.parentScreen);
		}).pos(this.width - 90, this.height - 200).size(80, 20).build();
		this.addRenderableWidget(btnUnload);

		this.buttonLayerMode = Button.builder(Component.translatable(
				(this.schematic != null ? this.schematic.layerMode : FakeLevel.LayerMode.ALL).name), (button) -> {
			this.schematic.layerMode = FakeLevel.LayerMode.next(this.schematic.layerMode);
			this.buttonLayerMode.setMessage(Component.translatable(this.schematic.layerMode.name));
			this.numericLayer.setActive(this.schematic.layerMode != FakeLevel.LayerMode.ALL);
			//			RenderSchematic.getINSTANCE().refresh();
		}).bounds(this.width - 90, this.height - 150 - 25, 80, 20).build();
		this.addRenderableWidget(this.buttonLayerMode);

		this.numericLayer = new NumericFieldWidget(this.width - 90, this.height - 150, 80, 20, (button) -> {
			this.schematic.renderLayer = this.numericLayer.getValue();
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.addRenderableWidget(this.numericLayer);

		this.buttonHide =
				Button.builder(this.schematic != null && this.schematic.isRendering() ? this.strHide : this.strShow,
				               (button) -> this.buttonHide.setMessage(
						               this.schematic.toggleRendering() ? this.strHide : this.strShow))
				      .bounds(this.width - 90, this.height - 105, 80, 20)
				      .build();
		this.addRenderableWidget(this.buttonHide);

		Button btnMove = Button.builder(Component.translatable(Names.Gui.Control.MOVE_HERE), (button) -> {
			ClientProxy.moveSchematicToPlayer(this.schematic);
			//RenderSchematic.getINSTANCE().refresh();
			setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.getWorldPos());
		}).bounds(this.width - 90, this.height - 80, 80, 20).build();
		this.addRenderableWidget(btnMove);

		Button btnFlipDirection = Button.builder(
				Component.translatable(Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisFlip.getName()),
				(button) -> {
					Direction[] values = Direction.values();
					ClientProxy.axisFlip = values[((ClientProxy.axisFlip.ordinal() + 2) % values.length)];
					button.setMessage(Component.translatable(
							Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisFlip.getName()));
				}).bounds(this.width - 180, this.height - 55, 80, 20).build();
		this.addRenderableWidget(btnFlipDirection);

		//				RenderSchematic.getINSTANCE().refresh();
		Button btnFlip =
				Button.builder(Component.literal("↔ " + " ").append(Component.translatable(Names.Gui.Control.FLIP)),
				               (button) -> {
					               if (FlipHelper.INSTANCE.flip(this.schematic, ClientProxy.axisFlip,
					                                            hasShiftDown())) {
						               //				RenderSchematic.getINSTANCE().refresh();
						               SchematicPrinter.INSTANCE.refresh();
					               }
				               }).bounds(this.width - 90, this.height - 55, 80, 20).build();
		this.addRenderableWidget(btnFlip);

		Button btnRotateDirection = Button.builder(
				Component.translatable(Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisRotation.getName()),
				(button) -> {
					Direction[] values = Direction.values();
					ClientProxy.axisRotation = values[((ClientProxy.axisRotation.ordinal() + 1) % values.length)];
					button.setMessage(Component.translatable(
							Names.Gui.Control.TRANSFORM_PREFIX + ClientProxy.axisRotation.getName()));
				}).bounds(this.width - 180, this.height - 30, 80, 20).build();
		this.addRenderableWidget(btnRotateDirection);

		//				RenderSchematic.getINSTANCE().refresh();
		Button btnRotate =
				Button.builder(Component.literal("↻ " + " ").append(Component.translatable(Names.Gui.Control.ROTATE)),
				               (button) -> {
					               if (RotationHelper.INSTANCE.rotate(this.schematic, ClientProxy.axisRotation,
					                                                  hasShiftDown())) {
						               setPoint(this.numericX, this.numericY, this.numericZ,
						                        this.schematic.getWorldPos());
						               //				RenderSchematic.getINSTANCE().refresh();
						               SchematicPrinter.INSTANCE.refresh();
					               }
				               }).bounds(this.width - 90, this.height - 30, 80, 20).build();
		this.addRenderableWidget(btnRotate);

		Button btnMaterials = Button.builder(this.strMaterials,
		                                     (button) -> this.minecraft.setScreen(new SchematicMaterialsScreen(screen)))
		                            .bounds(10, this.height - 70, 80, 20)
		                            .build();
		this.addRenderableWidget(btnMaterials);

		this.buttonPrint = Button.builder(this.printer.isPrinting() ? this.strOn : this.strOff, (button) -> {
			boolean isPrinting = this.printer.togglePrinting();
			this.buttonPrint.setMessage(isPrinting ? this.strOn : this.strOff);
		}).bounds(10, this.height - 30, 80, 20).build();
		this.addRenderableWidget(this.buttonPrint);

		this.numericX.setActive(this.schematic != null);
		this.numericY.setActive(this.schematic != null);
		this.numericZ.setActive(this.schematic != null);

		btnUnload.active = this.schematic != null;
		this.buttonLayerMode.active = this.schematic != null;
		this.numericLayer.setActive(this.schematic != null && this.schematic.layerMode != FakeLevel.LayerMode.ALL);

		this.buttonHide.active = this.schematic != null;
		btnMove.active = this.schematic != null;
		btnFlipDirection.active = this.schematic != null;
		btnFlip.active = this.schematic != null;
		btnRotateDirection.active = this.schematic != null;
		btnRotate.active = this.schematic != null;
		btnMaterials.active = this.schematic != null;
		this.buttonPrint.active = this.schematic != null && this.printer.isEnabled();

		setMinMax(this.numericX);
		setMinMax(this.numericY);
		setMinMax(this.numericZ);

		if (this.schematic != null) {
			setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.getWorldPos());
		}

		this.numericLayer.setMinimum(0);
		this.numericLayer.setMaximum(this.schematic != null ? this.schematic.getHeight() - 1 : 0);
		if (this.schematic != null) {
			this.numericLayer.setValue(this.schematic.renderLayer);
		}
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