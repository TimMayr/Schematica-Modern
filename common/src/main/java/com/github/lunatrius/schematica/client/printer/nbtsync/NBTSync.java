package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class NBTSync {
	protected final Minecraft minecraft = Minecraft.getInstance();

	public abstract boolean execute(Player player, Level schematic, BlockPos pos, Level mcWorld, BlockPos mcPos);

	public <T extends PacketListener> boolean sendPacket(Packet<T> packet) {
		ClientPacketListener connection = this.minecraft.getConnection();
		if (connection == null) {
			return false;
		}

		connection.send(packet);
		return true;
	}
}