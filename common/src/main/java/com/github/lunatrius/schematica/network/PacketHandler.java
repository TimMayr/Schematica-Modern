package com.github.lunatrius.schematica.network;

import com.github.lunatrius.schematica.network.message.MessageCapabilities;
import com.github.lunatrius.schematica.network.message.accounting.MessageAddSchematic;
import com.github.lunatrius.schematica.network.message.accounting.MessageRemoveSchematic;
import com.github.lunatrius.schematica.network.message.accounting.MessageSaveSchematic;
import com.github.lunatrius.schematica.network.message.download.*;
import commonnetwork.api.Network;

public class PacketHandler {
	public static void init() {
		Network.registerPacket(MessageCapabilities.TYPE, MessageCapabilities.class, MessageCapabilities.STREAM_CODEC,
				MessageCapabilities::handle);
		Network.registerPacket(MessageDownloadBegin.TYPE, MessageDownloadBegin.class,
				MessageDownloadBegin.STREAM_CODEC, MessageDownloadBegin::handle);
		Network.registerPacket(MessageDownloadBeginAck.TYPE, MessageDownloadBeginAck.class,
				MessageDownloadBeginAck.STREAM_CODEC, MessageDownloadBeginAck::handle);
		Network.registerPacket(MessageDownloadChunk.TYPE, MessageDownloadChunk.class,
				MessageDownloadChunk.STREAM_CODEC, MessageDownloadChunk::handle);
		Network.registerPacket(MessageDownloadChunkAck.TYPE, MessageDownloadChunkAck.class,
				MessageDownloadChunkAck.STREAM_CODEC, MessageDownloadChunkAck::handle);
		Network.registerPacket(MessageDownloadEnd.TYPE, MessageDownloadEnd.class, MessageDownloadEnd.STREAM_CODEC,
				MessageDownloadEnd::handle);
		Network.registerPacket(MessageSaveSchematic.TYPE, MessageSaveSchematic.class,
				MessageSaveSchematic.STREAM_CODEC,
				MessageSaveSchematic::handle);
		Network.registerPacket(MessageAddSchematic.TYPE, MessageAddSchematic.class, MessageAddSchematic.STREAM_CODEC,
				MessageAddSchematic::handle);
		Network.registerPacket(MessageRemoveSchematic.TYPE, MessageRemoveSchematic.class,
				MessageRemoveSchematic.STREAM_CODEC, MessageRemoveSchematic::handle);
	}
}