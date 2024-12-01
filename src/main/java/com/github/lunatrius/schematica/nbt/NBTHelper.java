package com.github.lunatrius.schematica.nbt;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.WorldDummy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class NBTHelper {
	public static List<TileEntity> readTileEntitiesFromCompound(final CompoundNBT compound) {
		return readTileEntitiesFromCompound(compound, new ArrayList<>());
	}

	public static List<TileEntity> readTileEntitiesFromCompound(final CompoundNBT compound,
	                                                            final List<TileEntity> tileEntities) {
		final ListNBT tagList = compound.getList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			final CompoundNBT tileEntityCompound = tagList.getCompound(i);
			final TileEntity tileEntity = readTileEntityFromCompound(tileEntityCompound);
			tileEntities.add(tileEntity);
		}

		return tileEntities;
	}

	public static TileEntity readTileEntityFromCompound(final CompoundNBT tileEntityCompound) {
		return TileEntity.create(tileEntityCompound);
	}

	public static CompoundNBT writeTileEntitiesToCompound(final List<TileEntity> tileEntities) {
		return writeTileEntitiesToCompound(tileEntities, new CompoundNBT());
	}

	public static CompoundNBT writeTileEntitiesToCompound(final List<TileEntity> tileEntities,
	                                                      final CompoundNBT compound) {
		final ListNBT tagList = new ListNBT();
		for (final TileEntity tileEntity : tileEntities) {
			final CompoundNBT tileEntityCompound = writeTileEntityToCompound(tileEntity);
			tagList.add(tileEntityCompound);
		}

		compound.put(Names.NBT.TILE_ENTITIES, tagList);

		return compound;
	}

	public static CompoundNBT writeTileEntityToCompound(final TileEntity tileEntity) {
		final CompoundNBT tileEntityCompound = new CompoundNBT();
		tileEntity.write(tileEntityCompound);
		return tileEntityCompound;
	}

	public static List<Entity> readEntitiesFromCompound(final CompoundNBT compound) {
		return readEntitiesFromCompound(compound, null, new ArrayList<>());
	}

	public static List<Entity> readEntitiesFromCompound(final CompoundNBT compound, final World world,
	                                                    final List<Entity> entities) {
		final ListNBT tagList = compound.getList(Names.NBT.ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			final CompoundNBT entityCompound = tagList.getCompound(i);
			final Entity entity = readEntityFromCompound(entityCompound, world);
			if (entity != null) {
				entities.add(entity);
			}
		}

		return entities;
	}

	public static Entity readEntityFromCompound(final CompoundNBT nbtTagCompound, final World world) {
		return EntityType.loadEntityUnchecked(nbtTagCompound, world).orElse(null);
	}

	public static List<Entity> readEntitiesFromCompound(final CompoundNBT compound, final World world) {
		return readEntitiesFromCompound(compound, world, new ArrayList<>());
	}

	public static List<Entity> readEntitiesFromCompound(final CompoundNBT compound, final List<Entity> entities) {
		return readEntitiesFromCompound(compound, null, entities);
	}

	public static CompoundNBT writeEntitiesToCompound(final List<Entity> entities) {
		return writeEntitiesToCompound(entities, new CompoundNBT());
	}

	public static CompoundNBT writeEntitiesToCompound(final List<Entity> entities, final CompoundNBT compound) {
		final ListNBT tagList = new ListNBT();
		for (final Entity entity : entities) {
			final CompoundNBT entityCompound = new CompoundNBT();
			entity.writeUnlessPassenger(entityCompound);
			tagList.add(entityCompound);
		}

		compound.put(Names.NBT.ENTITIES, tagList);

		return compound;
	}

	public static TileEntity reloadTileEntity(final TileEntity tileEntity) throws NBTConversionException {
		return reloadTileEntity(tileEntity, 0, 0, 0);
	}

	public static TileEntity reloadTileEntity(TileEntity tileEntity, final int offsetX, final int offsetY,
	                                          final int offsetZ) throws NBTConversionException {
		if (tileEntity == null) {
			return null;
		}

		try {
			final CompoundNBT tileEntityCompound = writeTileEntityToCompound(tileEntity);
			tileEntity = readTileEntityFromCompound(tileEntityCompound);
			final BlockPos pos = tileEntity.getPos();
			tileEntity.setPos(pos.add(-offsetX, -offsetY, -offsetZ));
		} catch (final Throwable t) {
			throw new NBTConversionException(tileEntity, t);
		}

		return tileEntity;
	}

	public static Entity reloadEntity(final Entity entity) throws NBTConversionException {
		return reloadEntity(entity, 0, 0, 0);
	}

	public static Entity reloadEntity(Entity entity, final int offsetX, final int offsetY, final int offsetZ)
			throws NBTConversionException {
		if (entity == null) {
			return null;
		}

		try {
			final CompoundNBT entityCompound = writeEntityToCompound(entity);
			if (entityCompound != null) {
				entity = readEntityFromCompound(entityCompound, WorldDummy.instance());

				if (entity != null) {
					entity.setPosition(entity.getPosX() - offsetX, entity.getPosY() - offsetY,
							entity.getPosZ() - offsetZ);
				}
			}
		} catch (final Throwable t) {
			throw new NBTConversionException(entity, t);
		}

		return entity;
	}

	public static CompoundNBT writeEntityToCompound(final Entity entity) {
		final CompoundNBT entityCompound = new CompoundNBT();
		if (entity.writeUnlessPassenger(entityCompound)) {
			return entityCompound;
		}

		return null;
	}
}
