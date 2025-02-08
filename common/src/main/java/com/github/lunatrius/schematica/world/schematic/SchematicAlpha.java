package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.proxy.PlatformProxy;
import com.github.lunatrius.schematica.reference.Constants;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SchematicAlpha extends SchematicFormat {
	@Override
	public ISchematic readFromNBT(CompoundTag tagCompound, Level level) {
		ItemStack icon = SchematicUtil.getIconFromNBT(tagCompound);

		List<BlockState> localBlockList =
				Arrays.stream(tagCompound.getIntArray(Names.NBT.BLOCKS)).mapToObj(Block::stateById).toList();
		BlockState[] localBlocks = localBlockList.toArray(new BlockState[]{});

		int width = tagCompound.getInt(Names.NBT.WIDTH);
		int length = tagCompound.getInt(Names.NBT.LENGTH);
		int height = tagCompound.getInt(Names.NBT.HEIGHT);

		Map<BlockState, BlockState> oldToNew = new HashMap<>();
		if (tagCompound.contains(Names.NBT.MAPPING_SCHEMATICA)) {
			CompoundTag mapping = tagCompound.getCompound(Names.NBT.MAPPING_SCHEMATICA);
			Set<String> names = mapping.getAllKeys();
			for (String id : names) {
				oldToNew.put(Block.stateById(Integer.parseInt(id)), Block.stateById(mapping.getInt(id)));
			}
		}

		MBlockPos pos = new MBlockPos();
		ISchematic schematic = new Schematic(icon, width, height, length);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					int index = x + (y * length + z) * width;
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

		ListTag blockEntitiesList = tagCompound.getList(Names.NBT.BLOCK_ENTITIES, Constants.NBT.TAG_COMPOUND);

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

		ListTag entitiesList = tagCompound.getList(Names.NBT.ENTITIES, Constants.NBT.TAG_COMPOUND);

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
	public void writeToNBT(CompoundTag tagCompoundIn, @NotNull ISchematic schematic) {
		CompoundTag tagCompound = new CompoundTag();
		CompoundTag tagCompoundIcon = new CompoundTag();
		ItemStack icon = schematic.getIcon();
		icon.save(Reference.proxy.getRegistryAccess(), tagCompoundIcon);
		tagCompound.put(Names.NBT.ICON, tagCompoundIcon);

		tagCompound.putInt(Names.NBT.WIDTH, schematic.getSizeX());
		tagCompound.putInt(Names.NBT.LENGTH, schematic.getSizeZ());
		tagCompound.putInt(Names.NBT.HEIGHT, schematic.getHeight());

		int size = schematic.getSizeX() * schematic.getSizeZ() * schematic.getHeight();
		BlockState[] localBlocks = new BlockState[size];

		MBlockPos pos = new MBlockPos();
		Map<BlockState, BlockState> mappings = new HashMap<>();
		for (int x = 0; x < schematic.getSizeX(); x++) {
			for (int y = 0; y < schematic.getHeight(); y++) {
				for (int z = 0; z < schematic.getSizeZ(); z++) {
					int index = x + (y * schematic.getSizeZ() + z) * schematic.getSizeX();
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
				int index = bePos.getX() + (bePos.getY() * schematic.getSizeZ() + bePos.getZ()) * schematic.getSizeX();
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

		tagCompound.putString(Names.NBT.FORMAT, Names.NBT.FORMAT_ALPHA);
		tagCompound.putIntArray(Names.NBT.BLOCKS, Arrays.stream(localBlocks).mapToInt(Block::getId).toArray());
		tagCompound.put(Names.NBT.ENTITIES, entityList);
		tagCompound.put(Names.NBT.BLOCK_ENTITIES, blockEntities);
		tagCompound.put(Names.NBT.MAPPING_SCHEMATICA, nbtMapping);
		tagCompoundIn.put("root", tagCompound);
	}

	@Override
	public String getName() {
		return Names.Formats.ALPHA;
	}

	@Override
	public String getNbtName() {
		return Names.NBT.FORMAT_ALPHA;
	}

	@Override
	public String getExtension() {
		return Names.Extensions.SCHEMATIC;
	}
}