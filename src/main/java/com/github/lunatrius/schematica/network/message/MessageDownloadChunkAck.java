package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDownloadChunkAck {
	private int baseX;
	private int baseY;
	private int baseZ;

	public MessageDownloadChunkAck() {
	}

	public MessageDownloadChunkAck(int baseX, int baseY, int baseZ) {
		this.baseX = baseX;
		this.baseY = baseY;
		this.baseZ = baseZ;
	}


	public static MessageDownloadChunkAck decode(ByteBuf buf) {
		return new MessageDownloadChunkAck(buf.readInt(), buf.readInt(), buf.readInt());
	}

	public static void encode(MessageDownloadChunkAck msg, ByteBuf buf) {
		buf.writeInt(msg.baseX);
		buf.writeInt(msg.baseY);
		buf.writeInt(msg.baseZ);
	}


	public static void handle(MessageDownloadChunkAck msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			SchematicTransfer transfer = DownloadHandler.INSTANCE.transferMap.get(player);

			if (transfer != null) {
				transfer.confirmChunk(msg.baseX, msg.baseY, msg.baseZ);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
