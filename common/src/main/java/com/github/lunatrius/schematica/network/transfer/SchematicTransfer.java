package com.github.lunatrius.schematica.network.transfer;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.network.message.download.MessageDownloadChunk;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SchematicTransfer {
	public final ISchematic schematic;
	public final String name;
	public final int width;
	public final int height;
	public final int length;
	public State state = State.BEGIN_WAIT;
	public int timeout = 0;
	public int retries = 0;
	public int baseX = 0;
	public int baseY = 0;
	public int baseZ = 0;

	public SchematicTransfer(@NotNull ISchematic schematic, String name) {
		this.schematic = schematic;
		this.name = name;

		this.width = schematic.getSizeX();
		this.height = schematic.getHeight();
		this.length = schematic.getSizeZ();
	}

	@Contract("_ -> new")
	public static @NotNull MessageDownloadChunk readFromBuff(@NotNull RegistryFriendlyByteBuf buf) {
		int msgBaseX = buf.readInt();
		int msgBaseY = buf.readInt();
		int msgBaseZ = buf.readInt();

		BlockState[][][] msgBlocks =
				new BlockState[Constants.SchematicChunk.WIDTH]
						[Constants.SchematicChunk.HEIGHT]
						[Constants.SchematicChunk.LENGTH];
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
			NBTHelper.readBlockEntitiesFromCompound(blockEntitiesTag, Reference.proxy.getLevel(),
					msgBlockEntities);
		}

		CompoundTag entitiesTag = buf.readNbt();
		NBTHelper.readEntitiesFromCompound(entitiesTag, msgEntities, Reference.proxy.getLevel());

		return new MessageDownloadChunk(msgBaseX, msgBaseY, msgBaseZ, msgBlocks, msgBlockEntities,
				msgEntities);
	}

	public void confirmChunk(int chunkX, int chunkY, int chunkZ) {
		if (chunkX == this.baseX && chunkY == this.baseY && chunkZ == this.baseZ) {
			setState(State.CHUNK_WAIT);
			this.baseX += Constants.SchematicChunk.WIDTH;

			if (this.baseX >= this.width) {
				this.baseX = 0;
				this.baseY += Constants.SchematicChunk.HEIGHT;

				if (this.baseY >= this.height) {
					this.baseY = 0;
					this.baseZ += Constants.SchematicChunk.LENGTH;

					if (this.baseZ >= this.length) {
						setState(State.END_WAIT);
					}
				}
			}
		}
	}

	public void setState(State state) {
		this.state = state;
		this.timeout = 0;
		this.retries = 0;
	}

	public enum State {
		BEGIN_WAIT(true),
		BEGIN,
		CHUNK_WAIT(true),
		CHUNK,
		END_WAIT(true),
		END;
		private boolean waiting;

		State() {}

		State(boolean waiting) {
			this.waiting = waiting;
		}

		public boolean isWaiting() {
			return this.waiting;
		}
	}
}