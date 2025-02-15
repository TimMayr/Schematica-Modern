package com.github.lunatrius.schematica.network.message;

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
public record MessageSave(String schematicName, String format, boolean isPrivate, BlockPos start, BlockPos end,
                          String iconName)
		implements CustomPacketPayload {
	public static final Type<MessageSave> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.SAVE_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageSave> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, MessageSave::schematicName,
					ByteBufCodecs.STRING_UTF8, MessageSave::format,
					ByteBufCodecs.BOOL, MessageSave::isPrivate,
					BlockPos.STREAM_CODEC, MessageSave::start,
					BlockPos.STREAM_CODEC, MessageSave::end,
					ByteBufCodecs.STRING_UTF8, MessageSave::iconName,
					MessageSave::new);

	public static void handle(@NotNull PacketContext<MessageSave> ctx) {
		if (ctx.side() == Side.SERVER) {
			ServerProxy.saveServerSchematic(ctx.sender(), ctx.message().schematicName(),
					ctx.sender().getCommandSenderWorld(), ctx.message().format(), ctx.message().start(),
					ctx.message().end(), ctx.message().isPrivate(), ctx.message().iconName());
		}
	}

	@Override
	public Type<MessageSave> type() {
		return TYPE;
	}
}