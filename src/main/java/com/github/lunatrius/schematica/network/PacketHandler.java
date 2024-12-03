package com.github.lunatrius.schematica.network;

import com.github.lunatrius.schematica.network.message.*;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	public static final SimpleChannel INSTANCE =
			NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Reference.MODID, "main_channel"))
			                              .clientAcceptedVersions(PROTOCOL_VERSION::equals)
			                              .serverAcceptedVersions(PROTOCOL_VERSION::equals)
			                              .networkProtocolVersion(() -> PROTOCOL_VERSION)
			                              .simpleChannel();

	@SuppressWarnings("UnusedAssignment")
	public static void init() {
		int disc = 0;
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
