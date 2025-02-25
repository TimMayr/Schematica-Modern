package com.github.lunatrius.schematica.network.message.accounting;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.core.CommonCodecs;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.networking.data.PacketContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@MethodsReturnNonnullByDefault
public record MessageRemoveSchematic(@NotNull UUID id, boolean synced) implements CustomPacketPayload {
	public static final Type<MessageRemoveSchematic> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.REMOVE_SCHEMATIC));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageRemoveSchematic> STREAM_CODEC =
			StreamCodec.composite(
					CommonCodecs.UUID, MessageRemoveSchematic::id,
					ByteBufCodecs.BOOL, MessageRemoveSchematic::synced,
					MessageRemoveSchematic::new);

	public static void handle(@NotNull PacketContext<MessageRemoveSchematic> ctx) {
		SchematicAccounter.removeSchematic(ctx.message().id(), true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
