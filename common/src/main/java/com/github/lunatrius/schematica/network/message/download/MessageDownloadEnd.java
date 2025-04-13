package com.github.lunatrius.schematica.network.message.download;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.core.PlayerUtils;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.schematic.format.SchematicFormat;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@MethodsReturnNonnullByDefault
public record MessageDownloadEnd(UUID id) implements CustomPacketPayload {
	public static final Type<MessageDownloadEnd> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_END_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadEnd> STREAM_CODEC =
			StreamCodec.composite(
					CommonCodecs.UUID, MessageDownloadEnd::id,
					MessageDownloadEnd::new);

	public static void handle(@NotNull PacketContext<MessageDownloadEnd> ctx) {
		if (ctx.side() == Side.CLIENT) {
			boolean success = false;
			Path path = null;
			Player player = PlayerUtils.getClientPlayer();
			SchematicTransfer transfer = DownloadHandler.INSTANCE.getTransferMap().get(player.getUUID());

			switch (transfer.type) {
				case LOAD -> {
					ClientProxy.schematic = FakeLevel.of(DownloadHandler.INSTANCE.schematic);
					DownloadHandler.INSTANCE.schematic = null;
				}
				case SAVE_TEMP -> {
					ClientProxy.tempSchematic = DownloadHandler.INSTANCE.schematic;
					DownloadHandler.INSTANCE.schematic = null;
				}
				case SAVE -> {
					try {
						String name = DownloadHandler.INSTANCE.schematic.getName();
						DownloadHandler.INSTANCE.schematic.setMetadata(DownloadHandler.INSTANCE.schematic.getMetadata()
								.withId(UUID.randomUUID()).withOwner(PlayerUtils.getClientPlayer().getUUID())
								.isPrivate(true).withName(name.split("\\.")[0] + " (Local)." + name.split("\\.")[1]));
						path = Reference.proxy.getSchematicDirectory();
						path = path.resolve(name);

						if (!Files.exists(path)) {
							Files.createDirectories(path.getParent());
						}

						success = SchematicFormat.writeToFile(DownloadHandler.INSTANCE.schematic);
						DownloadHandler.INSTANCE.schematic = null;
					} catch (IOException e) {
						Reference.logger.error("Unable to save schematic {} to directory [{}]",
								SchematicAccounter.get(ctx.message().id()).metadata().name(),
								path.toAbsolutePath().normalize().toString());
					} catch (NullPointerException e) {
						Reference.logger.error("Unable to save schematic to invalid directory");
					} finally {
						if (success) {
							Minecraft.getInstance().player.displayClientMessage(
									Component.translatable(Names.Command.Download.Message.DOWNLOAD_SUCCEEDED,
											SchematicAccounter.get(ctx.message().id()).metadata().name()), false);
						} else {
							Minecraft.getInstance().player.displayClientMessage(
									Component.translatable(Names.Command.Download.Message.DOWNLOAD_FAILED,
											SchematicAccounter.get(ctx.message().id()).metadata().name()), false);
						}

						DownloadHandler.INSTANCE.schematic = null;
					}
				}
			}

			DownloadHandler.INSTANCE.onDownloadComplete();
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}