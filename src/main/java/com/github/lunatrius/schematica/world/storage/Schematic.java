package com.github.lunatrius.schematica.world.storage;

import com.github.lunatrius.schematica.api.ISchematic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeBlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Schematic implements ISchematic {
	private static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.GRASS);
	private final BlockState[][][] blockstates;
	private final List<TileEntity> tileEntities = new ArrayList<>();
	private final List<Entity> entities = new ArrayList<Entity>();
	private final int width;
	private final int height;
	private final int length;
	private ItemStack icon;
	private String author;

	public Schematic(ItemStack icon, int width, int height, int length) {
		this(icon, width, height, length, "");
	}

	public Schematic(ItemStack icon, int width, int height, int length, @Nonnull String author) {
		this.icon = icon;
		this.blockstates = new BlockState[width][height][length];

		this.width = width;
		this.height = height;
		this.length = length;

		this.author = author;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (!isValid(pos)) {
			return Blocks.AIR.getDefaultState();
		}

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		return blockstates[x][y][z];
	}

	private boolean isValid(BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		return !(x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length);
	}

	@Override
	public boolean setBlockState(BlockPos pos, IForgeBlockState blockState) {
		if (!isValid(pos)) {
			return false;
		}

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		this.blockstates[x][y][z] = blockState.getBlockState();
		return true;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		for (TileEntity tileEntity : this.tileEntities) {
			if (tileEntity.getPos().equals(pos)) {
				return tileEntity;
			}
		}

		return null;
	}

	@Override
	public List<TileEntity> getTileEntities() {
		return this.tileEntities;
	}

	@Override
	public void setTileEntity(BlockPos pos, TileEntity tileEntity) {
		if (!isValid(pos)) {
			return;
		}

		removeTileEntity(pos);

		if (tileEntity != null) {
			this.tileEntities.add(tileEntity);
		}
	}

	@Override
	public void removeTileEntity(BlockPos pos) {
		this.tileEntities.removeIf(tileEntity -> tileEntity.getPos().equals(pos));
	}

	@Override
	public List<Entity> getEntities() {
		return this.entities;
	}

	@Override
	public void addEntity(Entity entity) {
		if (entity == null || entity instanceof PlayerEntity) {
			return;
		}

		for (Entity e : this.entities) {
			if (entity.getUniqueID().equals(e.getUniqueID())) {
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

		this.entities.removeIf(e -> entity.getUniqueID().equals(e.getUniqueID()));
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
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getLength() {
		return this.length;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	@Nonnull
	public String getAuthor() {
		return this.author;
	}

	@Override
	public void setAuthor(@Nonnull String author) {
		this.author = author;
	}
}
