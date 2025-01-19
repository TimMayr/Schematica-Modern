package com.github.lunatrius.schematica.nbt;

import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class NBTHelper {
	public static void readBlockEntitiesFromCompound(CompoundTag compound, Level level,
	                                                 List<BlockEntity> blockEntities) {
		ListTag tagList = compound.getList(Names.NBT.BLOCK_ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag BlockEntityCompound = tagList.getCompound(i);
			BlockEntity BlockEntity = readBlockEntityFromCompound(BlockEntityCompound, level);
			blockEntities.add(BlockEntity);
		}
	}

	public static BlockEntity readBlockEntityFromCompound(CompoundTag blockEntityCompound, Level level) {
		BlockPos pos = new BlockPos(blockEntityCompound.getShort("x"), blockEntityCompound.getShort("y"),
		                            blockEntityCompound.getShort("z"));
		return readBlockEntityFromCompound(pos, blockEntityCompound, level.getBlockState(pos));
	}

	public static BlockEntity readBlockEntityFromCompound(BlockPos pos, CompoundTag blockEntityCompound,
	                                                      BlockState state) {
		return BlockEntity.loadStatic(pos, state, blockEntityCompound, Reference.proxy.getRegistryAccess());
	}

	public static BlockEntity readBlockEntityFromCompound(CompoundTag blockEntityCompound, BlockState state) {
		BlockPos pos = new BlockPos(blockEntityCompound.getShort("x"), blockEntityCompound.getShort("y"),
		                            blockEntityCompound.getShort("z"));
		return BlockEntity.loadStatic(pos, state, blockEntityCompound, Reference.proxy.getRegistryAccess());
	}

	public static CompoundTag writeBlockEntitiesToCompound(List<BlockEntity> blockEntities) {
		return writeBlockEntitiesToCompound(blockEntities, new CompoundTag());
	}

	public static CompoundTag writeBlockEntitiesToCompound(List<BlockEntity> blockEntities, CompoundTag compound) {
		ListTag tagList = new ListTag();
		for (BlockEntity BlockEntity : blockEntities) {
			CompoundTag BlockEntityCompound = writeBlockEntityToCompound(BlockEntity);
			tagList.add(BlockEntityCompound);
		}

		compound.put(Names.NBT.BLOCK_ENTITIES, tagList);

		return compound;
	}

	public static CompoundTag writeBlockEntityToCompound(BlockEntity blockEntity) {
		return blockEntity.saveWithFullMetadata(blockEntity.getLevel().registryAccess());
	}

	public static void readEntitiesFromCompound(CompoundTag compound, List<Entity> entities, Level level) {
		readEntitiesFromCompound(compound, level, entities);
	}

	public static void readEntitiesFromCompound(CompoundTag compound, Level level, List<Entity> entities) {
		ListTag tagList = compound.getList(Names.NBT.ENTITIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag entityCompound = tagList.getCompound(i);
			Entity entity = readEntityFromCompound(entityCompound, level);
			if (entity != null) {
				entities.add(entity);
			}
		}
	}

	public static Entity readEntityFromCompound(CompoundTag nbtTagCompound, Level level) {
		return EntityType.loadEntityRecursive(nbtTagCompound, level, EntitySpawnReason.LOAD, adapter -> adapter);
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

	public static CompoundTag writeEntityToCompound(Entity entity) {
		CompoundTag entityCompound = new CompoundTag();
		if (entity.save(entityCompound)) {
			return entityCompound;
		}

		return null;
	}
}