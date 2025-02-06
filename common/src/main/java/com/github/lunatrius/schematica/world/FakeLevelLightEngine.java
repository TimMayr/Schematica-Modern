package com.github.lunatrius.schematica.world;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage.SectionType;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
@MethodsReturnNonnullByDefault
public class FakeLevelLightEngine extends LevelLightEngine {
	private final FakeLevel fakeLevel;
	private FakeLevelLayerLightEventListener blockLightLayer = null;
	private FakeLevelLayerLightEventListener skyLightLayer = null;

	public FakeLevelLightEngine(FakeLevel level) {
		super(new LightChunkGetter() {
			@Override
			public LightChunk getChunkForLighting(final int p_63023_, final int p_63024_) {
				throw new UnsupportedOperationException("Should never happen - FakeLevel light engine ctor");
			}

			@Override
			public BlockGetter getLevel() {
				return level;
			}
		}, false, false);

		this.fakeLevel = level;
	}

	@Override
	public void checkBlock(@Nullable BlockPos ignored) {
		// Noop
	}

	@Override
	public boolean hasLightWork() {
		// Noop
		return false;
	}

	@Override
	public int runLightUpdates() {
		// Noop
		return 0;
	}

	@Override
	public void updateSectionStatus(@Nullable SectionPos ignored_1, boolean ignored_2) {
		// Noop
	}

	@Override
	public void setLightEnabled(@Nullable ChunkPos ignored_1, boolean ignored_2) {
		// Noop
	}

	@Override
	public void propagateLightSources(@Nullable ChunkPos ignored) {
		// Noop
	}

	@Override
	public LayerLightEventListener getLayerListener(LightLayer layer) {
		return switch (layer) {
			case BLOCK -> {
				if (blockLightLayer == null) {
					blockLightLayer = new FakeLevelLayerLightEventListener(layer);
				}
				yield blockLightLayer;
			}
			case SKY -> {
				if (skyLightLayer == null) {
					skyLightLayer = new FakeLevelLayerLightEventListener(layer);
				}
				yield skyLightLayer;
			}
		};
	}

	@Override
	public String getDebugData(@NotNull LightLayer layer, @Nullable SectionPos ignored) {
		return "FakeLevel light engine redirect - " + layer;
	}

	@Override
	public SectionType getDebugSectionType(@Nullable LightLayer ignored_1, @Nullable SectionPos ignored_2) {
		// Noop, only debug rendering ?
		return SectionType.EMPTY;
	}

	@Override
	public void queueSectionData(@Nullable LightLayer ignored_1, @Nullable SectionPos ignored_2,
	                             @Nullable DataLayer ignored_3) {
		// Noop
	}

	@Override
	public void retainData(@Nullable ChunkPos ignored_1, boolean ignored_2) {
		// Noop
	}

	@Override
	public int getRawBrightness(@NotNull BlockPos pos, int amount) {
		return fakeLevel.getRawBrightness(pos, amount);
	}

	@Override
	public boolean lightOnInColumn(long ignored) {
		return false;
	}

	@Override
	public void updateSectionStatus(@Nullable BlockPos ignored_1, boolean ignored_2) {
		// Noop
	}

	private class FakeLevelLayerLightEventListener implements LayerLightEventListener {
		private final LightLayer lightLayer;

		private FakeLevelLayerLightEventListener(LightLayer lightLayer) {
			this.lightLayer = lightLayer;
		}

		@Override
		public void checkBlock(@Nullable BlockPos ignored) {
			// Noop
		}

		@Override
		public boolean hasLightWork() {
			// Noop
			return false;
		}

		@Override
		public int runLightUpdates() {
			// Noop
			return 0;
		}

		@Override
		public void updateSectionStatus(@Nullable SectionPos ignored_1, boolean ignored_2) {
			// Noop
		}

		@Override
		public void setLightEnabled(@Nullable ChunkPos ignored_1, boolean ignored_2) {
			// Noop
		}

		@Override
		public void propagateLightSources(@Nullable ChunkPos ignored) {
			// Noop
		}

		@Override
		@Nullable
		public DataLayer getDataLayerData(@Nullable SectionPos ignored) {
			// Noop
			return null;
		}

		@Override
		public int getLightValue(@NotNull BlockPos pos) {
			return fakeLevel.getBrightness(lightLayer, pos);
		}
	}
}