package com.github.lunatrius.schematica.world.schematic.format;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.proxy.PlatformProxy;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SchematicAlpha extends SchematicFormat {
	@Override
	public ISchematic readFromNbt(CompoundTag tagCompound, Level level) {
		SchematicMetadata metadata = readMetaFromNbt(tagCompound);

		List<BlockState> localBlockList =
				Arrays.stream(tagCompound.getIntArray(Names.NBT.BLOCKS)).mapToObj(Block::stateById).toList();
		BlockState[] localBlocks = localBlockList.toArray(new BlockState[]{});

		Map<BlockState, BlockState> oldToNew = new HashMap<>();
		if (tagCompound.contains(Names.NBT.MAPPING_SCHEMATICA)) {
			CompoundTag mapping = tagCompound.getCompound(Names.NBT.MAPPING_SCHEMATICA);
			Set<String> names = mapping.getAllKeys();
			for (String id : names) {
				BlockState toReplace;

				try {
					toReplace = Block.stateById(Integer.parseInt(id));
				} catch (NumberFormatException n) {
					toReplace = BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(id)).defaultBlockState();
				}

				BlockState replaceWith;

				String value = mapping.getString(id);

				if (value.isEmpty()) {
					replaceWith = Block.stateById(mapping.getInt(id));
				} else {
					ResourceLocation location = ResourceLocation.tryParse(mapping.getString(id));
					replaceWith = BuiltInRegistries.BLOCK.getValue(location).defaultBlockState();
				}

				oldToNew.put(toReplace, replaceWith);
			}
		}

		MBlockPos pos = new MBlockPos();
		ISchematic schematic = new Schematic(metadata);

		for (int x = 0; x < schematic.getWidth(); x++) {
			for (int y = 0; y < schematic.getHeight(); y++) {
				for (int z = 0; z < schematic.getLength(); z++) {
					int index = x + (y * schematic.getLength() + z) * schematic.getWidth();
					BlockState blockstate = localBlocks[index];

					if (oldToNew.containsKey(blockstate)) {
						blockstate = oldToNew.get(blockstate);
					}

					pos.set(x, y, z);

					try {
						schematic.setBlockState(pos, blockstate);
					} catch (Exception e) {
						Reference.logger.error("Could not set block state at {} to {} with blockstate {}", pos,
								blockstate.getBlock().arch$registryName(), blockstate, e);
					}
				}
			}
		}

		ListTag blockEntitiesList = tagCompound.getList(Names.NBT.BLOCK_ENTITIES, Tag.TAG_COMPOUND);

		for (int i = 0; i < blockEntitiesList.size(); i++) {
			try {
				BlockEntity blockEntity =
						NBTHelper.readBlockEntityFromCompound(blockEntitiesList.getCompound(i), level);
				if (blockEntity != null) {
					schematic.setBlockEntity(blockEntity.getBlockPos(), blockEntity);
				}
			} catch (Exception e) {
				Reference.logger.error("BlockEntity failed to load properly!", e);
			}
		}

		ListTag entitiesList = tagCompound.getList(Names.NBT.ENTITIES, Tag.TAG_COMPOUND);

		for (int i = 0; i < entitiesList.size(); i++) {
			try {
				Entity entity = NBTHelper.readEntityFromCompound(entitiesList.getCompound(i), level);

				if (entity != null) {
					schematic.addEntity(entity);
				}
			} catch (Exception e) {
				Reference.logger.error("Entity failed to load properly!", e);
			}
		}

		return schematic;
	}

	@Override
	public String getNbtName() {
		return Names.NBT.FORMAT_ALPHA;
	}

	@Override
	public void writeToNBT(CompoundTag tagCompoundIn, @NotNull ISchematic schematic) {
		CompoundTag tagCompound = new CompoundTag();

		int size = schematic.getWidth() * schematic.getLength() * schematic.getHeight();
		BlockState[] localBlocks = new BlockState[size];

		MBlockPos pos = new MBlockPos();
		Map<BlockState, BlockState> mappings = new HashMap<>();
		for (int x = 0; x < schematic.getWidth(); x++) {
			for (int y = 0; y < schematic.getHeight(); y++) {
				for (int z = 0; z < schematic.getLength(); z++) {
					int index = x + (y * schematic.getLength() + z) * schematic.getWidth();
					BlockState blockState = schematic.getBlockState(pos.set(x, y, z));
					localBlocks[index] = blockState;

					if (!mappings.containsKey(blockState)) {
						mappings.put(blockState, blockState);
					}
				}
			}
		}

		int count = 20;
		ListTag blockEntities = new ListTag();
		for (BlockEntity blockEntity : schematic.getBlockEntities()) {
			try {
				CompoundTag blockEntityCompoundTag = NBTHelper.writeBlockEntityToCompound(blockEntity);
				blockEntities.add(blockEntityCompoundTag);
			} catch (Exception e) {
				BlockPos bePos = blockEntity.getBlockPos();
				int index =
						bePos.getX() + (bePos.getY() * schematic.getLength() + bePos.getZ()) * schematic.getWidth();
				if (--count > 0) {
					BlockState blockState = schematic.getBlockState(bePos);
					Block block = blockState.getBlock();
					Reference.logger.error(
							"Block {}[{}] with BlockEntity {} failed to save! Replacing with air." + "..", block,
							BuiltInRegistries.BLOCK.getKey(block), blockEntity.getClass().getName(), e);
				}

				localBlocks[index] = Blocks.AIR.defaultBlockState();
			}
		}

		ListTag entityList = new ListTag();
		List<Entity> entities = schematic.getEntities();
		for (Entity entity : entities) {
			try {
				CompoundTag entityCompound = NBTHelper.writeEntityToCompound(entity);
				if (entityCompound != null) {
					entityList.add(entityCompound);
				}
			} catch (Throwable t) {
				Reference.logger.error("Entity {} failed to save, skipping!", entity, t);
			}
		}

		PlatformProxy.createAndPostPreSchematicSaveEvent(schematic, mappings);

		CompoundTag nbtMapping = new CompoundTag();
		for (Map.Entry<BlockState, BlockState> entry : mappings.entrySet()) {
			if (entry.getKey() != entry.getValue()) {
				nbtMapping.putInt(String.valueOf(Block.getId(entry.getKey())), Block.getId(entry.getValue()));
			}
		}

		tagCompound.putIntArray(Names.NBT.BLOCKS, Arrays.stream(localBlocks).mapToInt(Block::getId).toArray());
		tagCompound.put(Names.NBT.ENTITIES, entityList);
		tagCompound.put(Names.NBT.BLOCK_ENTITIES, blockEntities);
		tagCompound.put(Names.NBT.MAPPING_SCHEMATICA, nbtMapping);
		this.writeMetadataToNBT(tagCompound, schematic);
		tagCompoundIn.put(Names.NBT.ROOT, tagCompound);
	}

	@Override
	public String getName() {
		return Names.Formats.ALPHA;
	}

	@Override
	public String getExtension() {
		return Names.Extensions.SCHEMATIC;
	}

	public SchematicMetadata readMetaFromNbt(@NotNull CompoundTag tagCompound) {
		if (tagCompound.contains(Names.NBT.METADATA)) {
			CompoundTag tag = tagCompound.getCompound(Names.NBT.METADATA);
			return metaFromTag(tag);
		} else {
			return null;
		}
	}

	@Override
	public void writeMetadataToNBT(@NotNull CompoundTag tagCompound, @NotNull ISchematic schematic) {
		SchematicMetadata metadata = schematic.getMetadata();
		tagCompound.put(Names.NBT.METADATA, metaAsTag(metadata));
	}

	public @NotNull SchematicMetadata metaFromTag(@NotNull CompoundTag tag) {
		return SchematicFormat.defaultMetaFromTag(tag);
	}

	public @NotNull CompoundTag metaAsTag(@NotNull SchematicMetadata metadata) {
		return SchematicFormat.defaultMetaAsTag(metadata);
	}
}