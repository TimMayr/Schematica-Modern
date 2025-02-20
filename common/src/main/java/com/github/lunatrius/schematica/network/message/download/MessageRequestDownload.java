package com.github.lunatrius.schematica.network.message.download;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.core.Codecs;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@MethodsReturnNonnullByDefault
public record MessageRequestDownload(UUID id)
		implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<MessageRequestDownload> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_REQUEST_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageRequestDownload> STREAM_CODEC =
			StreamCodec.composite(
					Codecs.UUID, MessageRequestDownload::id,
					MessageRequestDownload::new);


	public static void handle(@NotNull PacketContext<MessageRequestDownload> ctx) {
		if (ctx.side() == Side.SERVER) {
			Player player = ctx.sender();
			SchematicHolder holder = SchematicAccounter.get(ctx.message().id());
			holder.getSchematic().thenAccept((iSchematic) ->
					DownloadHandler.INSTANCE.transferMap.put(player.getScoreboardName(),
							new SchematicTransfer(iSchematic, ctx.message().id().toString())));
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
