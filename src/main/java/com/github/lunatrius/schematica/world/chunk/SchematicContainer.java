package com.github.lunatrius.schematica.world.chunk;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.io.File;

public class SchematicContainer {
	public final ISchematic schematic;
	public final PlayerEntity player;
	public final World world;
	public final File file;

	@Nullable
	public final String format;

	public final int minX;
	public final int maxX;
	public final int minY;
	public final int maxY;
	public final int minZ;
	public final int maxZ;

	public final int minChunkX;
	public final int maxChunkX;
	public final int minChunkZ;
	public final int maxChunkZ;
	public final int chunkCount;
	public int curChunkX;
	public int curChunkZ;
	public int processedChunks;

	public SchematicContainer(ISchematic schematic, PlayerEntity player, World world, File file,
	                          @Nullable String format, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
		this.schematic = schematic;
		this.player = player;
		this.world = world;
		this.file = file;
		this.format = format;

		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minZ = minZ;
		this.maxZ = maxZ;

		this.minChunkX = this.minX >> 4;
		this.maxChunkX = this.maxX >> 4;
		this.minChunkZ = this.minZ >> 4;
		this.maxChunkZ = this.maxZ >> 4;

		this.curChunkX = this.minChunkX;
		this.curChunkZ = this.minChunkZ;

		this.chunkCount = (this.maxChunkX - this.minChunkX + 1) * (this.maxChunkZ - this.minChunkZ + 1);
	}

	public void next() {
		if (!hasNext()) {
			return;
		}

		Reference.logger.debug("Copying chunk at [{},{}] into {}", this.curChunkX, this.curChunkZ,
		                       this.file.getName());
		Reference.proxy.copyChunkToSchematic(this.schematic, this.world, this.curChunkX, this.curChunkZ, this.minX,
		                                     this.maxX, this.minY, this.maxY, this.minZ, this.maxZ);

		this.processedChunks++;
		this.curChunkX++;
		if (this.curChunkX > this.maxChunkX) {
			this.curChunkX = this.minChunkX;
			this.curChunkZ++;
		}
	}

	public boolean hasNext() {
		return this.curChunkX <= this.maxChunkX && this.curChunkZ <= this.maxChunkZ;
	}

	public boolean isFirst() {
		return this.curChunkX == this.minChunkX && this.curChunkZ == this.minChunkZ;
	}
}
