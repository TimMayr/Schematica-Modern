package com.github.lunatrius.schematica.reference;

import com.github.lunatrius.schematica.proxy.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
	public static final String MODID = "${modid}";
	public static final Logger logger = LogManager.getLogger(Reference.MODID);
	public static CommonProxy proxy;
}