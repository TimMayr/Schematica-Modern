package com.github.lunatrius.schematica.client.printer.nbtsync;


import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.jetbrains.annotations.NotNull;


public class NBTSyncCommandBlock extends NBTSync {
	@Override
	public boolean execute(Player player, @NotNull Level schematic, BlockPos pos, @NotNull Level level,
	                       BlockPos mcPos) {
		BlockEntity blockEntity = schematic.getBlockEntity(pos);
		BlockEntity mcBlockEntity = level.getBlockEntity(mcPos);

		if (blockEntity instanceof CommandBlockEntity && mcBlockEntity instanceof CommandBlockEntity) {
			BaseCommandBlock commandBlockLogic = ((CommandBlockEntity) blockEntity).getCommandBlock();
			BaseCommandBlock mcCommandBlockLogic = ((CommandBlockEntity) mcBlockEntity).getCommandBlock();

			if (!commandBlockLogic.getCommand().equals(mcCommandBlockLogic.getCommand())) {
				return sendPacket(new ServerboundSetCommandBlockPacket(pos, commandBlockLogic.getCommand(),
				                                                       ((CommandBlockEntity) blockEntity).getMode(),
				                                                       commandBlockLogic.isTrackOutput(), false,
				                                                       false));
			}
		}

		return false;
	}
}