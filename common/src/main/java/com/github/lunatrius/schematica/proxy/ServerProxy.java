package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.config.SchematicaClientConfig;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class ServerProxy extends CommonProxy {
	public static WeakReference<MinecraftServer> serverWeakReference = null;

	@Override
	public File getDataDirectory() {
		MinecraftServer server = ServerProxy.serverWeakReference != null ? ServerProxy.serverWeakReference.get() :
		                         null;
		File file = server != null ? server.getFile(".") : new File(".");
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			Reference.logger.warn("Could not canonize path!", e);
		}
		return file;
	}

	@Override
	public boolean loadSchematic(PlayerEntity player, File directory, String filename) {
		return false;
	}

	@Override
	public boolean isPlayerQuotaExceeded(PlayerEntity player) {
		int spaceUsed = 0;

		//Space used by private directory
		File schematicDirectory = getPlayerSchematicDirectory(player, true);
		spaceUsed += getSpaceUsedByDirectory(schematicDirectory);

		//Space used by public directory
		schematicDirectory = getPlayerSchematicDirectory(player, false);
		spaceUsed += getSpaceUsedByDirectory(schematicDirectory);
		return ((spaceUsed / 1024) > SchematicaConfig.SERVER.playerQuotaKilobytes.get());
	}

	private int getSpaceUsedByDirectory(File directory) {
		int spaceUsed = 0;
		//If we don't have a player directory yet, then they haven't uploaded any files yet.
		if (directory == null || !directory.exists()) {
			return 0;
		}

		File[] files = directory.listFiles();
		if (files == null) {
			files = new File[0];
		}
		for (File path : files) {
			spaceUsed += (int) path.length();
		}
		return spaceUsed;
	}

	@Override
	public File getPlayerSchematicDirectory(PlayerEntity player, boolean privateDirectory) {
		UUID playerId = player.getUniqueID();

		File playerDir = new File(SchematicaClientConfig.schematicDirectory.getAbsolutePath(), playerId.toString());
		if (privateDirectory) {
			return new File(playerDir, "private");
		} else {
			return new File(playerDir, "public");
		}
	}
}
