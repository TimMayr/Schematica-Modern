package com.github.lunatrius.schematica.network.message.commands;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@MethodsReturnNonnullByDefault
public record MessageDeleteSchematic(UUID id)
		implements CustomPacketPayload {

	public static final Type<MessageDeleteSchematic> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DELETE_SCHEMATIC));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDeleteSchematic> STREAM_CODEC =
			StreamCodec.composite(
					CommonCodecs.UUID, MessageDeleteSchematic::id,
					MessageDeleteSchematic::new);

	public static void handle(@NotNull PacketContext<MessageDeleteSchematic> ctx) {
		if (ctx.side() == Side.CLIENT) {
			SchematicMetadata meta = SchematicAccounter.get(ctx.message().id()).metadata();
			Path file = Reference.proxy.resolveSchematic(meta);
			UUID id = SchematicAccounter.getIdForFile(file);
			try {
				Files.delete(file);
				SchematicAccounter.removeSchematic(id, false);
				Reference.proxy.sendMessage(null,
						Component.translatable(Names.Command.Remove.Message.SCHEMATIC_REMOVED, meta.name()));
			} catch (IOException e) {
				Reference.proxy.sendMessage(null,
						Component.translatable(Names.Command.Remove.Message.SCHEMATIC_NOT_FOUND, meta.name()));
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}