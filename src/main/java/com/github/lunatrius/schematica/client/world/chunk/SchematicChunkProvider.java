package com.github.lunatrius.schematica.client.world.chunk;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// FIXME: `extends ChunkProviderClient` is required for the `WorldClient.getChunkProvider` method to work properly
@MethodsReturnNonnullByDefault
public class SchematicChunkProvider extends ClientChunkProvider {
	private final SchematicWorld world;
	private final Chunk emptyChunk;
	private final Map<Long, ChunkSchematic> chunks = new ConcurrentHashMap<>();

	public SchematicChunkProvider(final SchematicWorld world) {
		super(world, 8);
		this.world = world;
		this.emptyChunk = new EmptyChunk(world, new ChunkPos(0, 0)) {
			@Override
			public boolean isEmpty() {
				return false;
			}
		};
	}

	@Override
	public Chunk getChunkWithoutLoading(final int x, final int z) {
		if (!chunkExists(x, z)) {
			return this.emptyChunk;
		}

		final long key = ChunkPos.asLong(x, z);

		ChunkSchematic chunk = this.chunks.get(key);
		if (chunk == null) {
			Biome[] biomes = {Biomes.JUNGLE};
			chunk = new ChunkSchematic(this.world, x, z, new BiomeContainer(biomes));
			this.chunks.put(key, chunk);
		}

		return chunk;
	}

	@Override
	public boolean chunkExists(final int x, final int z) {
		return x >= 0 && z >= 0 && x < this.world.getWidth() && z < this.world.getLength();
	}

	// ChunkProviderClient
	@Override
	public void unloadChunk(int x, int z) {
		// NOOP: schematic chunks are part of the schematic world and are never unloaded separately
	}

	@Nullable
	@Override
	@ParametersAreNonnullByDefault
	public Chunk loadChunk(int x, int y, @Nullable BiomeContainer biomeContainerIn, PacketBuffer packetIn,
	                       CompoundNBT nbtTagIn, int sizeIn) {
		return super.loadChunk(x, y, biomeContainerIn, packetIn, nbtTagIn, sizeIn);
	}

	@Override
	public String makeString() {
		return "SchematicChunkCache";
	}
}
