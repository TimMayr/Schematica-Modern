package com.github.lunatrius.schematica.world;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WorldDummy extends World {
	private static final WorldDummy instance;

	protected WorldDummy(WorldInfo worldInfo,
	                     DimensionType dimensionType,
	                     BiFunction<World, Dimension, AbstractChunkProvider> chunkProviderBiFunction,
	                     IProfiler profiler,
	                     boolean remote) {
		super(worldInfo, dimensionType, chunkProviderBiFunction, profiler, remote);
	}


	public static WorldDummy instance() {
		if (instance == null) {
			final WorldSettings worldSettings = new WorldSettings(0, GameType.CREATIVE, false, false, WorldType.FLAT);
			final WorldInfo worldInfo = new WorldInfo(worldSettings, "FakeWorld");
			instance = new WorldDummy();
		}

		return instance;
	}

	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

	}

	@Override
	public void playSound(@Nullable PlayerEntity player,
	                      double x,
	                      double y,
	                      double z,
	                      SoundEvent soundIn,
	                      SoundCategory category,
	                      float volume,
	                      float pitch) {

	}

	@Override
	public void playMovingSound(@Nullable PlayerEntity playerIn,
	                            Entity entityIn,
	                            SoundEvent eventIn,
	                            SoundCategory categoryIn,
	                            float volume,
	                            float pitch) {

	}

	@Nullable
	@Override
	public Entity getEntityByID(int id) {
		return null;
	}

	@Nullable
	@Override
	public MapData getMapData(String mapName) {
		return null;
	}

	@Override
	public void registerMapData(MapData mapDataIn) {

	}

	@Override
	public int getNextMapId() {
		return 0;
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}

	@Override
	public Scoreboard getScoreboard() {
		return null;
	}

	@Override
	public RecipeManager getRecipeManager() {
		return null;
	}

	@Override
	public NetworkTagManager getTags() {
		return null;
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return null;
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return null;
	}

	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {

	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return Collections.emptyList();
	}

	@Override
	public Biome getNoiseBiomeRaw(int x, int y, int z) {
		return null;
	}
}
