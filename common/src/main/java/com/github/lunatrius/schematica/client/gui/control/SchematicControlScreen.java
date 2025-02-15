package com.github.lunatrius.schematica.client.gui.control;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.util.FlipHelper;
import com.github.lunatrius.schematica.client.util.RotationHelper;
import com.github.lunatrius.schematica.core.BaseScreen;
import com.github.lunatrius.schematica.core.NumericFieldWidget;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
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
	private NumericFieldWidget numericLayer = null;
	private Button buttonHide = null;
	private Button buttonPrint = null;

	public SchematicControlScreen(Screen parent) {
		super(parent);
		this.schematic = ClientProxy.schematic;
		this.printer = SchematicPrinter.INSTANCE;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
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

		this.numericX = new NumericFieldWidget(this.centerX - 50, this.centerY - 30, (button) -> {
			this.schematic.getWorldPos().offset(this.numericX.getValue(), 0, 0);
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.addRenderableWidget(numericX);


		this.numericY = new NumericFieldWidget(this.centerX - 50, this.centerY - 5, (button) -> {
			this.schematic.getWorldPos().offset(0, this.numericY.getValue(), 0);
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.addRenderableWidget(this.numericY);


		this.numericZ = new NumericFieldWidget(this.centerX - 50, this.centerY + 20, (button) -> {
			this.schematic.getWorldPos().offset(0, 0, this.numericZ.getValue());
			//			RenderSchematic.getINSTANCE().refresh();
		});
		this.addRenderableWidget(this.numericZ);


		Button buttonUnload = Button.builder(strUnload, (button) -> {
			Reference.proxy.unloadSchematic();
			this.minecraft.setScreen(this.parent);
		}).pos(this.width - 90, this.height - 200).size(80, 20).build();
		this.addRenderableWidget(buttonUnload);


		//				RenderSchematic.getINSTANCE().refresh();
		CycleButton<FakeLevel.LayerMode> buttonLayerMode =
				new CycleButton.Builder<FakeLevel.LayerMode>(l -> Component.translatable(l.name))
						.withInitialValue(FakeLevel.LayerMode.ALL)
						.withValues(FakeLevel.LayerMode.values())
						.displayOnlyValue()
						.create(this.width - 90, this.height - 150 - 25, 80, 20, Component.empty(),
								(button, layer) -> {
									this.schematic.layerMode = layer;
									this.numericLayer.setActive(this.schematic.layerMode != FakeLevel.LayerMode.ALL);
									//				RenderSchematic.getINSTANCE().refresh();
								});
		this.addRenderableWidget(buttonLayerMode);


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


		Button buttonMoveHere = Button.builder(Component.translatable(Names.Gui.Control.MOVE_HERE), (button) -> {
			ClientProxy.moveSchematicToPlayer(this.schematic);
			//RenderSchematic.getINSTANCE().refresh();
			setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.getWorldPos());
		}).bounds(this.width - 90, this.height - 80, 80, 20).build();
		this.addRenderableWidget(buttonMoveHere);


		CycleButton<Direction.Axis> buttonFlipDirection =
				new CycleButton.Builder<Direction.Axis>(axis -> Component.literal(axis.getName().toUpperCase()))
						.withInitialValue(Direction.Axis.X)
						.withValues(Direction.Axis.values())
						.displayOnlyValue()
						.create(this.width - 180, this.height - 55, 80, 20, Component.empty(),
								(button, axis) -> ClientProxy.axisFlip = axis);
		this.addRenderableWidget(buttonFlipDirection);

		//				RenderSchematic.getINSTANCE().refresh();
		Button buttonFlip =
				Button.builder(Component.literal("↔ " + " ").append(Component.translatable(Names.Gui.Control.FLIP)),
						(button) -> {
							if (FlipHelper.INSTANCE.flip(this.schematic, ClientProxy.axisFlip,
									hasShiftDown())) {
								//				RenderSchematic.getINSTANCE().refresh();
								SchematicPrinter.INSTANCE.refresh();
							}
						}).bounds(this.width - 90, this.height - 55, 80, 20).build();
		this.addRenderableWidget(buttonFlip);


		CycleButton<Direction> buttonRotateDirection =
				new CycleButton.Builder<Direction>(direction -> Component.translatable(
						Names.Gui.Control.TRANSFORM_PREFIX + direction.getName()))
						.withInitialValue(Direction.DOWN)
						.withValues(Direction.values())
						.displayOnlyValue()
						.create(this.width - 180, this.height - 30, 80, 20, Component.empty(),
								(button, direction) -> ClientProxy.axisRotation = direction);
		this.addRenderableWidget(buttonRotateDirection);


		//				RenderSchematic.getINSTANCE().refresh();
		Button buttonRotate =
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
		this.addRenderableWidget(buttonRotate);


		Button buttonMaterials = Button.builder(this.strMaterials,
						(button) -> this.minecraft.setScreen(new SchematicMaterialsScreen(screen)))
				.bounds(10, this.height - 70, 80, 20)
				.build();
		this.addRenderableWidget(buttonMaterials);


		this.buttonPrint = Button.builder(this.printer.isPrinting() ? this.strOn : this.strOff, (button) -> {
			boolean isPrinting = this.printer.togglePrinting();
			this.buttonPrint.setMessage(isPrinting ? this.strOn : this.strOff);
		}).bounds(10, this.height - 30, 80, 20).build();
		this.addRenderableWidget(this.buttonPrint);


		this.numericX.setActive(this.schematic != null);
		this.numericY.setActive(this.schematic != null);
		this.numericZ.setActive(this.schematic != null);

		buttonUnload.active = this.schematic != null;
		buttonLayerMode.active = this.schematic != null;
		this.numericLayer.setActive(this.schematic != null && this.schematic.layerMode != FakeLevel.LayerMode.ALL);

		this.buttonHide.active = this.schematic != null;
		buttonMoveHere.active = this.schematic != null;
		buttonFlipDirection.active = this.schematic != null;
		buttonFlip.active = this.schematic != null;
		buttonRotateDirection.active = this.schematic != null;
		buttonRotate.active = this.schematic != null;
		buttonMaterials.active = this.schematic != null;
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

	private void setPoint(@NotNull NumericFieldWidget numX, @NotNull NumericFieldWidget numY,
	                      @NotNull NumericFieldWidget numZ, @NotNull BlockPos point) {
		numX.setValue(point.getX());
		numY.setValue(point.getY());
		numZ.setValue(point.getZ());
	}

	private void setMinMax(@NotNull NumericFieldWidget numericField) {
		numericField.setMinimum(Constants.Level.MINIMUM_COORD);
		numericField.setMaximum(Constants.Level.MAXIMUM_COORD);
	}
}