package com.github.lunatrius.schematica.client.printer.nbtsync;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CUpdateCommandBlockPacket;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NBTSyncCommandBlock extends NBTSync {
	@Override
	public boolean execute(PlayerEntity player, World schematic, BlockPos pos, World mcWorld, BlockPos mcPos) {
		TileEntity tileEntity = schematic.getTileEntity(pos);
		TileEntity mcTileEntity = mcWorld.getTileEntity(mcPos);

		if (tileEntity instanceof CommandBlockTileEntity && mcTileEntity instanceof CommandBlockTileEntity) {
			CommandBlockLogic commandBlockLogic = ((CommandBlockTileEntity) tileEntity).getCommandBlockLogic();
			CommandBlockLogic mcCommandBlockLogic = ((CommandBlockTileEntity) mcTileEntity).getCommandBlockLogic();

			if (!commandBlockLogic.getCommand().equals(mcCommandBlockLogic.getCommand())) {
				return sendPacket(new CUpdateCommandBlockPacket(pos, mcCommandBlockLogic.getCommand(),
				                                                ((CommandBlockTileEntity) mcTileEntity).getMode(),
				                                                mcCommandBlockLogic.shouldTrackOutput(), false,
				                                                false));
			}
		}

		return false;
	}
}
