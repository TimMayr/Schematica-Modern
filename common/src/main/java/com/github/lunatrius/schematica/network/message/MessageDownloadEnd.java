package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@MethodsReturnNonnullByDefault
public record MessageDownloadEnd(String name) implements CustomPacketPayload {
	public static final Type<MessageDownloadEnd> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_END_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadEnd> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, MessageDownloadEnd::name,
					MessageDownloadEnd::new);

	public static void handle(@NotNull PacketContext<MessageDownloadEnd> ctx) {
		if (ctx.side() == Side.CLIENT) {
			File directory = Reference.proxy.getPlayerSchematicDirectory(null, true);
			boolean success =
					SchematicFormat.writeToFile(directory, ctx.message().name(), DownloadHandler.INSTANCE.schematic);

			if (success) {
				if (Minecraft.getInstance().player != null) {
					Minecraft.getInstance().player.displayClientMessage(
							Component.translatable(Names.Command.Download.Message.DOWNLOAD_SUCCEEDED,
									ctx.message().name()), false);
				}
			}

			DownloadHandler.INSTANCE.schematic = null;
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}