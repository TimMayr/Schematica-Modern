package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
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
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public record MessageCapabilities(boolean isPrinterEnabled, boolean isSaveEnabled, boolean isLoadEnabled)
		implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MessageCapabilities> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.CAPABILITIES_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageCapabilities> STREAM_CODEC =
			StreamCodec.composite(ByteBufCodecs.BOOL, MessageCapabilities::isPrinterEnabled, ByteBufCodecs.BOOL,
			                      MessageCapabilities::isSaveEnabled, ByteBufCodecs.BOOL,
			                      MessageCapabilities::isLoadEnabled, MessageCapabilities::new);

	public static void handle(@NotNull PacketContext<MessageCapabilities> ctx) {
		if (ctx.side() == Side.CLIENT) {
			SchematicPrinter.INSTANCE.setEnabled(ctx.message().isPrinterEnabled());
			Reference.proxy.isSaveEnabled = ctx.message().isSaveEnabled();
			Reference.proxy.isLoadEnabled = ctx.message().isLoadEnabled();

			Reference.logger.info("Server capabilities{printer={}, save={}, load={}}",
			                      ctx.message().isPrinterEnabled(),
			                      ctx.message().isSaveEnabled(), ctx.message().isLoadEnabled());
		}
	}

	@Override
	public Type<MessageCapabilities> type() {
		return TYPE;
	}
}