package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.reference.Reference;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageCapabilities {
	public final boolean isPrinterEnabled;
	public final boolean isSaveEnabled;
	public final boolean isLoadEnabled;

	public MessageCapabilities(boolean isPrinterEnabled, boolean isSaveEnabled, boolean isLoadEnabled) {
		this.isPrinterEnabled = isPrinterEnabled;
		this.isSaveEnabled = isSaveEnabled;
		this.isLoadEnabled = isLoadEnabled;
	}

	public static MessageCapabilities decode(PacketBuffer buf) {
		return new MessageCapabilities(buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
	}

	public static void encode(MessageCapabilities msg, ByteBuf buf) {
		buf.writeBoolean(msg.isPrinterEnabled);
		buf.writeBoolean(msg.isSaveEnabled);
		buf.writeBoolean(msg.isLoadEnabled);
	}

	public static void handle(MessageCapabilities msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			SchematicPrinter.INSTANCE.setEnabled(msg.isPrinterEnabled);
			Reference.proxy.isSaveEnabled = msg.isSaveEnabled;
			Reference.proxy.isLoadEnabled = msg.isLoadEnabled;

			Reference.logger.info("Server capabilities{printer={}, save={}, load={}}", msg.isPrinterEnabled,
			                      msg.isSaveEnabled, msg.isLoadEnabled);
		});
		ctx.get().setPacketHandled(true);
	}
}
