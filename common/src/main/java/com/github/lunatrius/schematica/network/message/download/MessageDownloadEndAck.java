package com.github.lunatrius.schematica.network.message.download;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.message.MessageCapabilities;
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
public record MessageDownloadEndAck(boolean ack) implements CustomPacketPayload {
	public static final Type<MessageCapabilities> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_END_ACK_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadEndAck> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.BOOL, MessageDownloadEndAck::ack,
					MessageDownloadEndAck::new);

	public static void handle(@NotNull PacketContext<MessageDownloadEndAck> ctx) {
		if (ctx.side() == Side.SERVER) {
			if (ctx.message().ack()) {
				Player player = ctx.sender();
				DownloadHandler.INSTANCE.transferMap.remove(player.getScoreboardName());
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}