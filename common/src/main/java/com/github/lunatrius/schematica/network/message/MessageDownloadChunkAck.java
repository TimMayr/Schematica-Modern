package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import dev.architectury.networking.NetworkManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@MethodsReturnNonnullByDefault
public record MessageDownloadChunkAck(int baseX, int baseY, int baseZ) implements CustomPacketPayload {
	public static Type<MessageDownloadChunkAck> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_CHUNK_ACK_LOCATION));

	public static StreamCodec<RegistryFriendlyByteBuf, MessageDownloadChunkAck> StreamCodec =
			net.minecraft.network.codec.StreamCodec.composite(ByteBufCodecs.INT, MessageDownloadChunkAck::baseX,
			                                                  ByteBufCodecs.INT, MessageDownloadChunkAck::baseY,
			                                                  ByteBufCodecs.INT, MessageDownloadChunkAck::baseZ,
			                                                  MessageDownloadChunkAck::new);


	public static void handle(MessageDownloadChunkAck msg, NetworkManager.PacketContext ctx) {
		ctx.queue(() -> {
			Player player = ctx.getPlayer();
			SchematicTransfer transfer = DownloadHandler.INSTANCE.transferMap.get(player.getScoreboardName());

			if (transfer != null) {
				transfer.confirmChunk(msg.baseX, msg.baseY, msg.baseZ);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}