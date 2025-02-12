package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.network.message.MessageDownloadBegin;
import com.github.lunatrius.schematica.network.message.MessageDownloadChunk;
import com.github.lunatrius.schematica.network.message.MessageDownloadEnd;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.api.Dispatcher;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class DownloadHandler {
	public static final DownloadHandler INSTANCE = new DownloadHandler();
	public final Map<String, SchematicTransfer> transferMap = new LinkedHashMap<>();
	public ISchematic schematic = null;

	private DownloadHandler() {
		TickEvent.SERVER_POST.register(this::processQueue);
	}

	private void processQueue(MinecraftServer server) {
		if (this.transferMap.isEmpty()) {
			return;
		}

		ServerPlayer player = server.getPlayerList().getPlayerByName(this.transferMap.keySet().iterator().next());
		SchematicTransfer transfer = this.transferMap.remove(player.getScoreboardName());

		if (transfer == null) {
			return;
		}

		if (!transfer.state.isWaiting()) {
			transfer.timeout += 1;
			if (transfer.timeout >= Constants.Network.TIMEOUT) {
				transfer.retries += 1;
				if (transfer.retries >= Constants.Network.RETRIES) {
					Reference.logger.warn("{}'s download was dropped!", player.getScoreboardName());
					return;
				}

				Reference.logger.warn("{}'s download timed out, retrying (#{})", player.getScoreboardName(),
				                      transfer.retries);

				sendChunk(player, transfer);
				transfer.timeout = 0;
			}
		} else if (transfer.state == SchematicTransfer.State.BEGIN_WAIT) {
			sendBegin(player, transfer);
		} else if (transfer.state == SchematicTransfer.State.CHUNK_WAIT) {
			sendChunk(player, transfer);
		} else if (transfer.state == SchematicTransfer.State.END_WAIT) {
			sendEnd(player, transfer);
			return;
		}

		this.transferMap.put(player.getScoreboardName(), transfer);
	}

	private void sendBegin(ServerPlayer player, @NotNull SchematicTransfer transfer) {
		transfer.setState(SchematicTransfer.State.BEGIN);

		MessageDownloadBegin message = new MessageDownloadBegin(transfer.schematic);
		Dispatcher.sendToClient(message, player);
	}

	private void sendChunk(ServerPlayer player, @NotNull SchematicTransfer transfer) {
		transfer.setState(SchematicTransfer.State.CHUNK);

		Reference.logger.trace("Sending chunk {},{},{}", transfer.baseX, transfer.baseY, transfer.baseZ);
		MessageDownloadChunk message =
				new MessageDownloadChunk(transfer.schematic, transfer.baseX, transfer.baseY, transfer.baseZ);
		Dispatcher.sendToClient(message, player);
	}

	private void sendEnd(ServerPlayer player, @NotNull SchematicTransfer transfer) {
		MessageDownloadEnd message = new MessageDownloadEnd(transfer.name);
		Dispatcher.sendToClient(message, player);
	}
}