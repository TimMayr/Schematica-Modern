package com.github.lunatrius.schematica.world.chunk;

import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.FakeLevelChunkSection;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Fake level fake chunk :D all data related methods must redirect to fake level Updating procedure is same as fakeLevel
 * Porting info:
 * <ol>
 * <li>uncomment last method section</li>
 * <li>fix compile errors</li>
 * <li>add override for remaining methods and sort/implement them accordingly</li>
 * <li>comment last method section</li>
 * </ol>
 * <p>
 */
@MethodsReturnNonnullByDefault
public class FakeChunk extends LevelChunk {
	private final FakeLevel fakeLevel;

	// section cache
	public int lastY;
	public LevelChunkSection lastSection = null;

	public FakeChunk(final FakeLevel worldIn, final int x, final int z) {
		super(worldIn, new ChunkPos(x, z));
		this.fakeLevel = worldIn;

		// set itself to cache
		fakeLevel.lastX = x;
		fakeLevel.lastZ = z;
		fakeLevel.lastChunk = this;
	}

	// ========================================
	// ========== REDIRECTED METHODS ==========
	// ========================================

	@Override
	public Set<BlockPos> getBlockEntitiesPos() {
		return getBlockEntities().keySet();
	}

	@Override
	public LevelChunkSection[] getSections() {
		// don't cache them
		return new LevelChunkSection[0];
	}

	@Override
	public LevelChunkSection getSection(int yIdx) {
		if (lastY == yIdx && lastSection != null) {
			return lastSection;
		}
		return new FakeLevelChunkSection(this, yIdx);
	}

	@Override
	public Collection<Entry<Types, Heightmap>> getHeightmaps() {
		return Collections.emptyList();
	}

	@Override
	public void setHeightmap(@Nullable Types ignored_1, long @Nullable [] ignored_2) {
		// Noop
	}

	@Override
	public Heightmap getOrCreateHeightmapUnprimed(@Nullable Types ignored) {
		return null;
	}

	@Override
	public boolean hasPrimedHeightmap(@Nullable Types ignored) {
		return false;
	}

	// ========================================
	// ======= NOOP UNSAFE NULL METHODS =======
	// ========================================

	// ========================================
	// ========== PERMANENT SETTINGS ==========
	// ========================================

	@Override
	public int getHeight(@NotNull Types type, int x, int z) {
		return fakeLevel.getHeight(type, chunkPos.getBlockX(x), chunkPos.getBlockZ(z));
	}

	@Override
	public void setStartForStructure(@Nullable Structure ignored_1, @Nullable StructureStart ignored_2) {
		// Noop
	}

	@Override
	public void setAllStarts(@Nullable Map<Structure, StructureStart> ignored) {
		// Noop
	}

	@Override
	public void addReferenceForStructure(@Nullable Structure ignored_1, long ignored_2) {
		// Noop
	}

	// ========================================
	// ========== HEIGHTMAP RELATED ===========
	// ========================================

	@Override
	public void setAllReferences(@Nullable Map<Structure, LongSet> ignored) {
		// Noop
	}

	@Override
	public boolean isYSpaceEmpty(int p_62075_, int p_62076_) {
		return false;
	}

	@Override
	public boolean isUnsaved() {
		return false;
	}

	@Override
	public void addPackedPostProcess(@Nullable ShortList ignored_1, int ignored_2) {
// Noop
	}

	// ========================================
	// =========== SECTION RELATED ============
	// ========================================

	@Override
	public void setBlockEntityNbt(@Nullable CompoundTag ignored) {
		// Noop
	}

	@Override
	@Nullable
	public CompoundTag getBlockEntityNbt(@Nullable BlockPos ignored) {
		// Noop, for pending BEs only
		return null;
	}

	@Override
	public void findBlocks(@NotNull Predicate<BlockState> predicate, @NotNull BiConsumer<BlockPos, BlockState> sink) {
		for (BlockPos mutablePos : BlockPos.betweenClosed(chunkPos.getBlockX(0), fakeLevel.getLevelSource().getMinY(),
		                                                  chunkPos.getBlockZ(0), Math.min(chunkPos.getBlockX(15),
		                                                                                  fakeLevel.getLevelSource()
		                                                                                           .getMaxX() - 1),
		                                                  fakeLevel.getLevelSource().getMaxY() - 1,
		                                                  Math.min(chunkPos.getBlockZ(15),
		                                                           fakeLevel.getLevelSource().getMaxZ() - 1))) {
			BlockState blockState = getBlockState(mutablePos);
			if (predicate.test(blockState)) {
				sink.accept(mutablePos, blockState);
			}
		}
	}

	@Override
	public boolean isLightCorrect() {
		return true;
	}

	// ========================================
	// ============= NOOP METHODS =============
	// ========================================

	@Override
	public void setLightCorrect(boolean ignored) {
		// Noop
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z) {
		return fakeLevel.getNoiseBiome(x, y, z);
	}

	@Override
	public void fillBiomesFromNoise(@Nullable BiomeResolver ignored_1, @Nullable Sampler ignored_2) {
		// Noop
	}

	@Override
	public boolean isUpgrading() {
		return false;
	}

	@Override
	public void markUnsaved() {
		// Noop
	}

	@Override
	public TickContainerAccess<Block> getBlockTicks() {
		// Noop
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public TickContainerAccess<Fluid> getFluidTicks() {
		// Noop
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public BlockState getBlockState(@NotNull BlockPos pos) {
		return fakeLevel.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(@NotNull BlockPos pos) {
		return fakeLevel.getFluidState(pos);
	}

	@Override
	public FluidState getFluidState(int bx, int by, int bz) {
		return getFluidState(new BlockPos(bx, by, bz));
	}

	@Override
	@Nullable
	public BlockState setBlockState(@Nullable BlockPos ignored_1, @Nullable BlockState ignored_2, boolean ignored_3) {
		// Noop
		return null;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(@NotNull BlockPos pos, @Nullable EntityCreationType ignored) {
		return fakeLevel.getBlockEntity(pos);
	}

	@Override
	public void addAndRegisterBlockEntity(@Nullable BlockEntity ignored) {
		// Noop
	}

	@Override
	public void setBlockEntity(@Nullable BlockEntity ignored) {
		// Noop
	}

	@Override
	public @Nullable CompoundTag getBlockEntityNbtForSaving(@Nullable BlockPos ignored_1,
	                                                        HolderLookup.@Nullable Provider ignored_2) {
		return null;
	}

	@Override
	public void removeBlockEntity(@Nullable BlockPos ignored) {
		// Noop
	}

	@Override
	public void replaceWithPacketData(@Nullable FriendlyByteBuf ignored_1, @Nullable CompoundTag ignored_2,
	                                  @Nullable Consumer<BlockEntityTagOutput> ignored_3) {
		// Noop
	}

	@Override
	public void replaceBiomes(@Nullable FriendlyByteBuf ignored) {
		// Noop
	}

	@Override
	public Map<BlockPos, BlockEntity> getBlockEntities() {
		AABB aabb = new AABB((this.chunkPos.x - 1) * 16, this.fakeLevel.getLevelSource().getMinY(),
		                     (this.chunkPos.z - 1) * 16, this.chunkPos.x * 16,
		                     this.fakeLevel.getLevelSource().getMaxY(), this.chunkPos.z * 16);
		return fakeLevel.blockEntities.entrySet()
		                              .stream()
		                              .filter(blockPosBlockEntityEntry -> aabb.contains(
				                              Vec3.atLowerCornerOf(blockPosBlockEntityEntry.getKey())))
		                              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y,
		                                                        LinkedHashMap::new));
	}

	@Override
	public void postProcessGeneration(@Nullable ServerLevel ignored) {
		// Noop
	}

	@Override
	public void unpackTicks(long ignored) {
		// Noop
	}

	@Override
	public FullChunkStatus getFullStatus() {
		return FullChunkStatus.FULL;
	}

	@Override
	public void setFullStatus(@Nullable Supplier<FullChunkStatus> ignored) {
		// Noop
	}

	@Override
	public void registerAllBlockEntitiesAfterLevelLoad() {
		// Noop
	}
}