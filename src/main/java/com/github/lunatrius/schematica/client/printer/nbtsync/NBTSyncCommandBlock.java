package com.github.lunatrius.schematica.client.printer.nbtsync;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NBTSyncCommandBlock extends NBTSync {
	@Override
	public boolean execute(EntityPlayer player, World schematic, BlockPos pos, World mcWorld, BlockPos mcPos) {
		TileEntity tileEntity = schematic.getTileEntity(pos);
		TileEntity mcTileEntity = mcWorld.getTileEntity(mcPos);

		if (tileEntity instanceof TileEntityCommandBlock && mcTileEntity instanceof TileEntityCommandBlock) {
			CommandBlockBaseLogic commandBlockLogic = ((TileEntityCommandBlock) tileEntity).getCommandBlockLogic();
			CommandBlockBaseLogic mcCommandBlockLogic = ((TileEntityCommandBlock) mcTileEntity).getCommandBlockLogic();

			if (!commandBlockLogic.getCommand().equals(mcCommandBlockLogic.getCommand())) {
				PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

				packetBuffer.writeByte(mcCommandBlockLogic.getCommandBlockType());
				mcCommandBlockLogic.fillInInfo(packetBuffer);
				packetBuffer.writeString(commandBlockLogic.getCommand());
				packetBuffer.writeBoolean(mcCommandBlockLogic.shouldTrackOutput());

				return sendPacket(new CPacketCustomPayload("MC|AdvCdm", packetBuffer));
			}
		}

		return false;
	}
}
