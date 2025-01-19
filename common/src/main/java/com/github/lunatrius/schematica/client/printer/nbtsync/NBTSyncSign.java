package com.github.lunatrius.schematica.client.printer.nbtsync;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.Arrays;

public class NBTSyncSign extends NBTSync {
	@Override
	public boolean execute(Player player, Level schematic, BlockPos pos, Level level, BlockPos mcPos) {
		BlockEntity blockEntity = schematic.getBlockEntity(pos);
		BlockEntity mcBlockEntity = level.getBlockEntity(mcPos);

		if (blockEntity instanceof SignBlockEntity && mcBlockEntity instanceof SignBlockEntity) {
			Component[] frontText = ((SignBlockEntity) blockEntity).getFrontText().getMessages(false);
			Component[] backText = ((SignBlockEntity) blockEntity).getBackText().getMessages(false);
			Component[] mcFrontText = ((SignBlockEntity) mcBlockEntity).getFrontText().getMessages(false);
			Component[] mcBackText = ((SignBlockEntity) mcBlockEntity).getBackText().getMessages(false);

			if (!Arrays.equals(backText, mcBackText) && !Arrays.equals(frontText, mcFrontText)) {
				return sendPacket(new CUpdateSignPacket(mcPos, backText[0], backText[1], backText[2], backText[3]));
			}
		}

		return false;
	}
}