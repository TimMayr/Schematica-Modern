package com.github.lunatrius.schematica.nbt;

import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.WorldDummy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class NBTHelper {
	public static List<BlockEntity> readBlockEntitiesFromCompound(CompoundTag compound) {
		return readBlockEntitiesFromCompound(compound, new ArrayList<>());
	}

	public static List<BlockEntity> readBlockEntitiesFromCompound(CompoundTag compound, List<BlockEntity> tileEntities) {
		ListTag tagList = compound.getList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag BlockEntityCompound = tagList.getCompound(i);
			BlockEntity BlockEntity = readBlockEntityFromCompound(BlockEntityCompound);
			tileEntities.add(BlockEntity);
		}

		return tileEntities;
	}

	public static BlockEntity readBlockEntityFromCompound(CompoundTag BlockEntityCompound) {

		return BlockEntity. (BlockEntityCompound);
	}

	public static CompoundTag writeTileEntitiesToCompound(List<BlockEntity> tileEntities) {
		return writeTileEntitiesToCompound(tileEntities, new CompoundTag());
	}

	public static CompoundTag writeTileEntitiesToCompound(List<BlockEntity> tileEntities, CompoundTag compound) {
		ListTag tagList = new ListTag();
		for (BlockEntity BlockEntity : tileEntities) {
			CompoundTag BlockEntityCompound = writeBlockEntityToCompound(BlockEntity);
			tagList.add(BlockEntityCompound);
		}

		compound.put(Names.NBT.TILE_ENTITIES, tagList);

		return compound;
	}

	public static CompoundTag writeBlockEntityToCompound(BlockEntity BlockEntity) {
		CompoundTag blockEntityCompound = new CompoundTag();
		BlockEntity.saveWithFullMetadata(blockEntityCompound);
		return blockEntityCompound;
	}

	public static List<Entity> readEntitiesFromCompound(CompoundTag compound) {
		return readEntitiesFromCompound(compound, null, new ArrayList<>());
	}

	public static List<Entity> readEntitiesFromCompound(CompoundTag compound, World world, List<Entity> entities) {
		ListTag tagList = compound.getList(Names.NBT.ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag entityCompound = tagList.getCompound(i);
			Entity entity = readEntityFromCompound(entityCompound, world);
			if (entity != null) {
				entities.add(entity);
			}
		}

		return entities;
	}

	public static Entity readEntityFromCompound(CompoundTag nbtTagCompound, World world) {
		return EntityType.loadEntityUnchecked(nbtTagCompound, world).orElse(null);
	}

	public static List<Entity> readEntitiesFromCompound(CompoundTag compound, World world) {
		return readEntitiesFromCompound(compound, world, new ArrayList<>());
	}

	public static void readEntitiesFromCompound(CompoundTag compound, List<Entity> entities) {
		readEntitiesFromCompound(compound, null, entities);
	}

	public static CompoundTag writeEntitiesToCompound(List<Entity> entities) {
		return writeEntitiesToCompound(entities, new CompoundTag());
	}

	public static CompoundTag writeEntitiesToCompound(List<Entity> entities, CompoundTag compound) {
		ListTag tagList = new ListTag();
		for (Entity entity : entities) {
			CompoundTag entityCompound = new CompoundTag();
			entity.writeUnlessPassenger(entityCompound);
			tagList.add(entityCompound);
		}

		compound.put(Names.NBT.ENTITIES, tagList);

		return compound;
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
			CompoundTag entityCompound = writeEntityToCompound(entity);
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

	public static CompoundTag writeEntityToCompound(Entity entity) {
		CompoundTag entityCompound = new CompoundTag();
		if (entity.writeUnlessPassenger(entityCompound)) {
			return entityCompound;
		}

		return null;
	}
}