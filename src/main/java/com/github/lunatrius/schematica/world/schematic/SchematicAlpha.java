package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.event.PreSchematicSaveEvent;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class SchematicAlpha extends SchematicFormat {
	@Override
	public ISchematic readFromNBT(CompoundNBT tagCompound) {
		ItemStack icon = SchematicUtil.getIconFromNBT(tagCompound);

		List<BlockState> localBlockList = Arrays.stream(tagCompound.getIntArray(Names.NBT.BLOCKS))
		                                        .mapToObj(Block::getStateById)
		                                        .collect(Collectors.toList());
		BlockState[] localBlocks = localBlockList.toArray(new BlockState[] {});

		int width = tagCompound.getInt(Names.NBT.WIDTH);
		int length = tagCompound.getInt(Names.NBT.LENGTH);
		int height = tagCompound.getInt(Names.NBT.HEIGHT);

		Block id;
		Map<ResourceLocation, Block> oldToNew = new HashMap<>();
		if (tagCompound.hasUniqueId(Names.NBT.MAPPING_SCHEMATICA)) {
			CompoundNBT mapping = tagCompound.getCompound(Names.NBT.MAPPING_SCHEMATICA);
			Set<String> names = mapping.keySet();
			for (String name : names) {
				oldToNew.put(ResourceLocation.tryCreate(mapping.getString(name)),
				             ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name)));
			}
		}

		MBlockPos pos = new MBlockPos();
		ISchematic schematic = new Schematic(icon, width, height, length);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					int index = x + (y * length + z) * width;
					BlockState blockstate = localBlocks[index];

					if ((id = oldToNew.get(blockstate.getBlock().getRegistryName())) != null) {
						blockstate = id.getDefaultState();
					}

					pos.set(x, y, z);
					try {
						schematic.setBlockState(pos, blockstate);
					} catch (Exception e) {
						Reference.logger.error("Could not set block state at {} to {} with blockstate {}", pos,
						                       blockstate.getBlock().getRegistryName(), blockstate, e);
					}
				}
			}
		}

		ListNBT tileEntitiesList = tagCompound.getList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < tileEntitiesList.size(); i++) {
			try {
				TileEntity tileEntity = NBTHelper.readTileEntityFromCompound(tileEntitiesList.getCompound(i));
				if (tileEntity != null) {
					schematic.setTileEntity(tileEntity.getPos(), tileEntity);
				}
			} catch (Exception e) {
				Reference.logger.error("TileEntity failed to load properly!", e);
			}
		}

		return schematic;
	}

	@Override
	public boolean writeToNBT(CompoundNBT tagCompound, ISchematic schematic) {
		CompoundNBT tagCompoundIcon = new CompoundNBT();
		ItemStack icon = schematic.getIcon();
		icon.write(tagCompoundIcon);
		tagCompound.put(Names.NBT.ICON, tagCompoundIcon);

		tagCompound.putInt(Names.NBT.WIDTH, schematic.getWidth());
		tagCompound.putInt(Names.NBT.LENGTH, schematic.getLength());
		tagCompound.putInt(Names.NBT.HEIGHT, schematic.getHeight());

		int size = schematic.getWidth() * schematic.getLength() * schematic.getHeight();
		BlockState[] localBlocks = new BlockState[size];

		MBlockPos pos = new MBlockPos();
		Map<String, Block> mappings = new HashMap<>();
		for (int x = 0; x < schematic.getWidth(); x++) {
			for (int y = 0; y < schematic.getHeight(); y++) {
				for (int z = 0; z < schematic.getLength(); z++) {
					int index = x + (y * schematic.getLength() + z) * schematic.getWidth();
					BlockState blockState = schematic.getBlockState(pos.set(x, y, z));
					localBlocks[index] = blockState;
					String name = String.valueOf(blockState.getBlock().getRegistryName());
					if (!mappings.containsKey(name)) {
						mappings.put(name, blockState.getBlock());
					}
				}
			}
		}

		int count = 20;
		ListNBT tileEntitiesList = new ListNBT();
		for (TileEntity tileEntity : schematic.getTileEntities()) {
			try {
				CompoundNBT tileEntityTagCompound = NBTHelper.writeTileEntityToCompound(tileEntity);
				tileEntitiesList.add(tileEntityTagCompound);
			} catch (Exception e) {
				BlockPos tePos = tileEntity.getPos();
				int index =
						tePos.getX() + (tePos.getY() * schematic.getLength() + tePos.getZ()) * schematic.getWidth();
				if (--count > 0) {
					BlockState blockState = schematic.getBlockState(tePos);
					Block block = blockState.getBlock();
					Reference.logger.error("Block {}[{}] with TileEntity {} failed to save! Replacing with bedrock...",
					                       block, ForgeRegistries.BLOCKS.getKey(block),
					                       tileEntity.getClass().getName(),
					                       e);
				}

				localBlocks[index] = Blocks.BEDROCK.getDefaultState();
			}
		}

		ListNBT entityList = new ListNBT();
		List<Entity> entities = schematic.getEntities();
		for (Entity entity : entities) {
			try {
				CompoundNBT entityCompound = NBTHelper.writeEntityToCompound(entity);
				if (entityCompound != null) {
					entityList.add(entityCompound);
				}
			} catch (Throwable t) {
				Reference.logger.error("Entity {} failed to save, skipping!", entity, t);
			}
		}

		PreSchematicSaveEvent event = new PreSchematicSaveEvent(schematic, mappings);
		MinecraftForge.EVENT_BUS.post(event);

		CompoundNBT nbtMapping = new CompoundNBT();
		for (Map.Entry<String, Block> entry : mappings.entrySet()) {
			nbtMapping.putString(entry.getKey(),
			                     Objects.requireNonNull(entry.getValue().getRegistryName()).toString());
		}

		tagCompound.putString(Names.NBT.MATERIALS, Names.NBT.FORMAT_ALPHA);
		tagCompound.putIntArray(Names.NBT.BLOCKS, Arrays.stream(localBlocks).mapToInt(Block::getStateId).toArray());
		tagCompound.put(Names.NBT.ENTITIES, entityList);
		tagCompound.put(Names.NBT.TILE_ENTITIES, tileEntitiesList);
		tagCompound.put(Names.NBT.MAPPING_SCHEMATICA, nbtMapping);

		return true;
	}

	@Override
	public String getName() {
		return Names.Formats.ALPHA;
	}

	@Override
	public String getExtension() {
		return Names.Extensions.SCHEMATIC;
	}
}
