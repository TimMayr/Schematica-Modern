package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class NBTSync {
	protected final Minecraft minecraft = Minecraft.getInstance();

	public abstract boolean execute(Player player, Level schematic, BlockPos pos, Level mcWorld, BlockPos mcPos);

	public <T extends INetHandler> boolean sendPacket(IPacket<T> packet) {
		ClientPlayNetHandler connection = this.minecraft.getConnection();
		if (connection == null) {
			return false;
		}

		connection.sendPacket(packet);
		return true;
	}
}