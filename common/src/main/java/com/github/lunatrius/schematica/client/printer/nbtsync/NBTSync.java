package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class NBTSync {
	protected final Minecraft minecraft = Minecraft.getInstance();

	public abstract boolean execute(PlayerEntity player, World schematic, BlockPos pos, World mcWorld, BlockPos mcPos);

	public <T extends INetHandler> boolean sendPacket(IPacket<T> packet) {
		ClientPlayNetHandler connection = this.minecraft.getConnection();
		if (connection == null) {
			return false;
		}

		connection.sendPacket(packet);
		return true;
	}
}
