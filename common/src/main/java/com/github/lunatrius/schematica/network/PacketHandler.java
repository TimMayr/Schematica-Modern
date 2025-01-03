package com.github.lunatrius.schematica.network;

import com.github.lunatrius.schematica.network.message.*;
import com.github.lunatrius.schematica.reference.Reference;
import dev.architectury.networking.NetworkManager;
import io.netty.channel.Channel;
import net.minecraft.resources.ResourceLocation;

//TODO: Fix as soon as I get a reply in the discord
public class PacketHandler {
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	public static final Channel INSTANCE =
			NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Reference.MOD_ID, "main_channel"))
			                              .clientAcceptedVersions(PROTOCOL_VERSION::equals)
			                              .serverAcceptedVersions(PROTOCOL_VERSION::equals)
			                              .networkProtocolVersion(() -> PROTOCOL_VERSION)
			                              .simpleChannel();

	public static void init() {
		NetworkManager.registerS2CPayloadType(MessageCapabilities.TYPE,MessageCapabilities.STREAM_CODEC);
		INSTANCE.registerMessage(disc++, MessageCapabilities.class, MessageCapabilities::encode,
		                         MessageCapabilities::decode, MessageCapabilities::handle);
		INSTANCE.registerMessage(disc++, MessageDownloadBegin.class, MessageDownloadBegin::encode,
		                         MessageDownloadBegin::decode, MessageDownloadBegin::handle);
		INSTANCE.registerMessage(disc++, MessageDownloadBeginAck.class, MessageDownloadBeginAck::encode,
		                         MessageDownloadBeginAck::decode, MessageDownloadBeginAck::handle);
		INSTANCE.registerMessage(disc++, MessageDownloadChunk.class, MessageDownloadChunk::encode,
		                         MessageDownloadChunk::decode, MessageDownloadChunk::handle);
		INSTANCE.registerMessage(disc++, MessageDownloadChunkAck.class, MessageDownloadChunkAck::encode,
		                         MessageDownloadChunkAck::decode, MessageDownloadChunkAck::handle);
		INSTANCE.registerMessage(disc++, MessageDownloadEnd.class, MessageDownloadEnd::encode,
		                         MessageDownloadEnd::decode, MessageDownloadEnd::handle);
	}
}