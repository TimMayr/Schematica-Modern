package com.github.lunatrius.schematica.network.message.commands;

import com.github.lunatrius.schematica.proxy.ServerProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public record MessageSaveSchematic(String schematicName, String format, boolean isPrivate, BlockPos start, BlockPos end,
                                   String iconName)
		implements CustomPacketPayload {
	public static final Type<MessageSaveSchematic> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.SAVE_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageSaveSchematic> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, MessageSaveSchematic::schematicName,
					ByteBufCodecs.STRING_UTF8, MessageSaveSchematic::format,
					ByteBufCodecs.BOOL, MessageSaveSchematic::isPrivate,
					BlockPos.STREAM_CODEC, MessageSaveSchematic::start,
					BlockPos.STREAM_CODEC, MessageSaveSchematic::end,
					ByteBufCodecs.STRING_UTF8, MessageSaveSchematic::iconName,
					MessageSaveSchematic::new);

	public static void handle(@NotNull PacketContext<MessageSaveSchematic> ctx) {
		if (ctx.side() == Side.SERVER) {
			ServerProxy.saveServerSchematic(ctx.sender(), ctx.message().schematicName(),
					ctx.sender().getCommandSenderWorld(), ctx.message().format(), ctx.message().start(),
					ctx.message().end(), ctx.message().isPrivate(), ctx.message().iconName());
		}
	}

	@Override
	public Type<MessageSaveSchematic> type() {
		return TYPE;
	}
}