package com.github.lunatrius.schematica.world.schematic.format;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.SchematicMetadata;
import com.github.lunatrius.schematica.core.NBTHelper;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

public class SchematicStructure extends SchematicFormat {
	@Override
	public String getNbtName() {
		return Names.NBT.FORMAT_STRUCTURE;
	}

	@Override
	public ISchematic readFromNbt(CompoundTag tagCompound, Level level) {
		SchematicMetadata metadata = readMetaFromNbt(tagCompound);
		StructureTemplate template = new StructureTemplate();
		template.load(BuiltInRegistries.BLOCK, tagCompound);

		Schematic schematic = new Schematic(metadata);

		for (StructureTemplate.Palette palette : template.palettes) {
			for (StructureTemplate.StructureBlockInfo block : palette.blocks()) {
				schematic.setBlockState(block.pos(), block.state());
				try {
					if (block.nbt() != null) {
						// This position isn't included by default
						block.nbt().putInt("x", block.pos().getX());
						block.nbt().putInt("y", block.pos().getY());
						block.nbt().putInt("z", block.pos().getZ());
					}

					BlockEntity blockEntity = NBTHelper.readBlockEntityFromCompound(block.nbt(), block.state());
					if (blockEntity != null) {
						schematic.setBlockEntity(block.pos(), blockEntity);
					}
				} catch (Exception e) {
					Reference.logger.error("BlockEntity failed to load properly!", e);
				}
			}
		}

		for (StructureTemplate.StructureEntityInfo entityInfo : template.entityInfoList) {
			try {
				entityInfo.nbt.putInt("x", entityInfo.blockPos.getX());
				entityInfo.nbt.putInt("y", entityInfo.blockPos.getY());
				entityInfo.nbt.putInt("z", entityInfo.blockPos.getZ());

				Entity entity = NBTHelper.readEntityFromCompound(entityInfo.nbt, level);
				schematic.addEntity(entity);
			} catch (Exception e) {
				Reference.logger.error("Entity failed to load properly!", e);
			}
		}

		return schematic;
	}

	@Override
	public void writeToNBT(@NotNull CompoundTag tagCompoundIn, ISchematic schematic) {
		StructureTemplate template = new StructureTemplate();

		template.fillFromWorld(FakeLevel.of(schematic), BlockPos.ZERO,
				new BlockPos(schematic.getWidth(), schematic.getHeight(), schematic.getLength()), true,
				null);

		template.setAuthor(schematic.getAuthor().toString());

		CompoundTag writeTo = new CompoundTag();

		template.save(writeTo);
		writeMetadataToNBT(writeTo, schematic.getMetadata());
		tagCompoundIn.put(Names.NBT.ROOT, writeTo);
	}

	@Override
	public String getName() {
		return Names.Formats.STRUCTURE;
	}

	@Override
	public String getExtension() {
		return Names.Extensions.STRUCTURE;
	}

	@Override
	public SchematicMetadata readMetaFromNbt(@NotNull CompoundTag tagCompound) {
		if (tagCompound.contains(Names.NBT.METADATA)) {
			CompoundTag tag = tagCompound.getCompound(Names.NBT.METADATA);
			return metaFromTag(tag);
		} else {
			return null;
		}
	}

	@Override
	public void writeMetadataToNBT(@NotNull CompoundTag tagCompound, @NotNull SchematicMetadata metadata) {
		tagCompound.put(Names.NBT.METADATA, metaAsTag(metadata));
	}

	public @NotNull SchematicMetadata metaFromTag(@NotNull CompoundTag tag) {
		return SchematicFormat.defaultMetaFromTag(tag);
	}

	public @NotNull CompoundTag metaAsTag(@NotNull SchematicMetadata metadata) {
		return SchematicFormat.defaultMetaAsTag(metadata);
	}
}