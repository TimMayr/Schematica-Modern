package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.core.Codecs;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.networking.data.PacketContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@MethodsReturnNonnullByDefault
public record MessageRemoveSchematic(@NotNull UUID id) implements CustomPacketPayload {

	public static final Type<MessageRemoveSchematic> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.REMOVE_SCHEMATIC));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageRemoveSchematic> STREAM_CODEC =
			StreamCodec.composite(
					Codecs.UUID, MessageRemoveSchematic::id,
					MessageRemoveSchematic::new);

	public static void handle(@NotNull PacketContext<MessageRemoveSchematic> ctx) {
		SchematicAccounter.syncedRemoveSchematic(ctx.message().id());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
