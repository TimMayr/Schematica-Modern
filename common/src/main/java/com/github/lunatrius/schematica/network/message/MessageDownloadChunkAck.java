package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@MethodsReturnNonnullByDefault
public record MessageDownloadChunkAck(boolean ack, int baseX, int baseY, int baseZ) implements CustomPacketPayload {
	public static Type<MessageDownloadChunkAck> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_CHUNK_ACK_LOCATION));

	public static StreamCodec<RegistryFriendlyByteBuf, MessageDownloadChunkAck> STREAM_CODEC =
			net.minecraft.network.codec.StreamCodec.composite(ByteBufCodecs.BOOL, MessageDownloadChunkAck::ack,
			                                                  ByteBufCodecs.INT, MessageDownloadChunkAck::baseX,
			                                                  ByteBufCodecs.INT, MessageDownloadChunkAck::baseY,
			                                                  ByteBufCodecs.INT, MessageDownloadChunkAck::baseZ,
			                                                  MessageDownloadChunkAck::new);


	public static void handle(PacketContext<MessageDownloadChunkAck> ctx) {
		if (ctx.side() == Side.SERVER) {
			if (ctx.message().ack()) {
				Player player = ctx.sender();
				SchematicTransfer transfer = DownloadHandler.INSTANCE.transferMap.get(player.getScoreboardName());

				if (transfer != null) {
					transfer.confirmChunk(ctx.message().baseX, ctx.message().baseY, ctx.message().baseZ);
				}
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}