package com.github.lunatrius.schematica.network.message;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.handler.DownloadHandler;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.network.PacketHandler;
import com.github.lunatrius.schematica.reference.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageDownloadChunk {
	public int baseX;
	public int baseY;
	public int baseZ;

	public BlockState[][][] blocks;
	public List<TileEntity> tileEntities;
	public List<Entity> entities;

	public MessageDownloadChunk(final ISchematic schematic, final int baseX, final int baseY, final int baseZ) {
		this.baseX = baseX;
		this.baseY = baseY;
		this.baseZ = baseZ;

		this.blocks =
				new BlockState[Constants.SchematicChunk.WIDTH][Constants.SchematicChunk.HEIGHT][Constants.SchematicChunk.LENGTH];

		this.tileEntities = new ArrayList<>();
		this.entities = new ArrayList<>();

		final MBlockPos pos = new MBlockPos();
		for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
			for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
				for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
					pos.set(baseX + x, baseY + y, baseZ + z);
					final BlockState blockState = schematic.getBlockState(pos);
					this.blocks[x][y][z] = blockState;
					final TileEntity tileEntity = schematic.getTileEntity(pos);
					if (tileEntity != null) {
						this.tileEntities.add(tileEntity);
					}
				}
			}
		}
	}

	private MessageDownloadChunk(int baseX, int baseY, int baseZ, BlockState[][][] blocks,
	                             List<TileEntity> tileEntities, List<Entity> entities) {
		this.baseX = baseX;
		this.baseY = baseY;
		this.baseZ = baseZ;
		this.blocks = blocks;
		this.tileEntities = tileEntities;
		this.entities = entities;
	}

	public static MessageDownloadChunk decode(PacketBuffer buf) {
		int msgBaseX = buf.readInt();
		int msgBaseY = buf.readInt();
		int msgBaseZ = buf.readInt();

		BlockState[][][] msgBlocks =
				new BlockState[Constants.SchematicChunk.WIDTH][Constants.SchematicChunk.HEIGHT][Constants.SchematicChunk.LENGTH];
		List<TileEntity> msgTileEntities = new ArrayList<>();
		List<Entity> msgEntities = new ArrayList<>();

		for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
			for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
				for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
					msgBlocks[x][y][z] = Block.getStateById(buf.readVarInt());
				}
			}
		}

		final CompoundNBT compound = buf.readCompoundTag();
		if (compound != null) {
			NBTHelper.readTileEntitiesFromCompound(compound, msgTileEntities);
		}

		final CompoundNBT compound2 = buf.readCompoundTag();
		NBTHelper.readEntitiesFromCompound(compound2, msgEntities);

		return new MessageDownloadChunk(msgBaseX, msgBaseY, msgBaseZ, msgBlocks, msgTileEntities, msgEntities);
	}

	public static void encode(MessageDownloadChunk msg, PacketBuffer buf) {
		buf.writeInt(msg.baseX);
		buf.writeInt(msg.baseY);
		buf.writeInt(msg.baseZ);

		for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
			for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
				for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
					buf.writeVarInt(Block.getStateId(msg.blocks[x][y][z]));
				}
			}
		}

		final CompoundNBT compound = NBTHelper.writeTileEntitiesToCompound(msg.tileEntities);
		buf.writeCompoundTag(compound);

		final CompoundNBT compound1 = NBTHelper.writeEntitiesToCompound(msg.entities);
		buf.writeCompoundTag(compound1);
	}

	public static void handle(MessageDownloadChunk msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			msg.copyToSchematic(DownloadHandler.INSTANCE.schematic);

			PacketHandler.INSTANCE.sendToServer(new MessageDownloadChunkAck());
		});
		ctx.get().setPacketHandled(true);
	}


	private void copyToSchematic(final ISchematic schematic) {
		final MBlockPos pos = new MBlockPos();
		for (int x = 0; x < Constants.SchematicChunk.WIDTH; x++) {
			for (int y = 0; y < Constants.SchematicChunk.HEIGHT; y++) {
				for (int z = 0; z < Constants.SchematicChunk.LENGTH; z++) {
					final BlockState blockState = this.blocks[x][y][z];
					pos.set(this.baseX + x, this.baseY + y, this.baseZ + z);

					schematic.setBlockState(pos, blockState);
				}
			}
		}

		for (final TileEntity tileEntity : this.tileEntities) {
			schematic.setTileEntity(tileEntity.getPos(), tileEntity);
		}
	}
}
