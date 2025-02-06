package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
public record MessageDownloadBegin(ItemStack icon, int width, int height, int length)
		implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<MessageDownloadBegin> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_BEGIN_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadBegin> STREAM_CODEC =
			StreamCodec.composite(ItemStack.STREAM_CODEC, MessageDownloadBegin::icon, ByteBufCodecs.INT,
			                      MessageDownloadBegin::width, ByteBufCodecs.INT, MessageDownloadBegin::height,
			                      ByteBufCodecs.INT, MessageDownloadBegin::length, MessageDownloadBegin::new);

	public MessageDownloadBegin(ISchematic schematic) {
		this(schematic.getIcon(), schematic.getSizeX(), schematic.getHeight(), schematic.getSizeZ());
	}

	public static void handle(PacketContext<MessageDownloadBegin> ctx) {
		if (ctx.side() == Side.CLIENT) {
			DownloadHandler.INSTANCE.schematic =
					new Schematic(ctx.message().icon, ctx.message().width, ctx.message().height, ctx.message().length);
			Dispatcher.sendToServer(new MessageDownloadBeginAck(true));
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return null;
	}
}