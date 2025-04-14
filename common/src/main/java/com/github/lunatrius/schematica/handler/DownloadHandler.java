package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.core.PlatformUtils;
import com.github.lunatrius.schematica.network.message.download.*;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.api.Dispatcher;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DownloadHandler {
	public static DownloadHandler INSTANCE;
	private final Map<Consumer<ISchematic>, Boolean> downloadCompleteListener = new HashMap<>();
	private final Map<UUID, SchematicTransfer> transferMap = new LinkedHashMap<>();
	public ISchematic schematic = null;

	private DownloadHandler() {
		TickEvent.SERVER_POST.register(this::processQueue);
	}

	private void processQueue(MinecraftServer server) {
		if (this.getTransferMap().isEmpty()) {
			return;
		}

		ServerPlayer player = server.getPlayerList().getPlayer(this.getTransferMap().keySet().iterator().next());

		if (player == null) {
			return;
		}

		SchematicTransfer transfer = this.getTransferMap().remove(player.getUUID());

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
		} else if (transfer.state == SchematicTransfer.State.REQUEST_WAIT) {
			sendRequest(transfer);
		} else if (transfer.state == SchematicTransfer.State.BEGIN_WAIT) {
			sendBegin(player, transfer);
		} else if (transfer.state == SchematicTransfer.State.CHUNK_WAIT) {
			sendChunk(player, transfer);
		} else if (transfer.state == SchematicTransfer.State.END_WAIT) {
			sendEnd(player, transfer);

			if (PlatformUtils.isPlatformServer()) {
				getTransferMap().remove(player.getUUID());
			}
		}

		this.getTransferMap().put(player.getUUID(), transfer);
	}

	public Map<UUID, SchematicTransfer> getTransferMap() {
		return transferMap;
	}

	private void sendChunk(ServerPlayer player, @NotNull SchematicTransfer transfer) {
		transfer.setState(SchematicTransfer.State.CHUNK);

		Reference.logger.trace("Sending chunk {},{},{}", transfer.baseX, transfer.baseY, transfer.baseZ);
		MessageDownloadChunk message = new MessageDownloadChunk(transfer.schematic, transfer.baseX, transfer.baseY,
				transfer.baseZ);
		Dispatcher.sendToClient(message, player);
	}

	private void sendRequest(@NotNull SchematicTransfer transfer) {
		transfer.setState(SchematicTransfer.State.REQUEST);
		MessageDownloadRequest message = new MessageDownloadRequest(transfer.schematic.getMetadata().id(),
				transfer.type);
		Dispatcher.sendToServer(message);
	}

	private void sendBegin(ServerPlayer player, @NotNull SchematicTransfer transfer) {
		transfer.setState(SchematicTransfer.State.BEGIN);

		MessageDownloadBegin message = new MessageDownloadBegin(transfer.schematic, transfer.type);
		Dispatcher.sendToClient(message, player);
	}

	private void sendEnd(ServerPlayer player, @NotNull SchematicTransfer transfer) {
		transfer.setState(SchematicTransfer.State.END);

		MessageDownloadEnd message = new MessageDownloadEnd(transfer.schematic.getMetadata().id());
		Dispatcher.sendToClient(message, player);
	}

	public static void init() {
		DownloadHandler.INSTANCE = new DownloadHandler();
	}

	public void registerDownloadCompleteListener(Consumer<ISchematic> listener, boolean removeWhenDone) {
		this.downloadCompleteListener.put(listener, removeWhenDone);
	}

	public void onDownloadComplete() {
		Dispatcher.sendToServer(new MessageDownloadEndAck(true));

		for (Consumer<ISchematic> listener : this.downloadCompleteListener.keySet()) {
			listener.accept(DownloadHandler.INSTANCE.schematic);
			if (this.downloadCompleteListener.get(listener)) {
				unregisterDownloadCompleteListener(listener);
			}
		}
	}

	public void unregisterDownloadCompleteListener(Consumer<ISchematic> listener) {
		this.downloadCompleteListener.remove(listener);
	}
}