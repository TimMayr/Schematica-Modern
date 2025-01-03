package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDownloadBeginAck {
	public static void handle(MessageDownloadBeginAck ignoredMsg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			SchematicTransfer transfer = DownloadHandler.INSTANCE.transferMap.get(player);

			if (transfer != null) {
				transfer.setState(SchematicTransfer.State.CHUNK_WAIT);
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static MessageDownloadBeginAck decode(ByteBuf ignoredBuf) {
		return new MessageDownloadBeginAck();
	}

	@SuppressWarnings("EmptyMethod")
	public static void encode(MessageDownloadBeginAck ignoredMsg, ByteBuf ignoredBuf) {

	}
}