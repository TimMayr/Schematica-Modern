package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.FakeLevel;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
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
	public ISchematic readFromNBT(CompoundTag tagCompound, Level level) {
		ItemStack icon = SchematicUtil.getIconFromNBT(tagCompound);
		StructureTemplate template = new StructureTemplate();
		template.load(BuiltInRegistries.BLOCK, tagCompound);

		Schematic schematic =
				new Schematic(icon, template.getSize().getX(), template.getSize().getY(), template.getSize().getZ(),
						template.getAuthor());

		for (StructureTemplate.Palette palette : template.palettes) {
			for (StructureTemplate.StructureBlockInfo block : palette.blocks()) {
				schematic.setBlockState(block.pos(), block.state());
				try {
					// This position isn't included by default
					block.nbt().putInt("x", block.pos().getX());
					block.nbt().putInt("y", block.pos().getY());
					block.nbt().putInt("z", block.pos().getZ());

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
				new BlockPos(schematic.getSizeX(), schematic.getHeight(), schematic.getSizeZ()), true,
				null);

		template.setAuthor(schematic.getAuthor());

		CompoundTag writeTo = new CompoundTag();

		template.save(writeTo);
		writeTo.putString(Names.NBT.FORMAT, Names.NBT.FORMAT_STRUCTURE);

		tagCompoundIn.put("root", writeTo);
	}

	@Override
	public String getName() {
		return Names.Formats.STRUCTURE;
	}

	@Override
	public String getExtension() {
		return Names.Extensions.STRUCTURE;
	}
}