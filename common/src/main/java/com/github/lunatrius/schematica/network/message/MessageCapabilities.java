package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import dev.architectury.networking.NetworkManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record MessageCapabilities(boolean isPrinterEnabled, boolean isSaveEnabled, boolean isLoadEnabled)
		implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MessageCapabilities> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.CAPABILITIES_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageCapabilities> STREAM_CODEC =
			StreamCodec.composite(ByteBufCodecs.BOOL, MessageCapabilities::isPrinterEnabled, ByteBufCodecs.BOOL,
			                      MessageCapabilities::isSaveEnabled, ByteBufCodecs.BOOL,
			                      MessageCapabilities::isLoadEnabled, MessageCapabilities::new);

	public static void handle(MessageCapabilities msg, NetworkManager.PacketContext ctx) {
		ctx.queue(() -> {
			SchematicPrinter.INSTANCE.setEnabled(msg.isPrinterEnabled());
			Reference.proxy.isSaveEnabled = msg.isSaveEnabled();
			Reference.proxy.isLoadEnabled = msg.isLoadEnabled();

			Reference.logger.info("Server capabilities{printer={}, save={}, load={}}", msg.isPrinterEnabled(),
			                      msg.isSaveEnabled(), msg.isLoadEnabled());
		});
	}

	@Override
	public Type<MessageCapabilities> type() {
		return TYPE;
	}
}