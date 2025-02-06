package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;

public class SyncRegistry {
	public static final SyncRegistry INSTANCE = new SyncRegistry();

	static {
		INSTANCE.register(Blocks.COMMAND_BLOCK, new NBTSyncCommandBlock());
		INSTANCE.register(Blocks.OAK_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.SPRUCE_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BIRCH_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.ACACIA_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.CHERRY_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.JUNGLE_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.DARK_OAK_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.PALE_OAK_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.CRIMSON_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.WARPED_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.MANGROVE_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BAMBOO_SIGN, new NBTSyncSign());

		INSTANCE.register(Blocks.OAK_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.SPRUCE_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BIRCH_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.ACACIA_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.CHERRY_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.JUNGLE_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.DARK_OAK_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.PALE_OAK_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.CRIMSON_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.WARPED_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.MANGROVE_WALL_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BAMBOO_WALL_SIGN, new NBTSyncSign());

		INSTANCE.register(Blocks.OAK_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.SPRUCE_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BIRCH_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.ACACIA_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.CHERRY_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.JUNGLE_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.DARK_OAK_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.PALE_OAK_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.CRIMSON_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.WARPED_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.MANGROVE_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BAMBOO_HANGING_SIGN, new NBTSyncSign());

		INSTANCE.register(Blocks.OAK_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.SPRUCE_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BIRCH_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.ACACIA_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.CHERRY_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.JUNGLE_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.DARK_OAK_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.PALE_OAK_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.CRIMSON_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.WARPED_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.MANGROVE_WALL_HANGING_SIGN, new NBTSyncSign());
		INSTANCE.register(Blocks.BAMBOO_WALL_HANGING_SIGN, new NBTSyncSign());
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