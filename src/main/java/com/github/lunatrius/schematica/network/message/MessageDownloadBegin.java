package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDownloadBegin {
	public final ItemStack icon;
	public final int width;
	public final int height;
	public final int length;

	public MessageDownloadBegin(ISchematic schematic) {
		this.icon = schematic.getIcon();
		this.width = schematic.getWidth();
		this.height = schematic.getHeight();
		this.length = schematic.getLength();
	}

	public static MessageDownloadBegin decode(PacketBuffer buf) {
		return new MessageDownloadBegin(
				new Schematic(buf.readItemStack(), buf.readInt(), buf.readInt(), buf.readInt()));
	}

	public static void encode(MessageDownloadBegin msg, PacketBuffer buf) {
		buf.writeItemStack(msg.icon);
		buf.writeInt(msg.width);
		buf.writeInt(msg.height);
		buf.writeInt(msg.length);
	}

	public static void handle(MessageDownloadBegin msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			DownloadHandler.INSTANCE.schematic = new Schematic(msg.icon, msg.width, msg.height, msg.length);
			PacketHandler.INSTANCE.sendToServer(new MessageDownloadBeginAck());
		});
		ctx.get().setPacketHandled(true);
	}
}