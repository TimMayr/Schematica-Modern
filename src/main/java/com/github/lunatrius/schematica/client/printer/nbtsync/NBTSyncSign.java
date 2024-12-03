package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.Arrays;

public class NBTSyncSign extends NBTSync {
	@Override
	public boolean execute(EntityPlayer player, World schematic, BlockPos pos, World mcWorld, BlockPos mcPos) {
		TileEntity tileEntity = schematic.getTileEntity(pos);
		TileEntity mcTileEntity = mcWorld.getTileEntity(mcPos);

		if (tileEntity instanceof TileEntitySign && mcTileEntity instanceof TileEntitySign) {
			ITextComponent[] signText = ((TileEntitySign) tileEntity).signText;
			ITextComponent[] mcSignText = ((TileEntitySign) mcTileEntity).signText;

			if (!Arrays.equals(signText, mcSignText)) {
				return sendPacket(new CPacketUpdateSign(mcPos, signText));
			}
		}

		return false;
	}
}
