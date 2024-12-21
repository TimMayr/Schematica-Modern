package com.github.lunatrius.schematica.reference;

import com.github.lunatrius.schematica.proxy.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
	public static final String MODID = "${modid}";
	public static final Logger logger = LogManager.getLogger(Reference.MODID);
	public static String NAME = "${modname}";
	public static String VERSION = "${modversion}";
	public static String FORGE = "${version_forge}";
	public static String MINECRAFT = "${version_minecraft}";
	public static String GUI_FACTORY = "com.github.lunatrius.schematica.client.gui.config.GuiFactory";
	public static CommonProxy proxy;
}