package com.github.lunatrius.schematica.client.printer.nbtsync;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class NBTSyncSign extends NBTSync {
	@Override
	public boolean execute(Player player, @NotNull Level schematic, BlockPos pos, @NotNull Level level,
	                       BlockPos mcPos) {
		BlockEntity blockEntity = schematic.getBlockEntity(pos);
		BlockEntity mcBlockEntity = level.getBlockEntity(mcPos);

		if (blockEntity instanceof SignBlockEntity && mcBlockEntity instanceof SignBlockEntity) {
			Component[] frontText = ((SignBlockEntity) blockEntity).getFrontText().getMessages(false);
			Component[] backText = ((SignBlockEntity) blockEntity).getBackText().getMessages(false);
			Component[] mcFrontText = ((SignBlockEntity) mcBlockEntity).getFrontText().getMessages(false);
			Component[] mcBackText = ((SignBlockEntity) mcBlockEntity).getBackText().getMessages(false);

			if (!Arrays.equals(backText, mcBackText)) {
				return sendPacket(
						new ServerboundSignUpdatePacket(mcPos, false, backText[0].getString(), backText[1].getString(),
						                                backText[2].getString(), backText[3].getString()));
			}

			if (!Arrays.equals(frontText, mcFrontText)) {
				return sendPacket(
						new ServerboundSignUpdatePacket(mcPos, true, backText[0].getString(), backText[1].getString(),
						                                backText[2].getString(), backText[3].getString()));
			}
		}

		return false;
	}
}