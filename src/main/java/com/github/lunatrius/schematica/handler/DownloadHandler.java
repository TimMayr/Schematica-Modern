package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.network.message.MessageDownloadBegin;
import com.github.lunatrius.schematica.network.message.MessageDownloadChunk;
import com.github.lunatrius.schematica.network.message.MessageDownloadEnd;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.Map;

public class DownloadHandler {
	public static final DownloadHandler INSTANCE = new DownloadHandler();
	public final Map<ServerPlayerEntity, SchematicTransfer> transferMap = new LinkedHashMap<>();
	public ISchematic schematic = null;

	private DownloadHandler() {}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}

		processQueue();
	}

	private void processQueue() {
		if (this.transferMap.isEmpty()) {
			return;
		}

		ServerPlayerEntity player = this.transferMap.keySet().iterator().next();
		SchematicTransfer transfer = this.transferMap.remove(player);

		if (transfer == null) {
			return;
		}

		if (!transfer.state.isWaiting()) {
			if (++transfer.timeout >= Constants.Network.TIMEOUT) {
				if (++transfer.retries >= Constants.Network.RETRIES) {
					Reference.logger.warn("{}'s download was dropped!", player.getName());
					return;
				}

				Reference.logger.warn("{}'s download timed out, retrying (#{})", player.getName(), transfer.retries);

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

		this.transferMap.put(player, transfer);
	}

	private void sendBegin(ServerPlayerEntity player, SchematicTransfer transfer) {
		transfer.setState(SchematicTransfer.State.BEGIN);

		MessageDownloadBegin message = new MessageDownloadBegin(transfer.schematic);
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
	}

	private void sendChunk(ServerPlayerEntity player, SchematicTransfer transfer) {
		transfer.setState(SchematicTransfer.State.CHUNK);

		Reference.logger.trace("Sending chunk {},{},{}", transfer.baseX, transfer.baseY, transfer.baseZ);
		MessageDownloadChunk message =
				new MessageDownloadChunk(transfer.schematic, transfer.baseX, transfer.baseY, transfer.baseZ);
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
	}

	private void sendEnd(ServerPlayerEntity player, SchematicTransfer transfer) {
		MessageDownloadEnd message = new MessageDownloadEnd(transfer.name);
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
	}
}
