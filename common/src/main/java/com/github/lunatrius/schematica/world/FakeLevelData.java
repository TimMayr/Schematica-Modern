package com.github.lunatrius.schematica.world;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
@MethodsReturnNonnullByDefault
public class FakeLevelData implements WritableLevelData {
	protected final IFakeLevelLightProvider lightProvider;
	protected Supplier<LevelData> vanillaLevelData;

	protected FakeLevelData(Supplier<LevelData> vanillaLevelData, IFakeLevelLightProvider lightProvider) {
		this.vanillaLevelData = vanillaLevelData;
		this.lightProvider = lightProvider;
	}

	@Override
	public BlockPos getSpawnPos() {
		return new BlockPos(0, 0, 0);
	}

	@Override
	public float getSpawnAngle() {
		return 0;
	}

	@Override
	public long getGameTime() {
		return vanillaLevelData.get().getGameTime();
	}

	@Override
	public long getDayTime() {
		return lightProvider.forceOwnLightLevel() ? lightProvider.getDayTime() : vanillaLevelData.get().getDayTime();
	}

	@Override
	public boolean isThundering() {
		return false;
	}

	@Override
	public boolean isRaining() {
		return false;
	}

	@Override
	public void setRaining(final boolean p_78171_) {
		// Noop
	}

	@Override
	public boolean isHardcore() {
		return false;
	}

	@Override
	public Difficulty getDifficulty() {
		// would like peaceful but don't want to trigger entity remove in case someone actually manage to tick fake
		// level
		return Difficulty.EASY;
	}

	@Override
	public boolean isDifficultyLocked() {
		return true;
	}

	@Override
	public void setSpawn(@NotNull BlockPos spawnPoint, float spawnAngle) {
		//N oop
	}
}