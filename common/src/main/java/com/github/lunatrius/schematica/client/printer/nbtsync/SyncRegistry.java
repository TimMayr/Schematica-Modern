package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.HashMap;

public class SyncRegistry {
	public static final SyncRegistry INSTANCE = new SyncRegistry();

	static {
		INSTANCE.register(Blocks.COMMAND_BLOCK, new NBTSyncCommandBlock());
		INSTANCE.register(Blocks.ACACIA_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.SPRUCE_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BIRCH_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.DARK_OAK_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.JUNGLE_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.OAK_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.ACACIA_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.SPRUCE_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BIRCH_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.DARK_OAK_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.JUNGLE_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.OAK_WALL_SIGN, new NBTSyncSign());
	}

	private final HashMap<Block, NBTSync> map = new HashMap<>();

	public void register(Block block, NBTSync handler) {
		if (block == null || handler == null) {
			return;
		}

		this.map.put(block, handler);
	}

	public NBTSync getHandler(Block block) {
		return this.map.get(block);
	}
}
