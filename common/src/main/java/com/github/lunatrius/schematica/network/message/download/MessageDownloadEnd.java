package com.github.lunatrius.schematica.network.message.download;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
			boolean success = false;
			Path path = null;

			try {
				path = Reference.proxy.getPlayerSchematicDirectory(null, true);
				path = path.resolve("downloaded").resolve(ctx.message().name());

				if (!Files.exists(path)) {
					Files.createDirectories(path.getParent());
				}

				success = SchematicFormat.writeToFile(path, null, DownloadHandler.INSTANCE.schematic);
				DownloadHandler.INSTANCE.schematic = null;
			} catch (IOException e) {
				Reference.logger.error("Unable to save schematic {} to directory [{}]", ctx.message().name(),
						path.toAbsolutePath().normalize().toString());
			} catch (NullPointerException e) {
				Reference.logger.error("Unable to save schematic to invalid directory");
			} finally {
				if (success) {
					Minecraft.getInstance().player.displayClientMessage(
							Component.translatable(Names.Command.Download.Message.DOWNLOAD_SUCCEEDED,
									ctx.message().name()), false);
				} else {
					Minecraft.getInstance().player.displayClientMessage(
							Component.translatable(Names.Command.Download.Message.DOWNLOAD_FAILED,
									ctx.message().name()), false);
				}

				DownloadHandler.INSTANCE.schematic = null;
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}