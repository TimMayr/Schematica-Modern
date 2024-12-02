package com.github.lunatrius.schematica.handler.client;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GuiHandler {
	public static final GuiHandler INSTANCE = new GuiHandler();

	@SubscribeEvent
	public void onGuiOpen(final GuiOpenEvent event) {
		if (SchematicPrinter.INSTANCE.isPrinting()) {
			if (event.getGui() instanceof EditSignScreen) {
				event.setGui(null);
			}
		}
	}
}
