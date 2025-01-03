package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import dev.architectury.networking.NetworkManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
public record MessageDownloadBegin(ItemStack icon, short width, short height, short length)
		implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<MessageDownloadBegin> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_BEGIN_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadBegin> STREAM_CODEC =
			StreamCodec.composite(ItemStack.STREAM_CODEC, MessageDownloadBegin::icon, ByteBufCodecs.SHORT,
			                      MessageDownloadBegin::width, ByteBufCodecs.SHORT, MessageDownloadBegin::height,
			                      ByteBufCodecs.SHORT, MessageDownloadBegin::length, MessageDownloadBegin::new);

	public MessageDownloadBegin(ISchematic schematic) {
		this(schematic.getIcon(), schematic.getWidth(), schematic.getHeight(), schematic.getLength());
	}

	public static void handle(MessageDownloadBegin msg, NetworkManager.PacketContext ctx) {
		ctx.queue(() -> {
			DownloadHandler.INSTANCE.schematic = new Schematic(msg.icon, msg.width, msg.height, msg.length);
			PacketHandler.INSTANCE.sendToServer(new MessageDownloadBeginAck());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return null;
	}
}