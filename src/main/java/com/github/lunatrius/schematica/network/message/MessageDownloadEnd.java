package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.File;
import java.util.function.Supplier;

public class MessageDownloadEnd {
	public final String name;

	public MessageDownloadEnd(String name) {
		this.name = name;
	}

	public static MessageDownloadEnd decode(PacketBuffer buf) {
		return new MessageDownloadEnd(buf.readString());
	}

	public static void encode(MessageDownloadEnd msg, PacketBuffer buf) {
		buf.writeString(msg.name);
	}

	public static void handle(MessageDownloadEnd msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			File directory = Reference.proxy.getPlayerSchematicDirectory(null, true);
			boolean success =
					SchematicFormat.writeToFile(directory, msg.name, null, DownloadHandler.INSTANCE.schematic);

			if (success) {
				if (Minecraft.getInstance().player != null) {
					Minecraft.getInstance().player.sendMessage(
							new TranslationTextComponent(Names.Command.Download.Message.DOWNLOAD_SUCCEEDED, msg.name));
				}
			}

			DownloadHandler.INSTANCE.schematic = null;
		});
		ctx.get().setPacketHandled(true);
	}
}