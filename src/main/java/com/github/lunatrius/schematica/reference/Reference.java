package com.github.lunatrius.schematica.reference;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.proxy.CommonProxy;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
	public static final String MODID = "${modid}";
	public static final String NAME = "${modname}";
	public static final String VERSION = "${modversion}";
	public static final String FORGE = "${version_forge}";
	public static final String MINECRAFT = "${version_minecraft}";
	public static final String GUI_FACTORY = "com.github.lunatrius.schematica.client.gui.config.GuiFactory";

	public static Logger logger = LogManager.getLogger(Reference.MODID);
	public static CommonProxy proxy;
}
