package com.github.lunatrius.schematica.world.storage;

import com.mojang.datafixers.DataFixer;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SaveHandlerSchematic extends SaveHandler {
	public SaveHandlerSchematic(File directory, String filename, @Nullable MinecraftServer server,
	                            DataFixer dataFixer) {
		super(directory, filename, server, dataFixer);
	}

	@Override
	public void saveWorldInfoWithPlayer(final WorldInfo info, @Nullable final CompoundNBT compound) {}

	@Override
	public File getWorldDirectory() {
		return null;
	}

	@Override
	public void checkSessionLock() {}

	@Override
	public WorldInfo loadWorldInfo() {
		return null;
	}

	@Override
	public void saveWorldInfo(final WorldInfo info) {}

	@Override
	public String[] func_215771_d() {
		return null;
	}

	@Override
	public TemplateManager getStructureTemplateManager() {
		return null;
	}

	@Override
	public CompoundNBT getPlayerNBT(ServerPlayerEntity player) {
		return null;
	}
}
