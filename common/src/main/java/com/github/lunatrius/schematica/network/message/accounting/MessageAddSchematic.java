package com.github.lunatrius.schematica.network.message.accounting;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.networking.data.PacketContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public record MessageAddSchematic(SchematicMetadata metadata) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<MessageAddSchematic> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.ADD_SCHEMATIC));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageAddSchematic> STREAM_CODEC =
			StreamCodec.composite(
					SchematicMetadata.STREAM_CODEC, MessageAddSchematic::metadata,
					MessageAddSchematic::new);

	public static void handle(@NotNull PacketContext<MessageAddSchematic> ctx) {
		SchematicHolder holder = new SchematicHolder(SchematicMetadata);
		SchematicAccounter.syncedAddSchematic(ctx.message().metadata().id(), holder);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
