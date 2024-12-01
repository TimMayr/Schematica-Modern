package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Reference.MODID)
public class Schematica {
	public static Schematica instance;

	public Schematica() {
		instance = this;
		MinecraftForge.EVENT_BUS.register(this);
		Reference.proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
	}
}
