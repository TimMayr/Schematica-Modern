package com.github.lunatrius.schematica.world.storage;

import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.block.state.pattern.BlockStateReplacer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
public class Schematic implements ISchematic {
	private static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.GRASS_BLOCK);
	private final BlockState[][][] blockstates;
	private final List<BlockEntity> blockEntities = new ArrayList<>();
	private final List<Entity> entities = new ArrayList<>();
	private final int width;
	private final int height;
	private final int length;
	private ItemStack icon;
	private String author;

	public Schematic(ItemStack icon, int width, int height, int length) {
		this(icon, width, height, length, "");
	}

	public Schematic(ItemStack icon, int width, int height, int length, String author) {
		this.icon = icon;
		this.blockstates = new BlockState[width][height][length];

		this.width = width;
		this.height = height;
		this.length = length;

		this.author = author;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(@NotNull BlockPos pos) {
		for (BlockEntity blockEntity : this.blockEntities) {
			if (blockEntity.getBlockPos().equals(pos)) {
				return blockEntity;
			}
		}

		return null;
	}

	private boolean isValid(@NotNull BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		return !(x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState blockState) {
		if (!isValid(pos)) {
			return false;
		}

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		this.blockstates[x][y][z] = blockState;
		return true;
	}

	@Override
	public BlockState getBlockState(@NotNull BlockPos pos) {
		if (!isValid(pos)) {
			return Blocks.AIR.defaultBlockState();
		}

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		return blockstates[x][y][z];
	}

	@Override
	public List<BlockEntity> getBlockEntities() {
		return this.blockEntities;
	}

	public void setBlockEntity(BlockPos pos, BlockEntity blockEntity) {
		if (!isValid(pos)) {
			return;
		}

		removeBlockEntity(pos);

		if (blockEntity != null) {
			this.blockEntities.add(blockEntity);
		}
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
		this.blockEntities.removeIf(blockEntity -> blockEntity.getBlockPos().equals(pos));
	}

	@Override
	public List<Entity> getEntities() {
		return this.entities;
	}

	@Override
	public void addEntity(Entity entity) {
		if (entity == null || entity instanceof Player) {
			return;
		}

		for (Entity e : this.entities) {
			if (entity.getUUID().equals(e.getUUID())) {
				return;
			}
		}

		this.entities.add(entity);
	}

	@Override
	public void removeEntity(Entity entity) {
		if (entity == null) {
			return;
		}

		this.entities.removeIf(e -> entity.getUUID().equals(e.getUUID()));
	}

	@Override
	public ItemStack getIcon() {
		return this.icon;
	}

	@Override
	public void setIcon(ItemStack icon) {
		if (icon != null) {
			this.icon = icon;
		} else {
			this.icon = DEFAULT_ICON.copy();
		}
	}

	@Override
	public int getSizeX() {
		return this.width;
	}

	@Override
	public int getSizeZ() {
		return this.length;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public String getAuthor() {
		return this.author;
	}

	@Override
	public void setAuthor(String author) {
		this.author = author;
	}
}