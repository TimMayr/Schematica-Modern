package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.Arrays;

public class NBTSyncSign extends NBTSync {
	@Override
	public boolean execute(PlayerEntity player, World schematic, BlockPos pos, World mcWorld, BlockPos mcPos) {
		TileEntity tileEntity = schematic.getTileEntity(pos);
		TileEntity mcTileEntity = mcWorld.getTileEntity(mcPos);

		if (tileEntity instanceof SignTileEntity && mcTileEntity instanceof SignTileEntity) {
			ITextComponent[] signText = ((SignTileEntity) tileEntity).signText;
			ITextComponent[] mcSignText = ((SignTileEntity) mcTileEntity).signText;

			if (!Arrays.equals(signText, mcSignText)) {
				return sendPacket(new CUpdateSignPacket(mcPos, signText[0], signText[1], signText[2], signText[3]));
			}
		}

		return false;
	}
}
