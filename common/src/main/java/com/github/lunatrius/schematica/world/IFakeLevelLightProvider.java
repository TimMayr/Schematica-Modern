package com.github.lunatrius.schematica.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

/**
 * Loosely based on {@link BlockAndTintGetter}
 */
public interface IFakeLevelLightProvider {
	IFakeLevelLightProvider USE_CLIENT_LEVEL = new IFakeLevelLightProvider() {
		@Override
		public boolean forceOwnLightLevel() {
			return false;
		}

		@Override
		public int getSkyDarken() {
			throw new UnsupportedOperationException("Noop light provider");
		}

		@Override
		public int getBlockLight(final BlockPos pos) {
			throw new UnsupportedOperationException("Noop light provider");
		}
	};

	/**
	 * Returning false here means no other method from this iface will get called and all logic will be redirected to
	 * current client level.
	 *
	 * @return false if client level should be used instead
	 */
	boolean forceOwnLightLevel();

	/**
	 * @return sth sth vanilla daylight progress?
	 */
	int getSkyDarken();

	/**
	 * @return day time from 0 to 24000
	 */
	default long getDayTime() {
		return 6000; // noon
	}

	/**
	 * @return 0-15 lighting level for given pos
	 */
	default int getBrightness(final LightLayer lightLayer, final BlockPos pos) {
		return lightLayer == LightLayer.SKY ? getSkyLight(pos) : getBlockLight(pos);
	}

	/**
	 * @return 0-15 lighting level for given pos
	 */
	default int getSkyLight(final BlockPos pos) {
		return getBlockLight(pos);
	}

	/**
	 * @return 0-15 lighting level for given pos
	 */
	int getBlockLight(BlockPos pos);

	/**
	 * @return 0-15 lighting level for given pos
	 */
	default int getRawBrightness(final BlockPos pos, final int skyAmount) {
		final int sky = getSkyLight(pos) - skyAmount;
		final int block = getBlockLight(pos);
		return Math.max(block, sky);
	}
}