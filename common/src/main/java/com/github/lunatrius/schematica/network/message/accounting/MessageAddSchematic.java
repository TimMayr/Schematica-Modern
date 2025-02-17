package com.github.lunatrius.schematica.network.message.accounting;

import com.github.lunatrius.schematica.accounting.SchematicAccounter;
import com.github.lunatrius.schematica.accounting.SchematicHolder;
import com.github.lunatrius.schematica.accounting.SchematicLocation;
import com.github.lunatrius.schematica.core.Codecs;
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

import java.util.Set;
import java.util.UUID;

@MethodsReturnNonnullByDefault
public record MessageAddSchematic(@NotNull UUID schematicId, @NotNull String name, long fileSize,
                                  SchematicLocation locationType,
                                  UUID owner, Set<UUID> readPlayers,
                                  Set<UUID> removePlayers) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<MessageAddSchematic> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.ADD_SCHEMATIC));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageAddSchematic> STREAM_CODEC =
			StreamCodec.composite(
					Codecs.UUID, MessageAddSchematic::schematicId,
					ByteBufCodecs.STRING_UTF8, MessageAddSchematic::name,
					ByteBufCodecs.LONG, MessageAddSchematic::fileSize,
					Codecs.ENUM(SchematicLocation.class), MessageAddSchematic::locationType,
					Codecs.UUID, MessageAddSchematic::owner,
					Codecs.SET(Codecs.UUID), MessageAddSchematic::readPlayers,
					Codecs.SET(Codecs.UUID), MessageAddSchematic::removePlayers,
					MessageAddSchematic::new);

	public static void handle(@NotNull PacketContext<MessageAddSchematic> ctx) {
		SchematicHolder holder = new SchematicHolder(ctx.message().name(), ctx.message().fileSize(),
				ctx.message().locationType(), ctx.message().owner(), ctx.message().readPlayers(),
				ctx.message().removePlayers());

		SchematicAccounter.syncedAddSchematic(ctx.message().schematicId(), holder);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
