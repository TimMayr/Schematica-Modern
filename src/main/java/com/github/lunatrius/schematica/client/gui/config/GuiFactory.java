package com.github.lunatrius.schematica.client.gui.config;

import com.github.lunatrius.core.client.gui.config.GuiConfigComplex;
import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class GuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraftInstance) {
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new GuiModConfig(parentScreen);
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	public static class GuiModConfig extends GuiConfigComplex {
		public GuiModConfig(GuiScreen guiScreen) {
			super(guiScreen, Reference.MODID, SchematicaClientConfig.configuration, Names.Config.LANG_PREFIX);
		}
	}
}
