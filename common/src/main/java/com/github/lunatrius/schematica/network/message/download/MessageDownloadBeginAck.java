package com.github.lunatrius.schematica.network.message.download;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.message.MessageCapabilities;
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
import org.jetbrains.annotations.NotNull;

//ack value is needed because the StreamCodec needs something to decode
@MethodsReturnNonnullByDefault
public record MessageDownloadBeginAck(boolean ack) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MessageCapabilities> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_BEGIN_ACK_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadBeginAck> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.BOOL, MessageDownloadBeginAck::ack,
					MessageDownloadBeginAck::new);

	public static void handle(@NotNull PacketContext<MessageDownloadBeginAck> ctx) {
		if (ctx.side() == Side.SERVER) {
			if (ctx.message().ack()) {
				Player player = ctx.sender();
				SchematicTransfer transfer = DownloadHandler.INSTANCE.transferMap.get(player.getScoreboardName());

				if (transfer != null) {
					transfer.setState(SchematicTransfer.State.CHUNK_WAIT);
				}
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}