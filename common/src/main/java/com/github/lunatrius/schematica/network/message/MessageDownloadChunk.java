package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.network.transfer.SchematicTransfer;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public class MessageDownloadChunk implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MessageCapabilities> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, Names.Network.DOWNLOAD_CHUNK_LOCATION));

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageDownloadChunk> STREAM_CODEC = new StreamCodec<>() {
		@Contract("_ -> new")
		public @NotNull MessageDownloadChunk decode(@NotNull RegistryFriendlyByteBuf buf) {
			if (Platform.getEnv() == EnvType.CLIENT) {
				return SchematicTransfer.readFromBuff(buf);
			}

			throw new IllegalStateException("Can't be run on server");
		}

		public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull MessageDownloadChunk msg) {
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

		this.blocks = new BlockState[Constants.SchematicChunk.WIDTH]
				[Constants.SchematicChunk.HEIGHT]
				[Constants.SchematicChunk.LENGTH];

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
					this.blockEntities.add(blockEntity);
				}
			}
		}
	}

	public MessageDownloadChunk(int baseX, int baseY, int baseZ, BlockState[][][] blocks,
	                            List<BlockEntity> blockEntities, List<Entity> entities) {
		this.baseX = baseX;
		this.baseY = baseY;
		this.baseZ = baseZ;
		this.blocks = blocks;
		this.blockEntities = blockEntities;
		this.entities = entities;
	}

	public static void handle(@NotNull PacketContext<MessageDownloadChunk> ctx) {
		if (ctx.side() == Side.CLIENT) {
			ctx.message().copyToSchematic(DownloadHandler.INSTANCE.schematic);
			Dispatcher.sendToServer(
					new MessageDownloadChunkAck(true, ctx.message().getBaseX(), ctx.message().getBaseY(),
							ctx.message().getBaseZ()));
		}
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
		return TYPE;
	}
}