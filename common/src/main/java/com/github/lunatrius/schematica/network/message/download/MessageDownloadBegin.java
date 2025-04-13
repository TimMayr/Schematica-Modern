package com.github.lunatrius.schematica.network.message.download;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.core.PlayerUtils;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public record MessageDownloadBegin(SchematicMetadata metadata, DownloadType downloadType)
		implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<MessageDownloadBegin> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_BEGIN_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadBegin> STREAM_CODEC =
			StreamCodec.composite(
					SchematicMetadata.STREAM_CODEC, MessageDownloadBegin::metadata,
					CommonCodecs.ENUM(DownloadType.class), MessageDownloadBegin::downloadType,
					MessageDownloadBegin::new);

	public MessageDownloadBegin(@NotNull ISchematic schematic, DownloadType downloadType) {
		this(schematic.getMetadata(), downloadType);
	}

	public static void handle(@NotNull PacketContext<MessageDownloadBegin> ctx) {
		if (ctx.side() == Side.CLIENT) {
			DownloadHandler.INSTANCE.schematic = new Schematic(ctx.message().metadata());
			DownloadHandler.INSTANCE.getTransferMap().put(PlayerUtils.getClientPlayer().getUUID(),
					new SchematicTransfer(DownloadHandler.INSTANCE.schematic, ctx.message().downloadType()));
			Dispatcher.sendToServer(new MessageDownloadBeginAck(true));
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}