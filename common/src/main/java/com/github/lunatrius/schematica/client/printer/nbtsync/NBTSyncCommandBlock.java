package com.github.lunatrius.schematica.client.printer.nbtsync;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;


public class NBTSyncCommandBlock extends NBTSync {
	@Override
	public boolean execute(Player player, Level schematic, BlockPos pos, Level level, BlockPos mcPos) {
		BlockEntity blockEntity = schematic.getBlockEntity(pos);
		BlockEntity mcBlockEntity = level.getBlockEntity(mcPos);

		if (blockEntity instanceof CommandBlockEntity && mcBlockEntity instanceof CommandBlockEntity) {
			CommandBlockLogic commandBlockLogic = ((CommandBlockEntity) blockEntity).getCommandBlockLogic();
			CommandBlockLogic mcCommandBlockLogic = ((CommandBlockEntity) mcBlockEntity).getCommandBlockLogic();

			if (!commandBlockLogic.getCommand().equals(mcCommandBlockLogic.getCommand())) {
				return sendPacket(new CUpdateCommandBlockPacket(pos, mcCommandBlockLogic.getCommand(),
				                                                ((CommandBlockEntity) mcBlockEntity).getMode(),
				                                                mcCommandBlockLogic.shouldTrackOutput(), false,
				                                                false));
			}
		}

		return false;
	}
}