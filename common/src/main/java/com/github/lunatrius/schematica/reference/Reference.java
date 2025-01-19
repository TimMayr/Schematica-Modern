package com.github.lunatrius.schematica.reference;

import com.github.lunatrius.schematica.proxy.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
	public static final String MOD_ID = "${mod_id}";
	public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);
	public static CommonProxy proxy;
}