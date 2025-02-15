package com.github.lunatrius.schematica.client.gui.save;

import com.github.lunatrius.schematica.core.BaseScreen;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SchematicSaveConfirmScreen extends BaseScreen {
	private final String filename;
	private final String format;
	private final boolean isSavePrivate;
	private final Screen superParent;

	public SchematicSaveConfirmScreen(Screen parent, Screen superParent, String filename, @Nullable String formatName,
	                                  boolean isSavePrivate) {
		super(parent);
		this.filename = filename;
		this.format = formatName;
		this.isSavePrivate = isSavePrivate;
		this.superParent = superParent;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.minecraft.font,
				Component.translatable(Names.Command.Save.Message.CONFIRM_MESSAGE), (this.width / 2),
				(this.height / 2) - 30, 0xFFFFFF);
	}

	@Override
	public void init() {
		this.clearWidgets();

		Button buttonBack = Button.builder(Component.translatable(Names.Gui.BACK),
				(button) -> this.minecraft.setScreen(parent)).bounds((this.width / 2) - 105, (this.height / 2) - 10,
				100, 20).build();
		this.addRenderableWidget(buttonBack);

		Button buttonContinue = Button.builder(Component.translatable(Names.Gui.CONTINUE),
				(button) -> {
					if (Reference.proxy.saveSchematic(this.minecraft.player, filename, this.minecraft.level,
							this.format, ClientProxy.pointMin, ClientProxy.pointMax, this.isSavePrivate, "")) {
						this.minecraft.setScreen(this.superParent);
					}
				}).bounds((this.width / 2) + 5, (this.height / 2) - 10,
				100, 20).build();
		this.addRenderableWidget(buttonContinue);
	}
}