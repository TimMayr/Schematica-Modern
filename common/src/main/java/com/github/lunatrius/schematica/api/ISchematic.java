package com.github.lunatrius.schematica.api;

import net.minecraft.CrashReportCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
public interface ISchematic extends BlockGetter {
	/**
	 * Gets the block entity at the requested location. If no block entity exists at that location, null will be
	 * returned.
	 *
	 * @param pos
	 * 		the location in world space.
	 *
	 * @return the located block entity.
	 */
	@Nullable BlockEntity getBlockEntity(@NotNull BlockPos pos);

	/**
	 * Gets a block state at a given location within the schematic. Requesting a block state outside of those bounds
	 * returns the default block state for air.
	 *
	 * @param pos
	 * 		the location in world space.
	 *
	 * @return the block at the requested location.
	 */

	BlockState getBlockState(@NotNull BlockPos pos);

	@Override
	default FluidState getFluidState(@NotNull BlockPos pos) {
		return isPosInside(pos) ? getBlockState(pos).getFluidState() : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	default int getMaxY() {
		return getMinY() + getHeight();
	}

	/**
	 * @param pos
	 * 		tested pos
	 *
	 * @return true if inside aabb
	 *
	 * @see #isOutsideBuildHeight(BlockPos) extension of
	 */
	default boolean isPosInside(@NotNull BlockPos pos) {
		return getMinX() <= pos.getX()
				&& pos.getX() < getMaxX()
				&& getMinY() <= pos.getY()
				&& pos.getY() <= getMaxY()
				&& getMinZ() <= pos.getZ()
				&& pos.getZ() < getMaxZ();
	}

	/**
	 * @return min X coord inclusive
	 */
	default int getMinX() {
		return 0;
	}

	/**
	 * @return min Z coord inclusive
	 */
	default int getMinZ() {
		return 0;
	}

	/**
	 * @return max X coord exclusive
	 */
	default int getMaxX() {
		return getMinX() + getSizeX();
	}

	/**
	 * The width of the schematic
	 *
	 * @return the schematic width
	 */
	int getSizeX();

	/**
	 * @return max Z coord exclusive
	 */
	default int getMaxZ() {
		return getMinZ() + getSizeZ();
	}

	/**
	 * The length of the schematic
	 *
	 * @return the schematic length
	 */
	int getSizeZ();

	/**
	 * Sets the block state at the given location. Attempting to set a block state outside of the schematic
	 * boundaries or with an invalid block state will result in no change being made and this method will return false.
	 *
	 * @param pos
	 * 		the location in world space.
	 * @param blockState
	 * 		the block state to set
	 *
	 * @return true if the block state was successfully set.
	 */
	boolean setBlockState(BlockPos pos, BlockState blockState);

	/**
	 * Returns a list of all entities in the schematic.
	 *
	 * @return all entities.
	 */
	List<Entity> getEntities();

	/**
	 * Adds an entity to the schematic if it's not a player.
	 *
	 * @param entity
	 * 		the entity to add.
	 */
	void addEntity(Entity entity);

	/**
	 * Removes an entity from the schematic.
	 *
	 * @param entity
	 * 		the entity to remove.
	 */
	void removeEntity(Entity entity);

	/**
	 * Retrieves the icon that will be used to save the schematic.
	 *
	 * @return the schematic's future icon.
	 */
	ItemStack getIcon();

	/**
	 * Modifies the icon that will be used when saving the schematic.
	 *
	 * @param icon
	 * 		an ItemStack of the Item you wish you use as the icon.
	 */
	void setIcon(ItemStack icon);

	/**
	 * Gets the author of the schematic, or an empty String if unknown.
	 *
	 * @return The author of the schematic.
	 */
	String getAuthor();

	/**
	 * Sets the author of the schematic.
	 *
	 * @param author
	 * 		The new author of the schematic.
	 */
	void setAuthor(String author);

	/**
	 * Returns a list of all block entities in the schematic.
	 *
	 * @return all block entities.
	 */
	List<BlockEntity> getBlockEntities();

	/**
	 * Add or replace a block entity to a block at the requested location. Does nothing if the location is out of
	 * bounds.
	 *
	 * @param pos
	 * 		the location in world space.
	 * @param blockEntity
	 * 		the block entity to set.
	 */
	void setBlockEntity(BlockPos pos, BlockEntity blockEntity);

	/**
	 * Removes a block entity from the specific location if it exists, otherwise it silently continues.
	 *
	 * @param pos
	 * 		the location in world space.
	 */
	void removeBlockEntity(BlockPos pos);

	/**
	 * The height of the schematic
	 *
	 * @return the schematic height
	 */
	int getHeight();

	default int getMinY() {
		return 0;
	}

	/**
	 * @param pos
	 * 		tested pos
	 *
	 * @return true if outside aabb
	 *
	 * @see #isOutsideBuildHeight(BlockPos) extension of
	 */
	default boolean isPosOutside(BlockPos pos) {
		return !isPosInside(pos);
	}

	/**
	 * To show who is this fake level in level crashes
	 */
	default void describeSelfInCrashReport(CrashReportCategory ignoredCategory) {}

	/**
	 * @return function useful temporary insert into existing world
	 *
	 * @see #getRawBlockState(BlockPos)
	 */
	default Function<BlockPos, @Nullable BlockState> getRawBlockStateFunction() {
		return this::getRawBlockState;
	}

	/**
	 * @return null if pos is outside of aabb
	 */
	@Nullable
	default BlockState getRawBlockState(BlockPos pos) {
		return isPosInside(pos) ? getBlockState(pos) : null;
	}

	/**
	 * @return aabb with end being blockpos-wise exclusive
	 */
	default AABB getAABB() {
		return new AABB(getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ());
	}
}