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
	public static List<TileEntity> readTileEntitiesFromCompound(CompoundNBT compound) {
		return readTileEntitiesFromCompound(compound, new ArrayList<>());
	}

	public static List<TileEntity> readTileEntitiesFromCompound(CompoundNBT compound, List<TileEntity> tileEntities) {
		ListNBT tagList = compound.getList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundNBT tileEntityCompound = tagList.getCompound(i);
			TileEntity tileEntity = readTileEntityFromCompound(tileEntityCompound);
			tileEntities.add(tileEntity);
		}

		return tileEntities;
	}

	public static TileEntity readTileEntityFromCompound(CompoundNBT tileEntityCompound) {
		return TileEntity.create(tileEntityCompound);
	}

	public static CompoundNBT writeTileEntitiesToCompound(List<TileEntity> tileEntities) {
		return writeTileEntitiesToCompound(tileEntities, new CompoundNBT());
	}

	public static CompoundNBT writeTileEntitiesToCompound(List<TileEntity> tileEntities, CompoundNBT compound) {
		ListNBT tagList = new ListNBT();
		for (TileEntity tileEntity : tileEntities) {
			CompoundNBT tileEntityCompound = writeTileEntityToCompound(tileEntity);
			tagList.add(tileEntityCompound);
		}

		compound.put(Names.NBT.TILE_ENTITIES, tagList);

		return compound;
	}

	public static CompoundNBT writeTileEntityToCompound(TileEntity tileEntity) {
		CompoundNBT tileEntityCompound = new CompoundNBT();
		tileEntity.write(tileEntityCompound);
		return tileEntityCompound;
	}

	public static List<Entity> readEntitiesFromCompound(CompoundNBT compound) {
		return readEntitiesFromCompound(compound, null, new ArrayList<>());
	}

	public static List<Entity> readEntitiesFromCompound(CompoundNBT compound, World world, List<Entity> entities) {
		ListNBT tagList = compound.getList(Names.NBT.ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundNBT entityCompound = tagList.getCompound(i);
			Entity entity = readEntityFromCompound(entityCompound, world);
			if (entity != null) {
				entities.add(entity);
			}
		}

		return entities;
	}

	public static Entity readEntityFromCompound(CompoundNBT nbtTagCompound, World world) {
		return EntityType.loadEntityUnchecked(nbtTagCompound, world).orElse(null);
	}

	public static List<Entity> readEntitiesFromCompound(CompoundNBT compound, World world) {
		return readEntitiesFromCompound(compound, world, new ArrayList<>());
	}

	public static List<Entity> readEntitiesFromCompound(CompoundNBT compound, List<Entity> entities) {
		return readEntitiesFromCompound(compound, null, entities);
	}

	public static CompoundNBT writeEntitiesToCompound(List<Entity> entities) {
		return writeEntitiesToCompound(entities, new CompoundNBT());
	}

	public static CompoundNBT writeEntitiesToCompound(List<Entity> entities, CompoundNBT compound) {
		ListNBT tagList = new ListNBT();
		for (Entity entity : entities) {
			CompoundNBT entityCompound = new CompoundNBT();
			entity.writeUnlessPassenger(entityCompound);
			tagList.add(entityCompound);
		}

		compound.put(Names.NBT.ENTITIES, tagList);

		return compound;
	}

	public static TileEntity reloadTileEntity(TileEntity tileEntity) throws NBTConversionException {
		return reloadTileEntity(tileEntity, 0, 0, 0);
	}

	public static TileEntity reloadTileEntity(TileEntity tileEntity, int offsetX, int offsetY, int offsetZ)
			throws NBTConversionException {
		if (tileEntity == null) {
			return null;
		}

		try {
			CompoundNBT tileEntityCompound = writeTileEntityToCompound(tileEntity);
			tileEntity = readTileEntityFromCompound(tileEntityCompound);
			BlockPos pos = tileEntity.getPos();
			tileEntity.setPos(pos.add(-offsetX, -offsetY, -offsetZ));
		} catch (Throwable t) {
			throw new NBTConversionException(tileEntity, t);
		}

		return tileEntity;
	}

	public static Entity reloadEntity(Entity entity) throws NBTConversionException {
		return reloadEntity(entity, 0, 0, 0);
	}

	public static Entity reloadEntity(Entity entity, int offsetX, int offsetY, int offsetZ)
			throws NBTConversionException {
		if (entity == null) {
			return null;
		}

		try {
			CompoundNBT entityCompound = writeEntityToCompound(entity);
			if (entityCompound != null) {
				entity = readEntityFromCompound(entityCompound, WorldDummy.instance());

				if (entity != null) {
					entity.setPosition(entity.getPosX() - offsetX, entity.getPosY() - offsetY,
					                   entity.getPosZ() - offsetZ);
				}
			}
		} catch (Throwable t) {
			throw new NBTConversionException(entity, t);
		}

		return entity;
	}

	public static CompoundNBT writeEntityToCompound(Entity entity) {
		CompoundNBT entityCompound = new CompoundNBT();
		if (entity.writeUnlessPassenger(entityCompound)) {
			return entityCompound;
		}

		return null;
	}
}
