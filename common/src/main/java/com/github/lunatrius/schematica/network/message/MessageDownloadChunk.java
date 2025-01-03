package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import dev.architectury.networking.NetworkManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public class MessageDownloadChunk implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MessageCapabilities> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_CHUNK_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadChunk> CODEC = new StreamCodec<>() {
		public @NotNull MessageDownloadChunk decode(RegistryFriendlyByteBuf buf) {
			int msgBaseX = buf.readInt();
			int msgBaseY = buf.readInt();
			int msgBaseZ = buf.readInt();

			BlockState[][][] msgBlocks =
					new BlockState[Constants.SchematicChunk.WIDTH][Constants.SchematicChunk.HEIGHT][Constants.SchematicChunk.LENGTH];
			List<BlockEntity> msgBlockEntities = new ArrayList<>();
			List<Entity> msgEntities = new ArrayList<>();

			for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
				for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
					for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
						msgBlocks[x][y][z] = Block.stateById(buf.readVarInt());
					}
				}
			}

			CompoundTag blockEntitiesTag = buf.readNbt();
			if (blockEntitiesTag != null) {
				NBTHelper.readBlockEntitiesFromCompound(blockEntitiesTag, Minecraft.getInstance().level,
				                                        msgBlockEntities);
			}

			CompoundTag entitiesTag = buf.readNbt();
			NBTHelper.readEntitiesFromCompound(entitiesTag, msgEntities);

			return new MessageDownloadChunk(msgBaseX, msgBaseY, msgBaseZ, msgBlocks, msgBlockEntities, msgEntities);

		}

		public void encode(RegistryFriendlyByteBuf buf, MessageDownloadChunk msg) {
			buf.writeInt(msg.getBaseX());
			buf.writeInt(msg.getBaseY());
			buf.writeInt(msg.getBaseZ());

			for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
				for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
					for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
						buf.writeVarInt(Block.getId(msg.blocks[x][y][z]));
					}
				}
			}

			CompoundTag blockEntitiesCompound = NBTHelper.writeBlockEntitiesToCompound(msg.blockEntities);
			buf.writeNbt(blockEntitiesCompound);

			CompoundTag entitiesCompound = NBTHelper.writeEntitiesToCompound(msg.entities);
			buf.writeNbt(entitiesCompound);
		}
	};

	public final BlockState[][][] blocks;
	public final List<BlockEntity> blockEntities;
	public final List<Entity> entities;
	private final int baseX;
	private final int baseY;
	private final int baseZ;

	public MessageDownloadChunk(ISchematic schematic, int baseX, int baseY, int baseZ) {
		this.baseX = baseX;
		this.baseY = baseY;
		this.baseZ = baseZ;

		this.blocks =
				new BlockState[Constants.SchematicChunk.WIDTH][Constants.SchematicChunk.HEIGHT][Constants.SchematicChunk.LENGTH];

		this.blockEntities = new ArrayList<>();
		this.entities = new ArrayList<>();

		MBlockPos pos = new MBlockPos();
		for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
			for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
				for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
					pos.set(baseX + x, baseY + y, baseZ + z);
					BlockState blockState = schematic.getBlockState(pos);
					this.blocks[x][y][z] = blockState;
					BlockEntity blockEntity = schematic.getBlockEntity(pos);
					if (blockEntity != null) {
						this.blockEntities.add(blockEntity);
					}
				}
			}
		}
	}

	private MessageDownloadChunk(int baseX, int baseY, int baseZ, BlockState[][][] blocks,
	                             List<BlockEntity> blockEntities, List<Entity> entities) {
		this.baseX = baseX;
		this.baseY = baseY;
		this.baseZ = baseZ;
		this.blocks = blocks;
		this.blockEntities = blockEntities;
		this.entities = entities;
	}

	public static void handle(MessageDownloadChunk msg, NetworkManager.PacketContext ctx) {
		ctx.queue(() -> {
			msg.copyToSchematic(DownloadHandler.INSTANCE.schematic);
			PacketHandler.INSTANCE.sendToServer(
					new MessageDownloadChunkAck(msg.getBaseX(), msg.getBaseY(), msg.getBaseZ()));
		});
	}

	private void copyToSchematic(ISchematic schematic) {
		MBlockPos pos = new MBlockPos();
		for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
			for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
				for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
					BlockState blockState = this.blocks[x][y][z];
					pos.set(this.getBaseX() + x, this.getBaseY() + y, this.getBaseZ() + z);

					schematic.setBlockState(pos, blockState);
				}
			}
		}

		for (BlockEntity blockEntity : this.blockEntities) {
			schematic.setBlockEntity(blockEntity.getBlockPos(), blockEntity);
		}
	}

	public int getBaseX() {
		return baseX;
	}

	public int getBaseY() {
		return baseY;
	}

	public int getBaseZ() {
		return baseZ;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return null;
	}
}