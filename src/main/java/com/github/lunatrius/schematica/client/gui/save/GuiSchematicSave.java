package com.github.lunatrius.schematica.client.gui.save;

import com.github.lunatrius.core.client.gui.NumericFieldWidget;
import com.github.lunatrius.core.client.gui.ScreenBase;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.handler.SchematicaClientConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class GuiSchematicSave extends ScreenBase {
    private int centerX = 0;
    private int centerY = 0;

    private Button btnPointA = null;

    private NumericFieldWidget numericAX = null;
    private NumericFieldWidget numericAY = null;
    private NumericFieldWidget numericAZ = null;

    private Button btnPointB = null;

    private NumericFieldWidget numericBX = null;
    private NumericFieldWidget numericBY = null;
    private NumericFieldWidget numericBZ = null;

    private Button btnEnable = null;
    private Button btnFormat = null;
    private Button btnSave = null;
    private TextFieldWidget tfFilename = null;

    private String filename = "";

    /** The currently selected format */
    private String format;
    /**
     * An iterator that gets new formats from {@link SchematicFormat#FORMATS}.
     * <p>
     * Is reset after it no longer has new elements.
     */
    private Iterator<String> formatIterator = null;

    private final String strSaveSelection = I18n.format(Names.Gui.Save.SAVE_SELECTION);
    private final String strX = I18n.format(Names.Gui.X);
    private final String strY = I18n.format(Names.Gui.Y);
    private final String strZ = I18n.format(Names.Gui.Z);
    private final String strOn = I18n.format(Names.Gui.ON);
    private final String strOff = I18n.format(Names.Gui.OFF);

    public GuiSchematicSave(final Screen guiScreen) {
        super(guiScreen);
        this.format = nextFormat();
    }

    @Override
    public void initGui() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.buttonList.clear();

        int id = 0;

        this.btnPointA = new Button(id++, this.centerX - 130, this.centerY - 55, 100, 20, I18n.format(Names.Gui.Save.POINT_RED));
        this.buttonList.add(this.btnPointA);

        this.numericAX = new NumericFieldWidget(this.fontRenderer, id++, this.centerX - 130, this.centerY - 30);
        this.buttonList.add(this.numericAX);

        this.numericAY = new NumericFieldWidget(this.fontRenderer, id++, this.centerX - 130, this.centerY - 5);
        this.buttonList.add(this.numericAY);

        this.numericAZ = new NumericFieldWidget(this.fontRenderer, id++, this.centerX - 130, this.centerY + 20);
        this.buttonList.add(this.numericAZ);

        this.btnPointB = new Button(id++, this.centerX + 30, this.centerY - 55, 100, 20, I18n.format(Names.Gui.Save.POINT_BLUE));
        this.buttonList.add(this.btnPointB);

        this.numericBX = new NumericFieldWidget(this.fontRenderer, id++, this.centerX + 30, this.centerY - 30);
        this.buttonList.add(this.numericBX);

        this.numericBY = new NumericFieldWidget(this.fontRenderer, id++, this.centerX + 30, this.centerY - 5);
        this.buttonList.add(this.numericBY);

        this.numericBZ = new NumericFieldWidget(this.fontRenderer, id++, this.centerX + 30, this.centerY + 20);
        this.buttonList.add(this.numericBZ);

        this.btnEnable = new Button(id++, this.width - 210, this.height - 55, 50, 20, ClientProxy.isRenderingGuide && Schematica.proxy.isSaveEnabled ? this.strOn : this.strOff);
        this.buttonList.add(this.btnEnable);

        this.tfFilename = new GuiTextField(id++, this.fontRenderer, this.width - 209, this.height - 29, 153, 18);
        this.textFields.add(this.tfFilename);

        this.btnSave = new Button(id++, this.width - 50, this.height - 30, 40, 20, I18n.format(Names.Gui.Save.SAVE));
        this.btnSave.enabled = ClientProxy.isRenderingGuide && Schematica.proxy.isSaveEnabled || ClientProxy.schematic != null;
        this.buttonList.add(this.btnSave);

        this.btnFormat = new Button(id++, this.width - 155, this.height - 55, 145, 20, I18n.format(Names.Gui.Save.FORMAT, I18n.format(SchematicFormat.getFormatName(this.format))));
        this.btnFormat.enabled = ClientProxy.isRenderingGuide && Schematica.proxy.isSaveEnabled || ClientProxy.schematic != null;
        this.buttonList.add(this.btnFormat);

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

    private void setMinMax(final NumericFieldWidget numericField) {
        numericField.setMinimum(Constants.World.MINIMUM_COORD);
        numericField.setMaximum(Constants.World.MAXIMUM_COORD);
    }

    private void setPoint(final NumericFieldWidget numX, final NumericFieldWidget numY, final NumericFieldWidget numZ, final BlockPos point) {
        numX.setValue(point.getX());
        numY.setValue(point.getY());
        numZ.setValue(point.getZ());
    }

    @Override
    protected void actionPerformed(final Button Button) {
        if (Button.enabled) {
            if (Button.id == this.btnPointA.id) {
                ClientProxy.movePointToPlayer(ClientProxy.pointA);
                ClientProxy.updatePoints();
                setPoint(this.numericAX, this.numericAY, this.numericAZ, ClientProxy.pointA);
            } else if (Button.id == this.numericAX.id) {
                ClientProxy.pointA.x = this.numericAX.getValue();
                ClientProxy.updatePoints();
            } else if (Button.id == this.numericAY.id) {
                ClientProxy.pointA.y = this.numericAY.getValue();
                ClientProxy.updatePoints();
            } else if (Button.id == this.numericAZ.id) {
                ClientProxy.pointA.z = this.numericAZ.getValue();
                ClientProxy.updatePoints();
            } else if (Button.id == this.btnPointB.id) {
                ClientProxy.movePointToPlayer(ClientProxy.pointB);
                ClientProxy.updatePoints();
                setPoint(this.numericBX, this.numericBY, this.numericBZ, ClientProxy.pointB);
            } else if (Button.id == this.numericBX.id) {
                ClientProxy.pointB.x = this.numericBX.getValue();
                ClientProxy.updatePoints();
            } else if (Button.id == this.numericBY.id) {
                ClientProxy.pointB.y = this.numericBY.getValue();
                ClientProxy.updatePoints();
            } else if (Button.id == this.numericBZ.id) {
                ClientProxy.pointB.z = this.numericBZ.getValue();
                ClientProxy.updatePoints();
            } else if (Button.id == this.btnEnable.id) {
                ClientProxy.isRenderingGuide = !ClientProxy.isRenderingGuide && Schematica.proxy.isSaveEnabled;
                this.btnEnable.displayString = ClientProxy.isRenderingGuide ? this.strOn : this.strOff;
                this.btnSave.enabled = ClientProxy.isRenderingGuide || ClientProxy.schematic != null;
                this.btnFormat.enabled = ClientProxy.isRenderingGuide || ClientProxy.schematic != null;
            } else if (Button.id == this.btnFormat.id) {
                this.format = nextFormat();
                this.btnFormat.displayString = I18n.format(Names.Gui.Save.FORMAT, I18n.format(SchematicFormat.getFormatName(this.format)));
            } else if (Button.id == this.btnSave.id) {
                final String path = this.tfFilename.getText() + SchematicFormat.getExtension(this.format);
                if (ClientProxy.isRenderingGuide) {
                    if (Schematica.proxy.saveSchematic(this.mc.player, SchematicaClientConfig.schematicDirectoryPath, path, this.mc.world, this.format, ClientProxy.pointMin, ClientProxy.pointMax)) {
                        this.filename = "";
                        this.tfFilename.setText(this.filename);
                    }
                } else {
                    SchematicFormat.writeToFileAndNotify(new File(SchematicaClientConfig.schematicDirectoryPath, path), this.format, ClientProxy.schematic.getSchematic(), this.mc.player);
                }
            }
        }
    }

    @Override
    protected void keyTyped(final char character, final int code) throws IOException {
        super.keyTyped(character, code);
        this.filename = this.tfFilename.getText();
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        // drawDefaultBackground();

        drawString(this.fontRenderer, this.strSaveSelection, this.width - 205, this.height - 70, 0xFFFFFF);

        drawString(this.fontRenderer, this.strX, this.centerX - 145, this.centerY - 24, 0xFFFFFF);
        drawString(this.fontRenderer, Integer.toString(ClientProxy.pointA.x), this.centerX - 25, this.centerY - 24, 0xFFFFFF);

        drawString(this.fontRenderer, this.strY, this.centerX - 145, this.centerY + 1, 0xFFFFFF);
        drawString(this.fontRenderer, Integer.toString(ClientProxy.pointA.y), this.centerX - 25, this.centerY + 1, 0xFFFFFF);

        drawString(this.fontRenderer, this.strZ, this.centerX - 145, this.centerY + 26, 0xFFFFFF);
        drawString(this.fontRenderer, Integer.toString(ClientProxy.pointA.z), this.centerX - 25, this.centerY + 26, 0xFFFFFF);

        drawString(this.fontRenderer, this.strX, this.centerX + 15, this.centerY - 24, 0xFFFFFF);
        drawString(this.fontRenderer, Integer.toString(ClientProxy.pointB.x), this.centerX + 135, this.centerY - 24, 0xFFFFFF);

        drawString(this.fontRenderer, this.strY, this.centerX + 15, this.centerY + 1, 0xFFFFFF);
        drawString(this.fontRenderer, Integer.toString(ClientProxy.pointB.y), this.centerX + 135, this.centerY + 1, 0xFFFFFF);

        drawString(this.fontRenderer, this.strZ, this.centerX + 15, this.centerY + 26, 0xFFFFFF);
        drawString(this.fontRenderer, Integer.toString(ClientProxy.pointB.z), this.centerX + 135, this.centerY + 26, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
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
            assert SchematicFormat.FORMATS.size() > 0 : "No formats are defined!";
            assert SchematicFormat.FORMATS.containsKey(SchematicFormat.FORMAT_DEFAULT) : "The default format does not exist!";

            this.formatIterator = SchematicFormat.FORMATS.keySet().iterator();
            while (!this.formatIterator.next().equals(SchematicFormat.FORMAT_DEFAULT)) {
                continue;
            }
            return SchematicFormat.FORMAT_DEFAULT;
        }

        if (!this.formatIterator.hasNext()) {
            this.formatIterator = SchematicFormat.FORMATS.keySet().iterator();
        }
        return this.formatIterator.next();
    }
}
