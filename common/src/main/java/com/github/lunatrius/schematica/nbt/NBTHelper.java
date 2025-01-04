package com.github.lunatrius.schematica.nbt;

import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.world.LevelDummy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class NBTHelper {
	public static List<BlockEntity> readBlockEntitiesFromCompound(CompoundTag compound, Level level) {
		return readBlockEntitiesFromCompound(compound, level, new ArrayList<>());
	}

	public static List<BlockEntity> readBlockEntitiesFromCompound(CompoundTag compound, Level level,
	                                                              List<BlockEntity> tileEntities) {
		ListTag tagList = compound.getList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag BlockEntityCompound = tagList.getCompound(i);
			BlockEntity BlockEntity = readBlockEntityFromCompound(BlockEntityCompound, level);
			tileEntities.add(BlockEntity);
		}

		return tileEntities;
	}

	public static BlockEntity readBlockEntityFromCompound(CompoundTag blockEntityCompound, LevelReader level) {
		BlockPos pos = new BlockPos(blockEntityCompound.getShort("x"), blockEntityCompound.getShort("y"),
		                            blockEntityCompound.getShort("z"));
		return BlockEntity.loadStatic(pos, level.getBlockState(pos), blockEntityCompound, level.registryAccess());
	}

	public static CompoundTag writeBlockEntitiesToCompound(List<BlockEntity> tileEntities) {
		return writeBlockEntitiesToCompound(tileEntities, new CompoundTag());
	}

	public static CompoundTag writeBlockEntitiesToCompound(List<BlockEntity> tileEntities, CompoundTag compound) {
		ListTag tagList = new ListTag();
		for (BlockEntity BlockEntity : tileEntities) {
			CompoundTag BlockEntityCompound = writeBlockEntityToCompound(BlockEntity);
			tagList.add(BlockEntityCompound);
		}

		compound.put(Names.NBT.TILE_ENTITIES, tagList);

		return compound;
	}

	public static CompoundTag writeBlockEntityToCompound(BlockEntity blockEntity) {
		return blockEntity.saveWithFullMetadata(blockEntity.getLevel().registryAccess());
	}

	public static List<Entity> readEntitiesFromCompound(CompoundTag compound) {
		return readEntitiesFromCompound(compound, null, new ArrayList<>());
	}

	public static List<Entity> readEntitiesFromCompound(CompoundTag compound, Level level, List<Entity> entities) {
		ListTag tagList = compound.getList(Names.NBT.ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag entityCompound = tagList.getCompound(i);
			Entity entity = readEntityFromCompound(entityCompound, level);
			if (entity != null) {
				entities.add(entity);
			}
		}

		return entities;
	}

	public static Entity readEntityFromCompound(CompoundTag nbtTagCompound, Level level) {
		return EntityType.loadEntityRecursive(nbtTagCompound, level, EntitySpawnReason.LOAD, adapter -> adapter);
	}

	public static List<Entity> readEntitiesFromCompound(CompoundTag compound, Level level) {
		return readEntitiesFromCompound(compound, level, new ArrayList<>());
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
			entity.save(entityCompound);
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
				entity = readEntityFromCompound(entityCompound, LevelDummy.instance());

				if (entity != null) {
					entity.setPos(entity.getX() - offsetX, entity.getY() - offsetY, entity.getZ() - offsetZ);
				}
			}
		} catch (Throwable t) {
			throw new NBTConversionException(entity, t);
		}

		return entity;
	}

	public static CompoundTag writeEntityToCompound(Entity entity) {
		CompoundTag entityCompound = new CompoundTag();
		if (entity.save(entityCompound)) {
			return entityCompound;
		}

		return null;
	}
}