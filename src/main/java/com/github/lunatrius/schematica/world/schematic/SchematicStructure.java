package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.template.Template;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SchematicStructure extends SchematicFormat {
	@Override
	public ISchematic readFromNBT(CompoundNBT tagCompound) {
		ItemStack icon = SchematicUtil.getIconFromNBT(tagCompound);

		Template template = new Template();
		template.read(tagCompound);

		Schematic schematic =
				new Schematic(icon, template.getSize().getX(), template.getSize().getY(), template.getSize().getZ(),
				              template.getAuthor());

		for (List<Template.BlockInfo> blockList : template.blocks) {
			for (Template.BlockInfo block : blockList) {
				schematic.setBlockState(block.pos, block.state.getBlockState());
				if (block.nbt != null) {
					try {
						// This position isn't included by default
						block.nbt.putInt("x", block.pos.getX());
						block.nbt.putInt("y", block.pos.getY());
						block.nbt.putInt("z", block.pos.getZ());

						TileEntity tileEntity = NBTHelper.readTileEntityFromCompound(block.nbt);
						if (tileEntity != null) {
							schematic.setTileEntity(block.pos, tileEntity);
						}
					} catch (Exception e) {
						Reference.logger.error("TileEntity failed to load properly!", e);
					}
				}
			}
		}

		// for (Template.EntityInfo entity : template.entities) {
		//     schematic.addEntity(...);
		// }

		return schematic;
	}


	@Override
	public void writeToNBT(CompoundNBT tagCompound, ISchematic schematic) {
		Template template = new Template();
		template.size = new BlockPos(schematic.getWidth(), schematic.getHeight(), schematic.getLength());

		template.setAuthor(schematic.getAuthor());

		List<Template.BlockInfo> blockInfos = new LinkedList<>();
		// NOTE: Can't use MutableBlockPos here because we're keeping a reference to it in BlockInfo
		for (BlockPos pos : BlockPos.getAllInBox(BlockPos.ZERO, template.size.add(-1, -1, -1))
		                            .collect(Collectors.toList())) {
			TileEntity tileEntity = schematic.getTileEntity(pos);
			CompoundNBT compound;
			if (tileEntity != null) {
				compound = NBTHelper.writeTileEntityToCompound(tileEntity);
				// Tile entities in structures don't store these coords
				compound.remove("x");
				compound.remove("y");
				compound.remove("z");
			} else {
				compound = null;
			}

			blockInfos.add(new Template.BlockInfo(pos, schematic.getBlockState(pos), compound));
		}

		template.blocks.add(blockInfos);

		for (Entity entity : schematic.getEntities()) {
			try {
				// Entity positions are already offset via NBTHelper.reloadEntity
				Vec3d vec3d = new Vec3d(entity.getPosX(), entity.getPosY(), entity.getPosZ());
				CompoundNBT CompoundNBT = new CompoundNBT();
				entity.writeUnlessPassenger(CompoundNBT);
				BlockPos blockpos;

				// TODO: Vanilla has a check like this, but we don't; this doesn't seem to
				// cause any problems though.
				// if (entity instanceof EntityPainting) {
				//     blockpos = ((EntityPainting)entity).getHangingPosition().subtract(startPos);
				// } else {
				blockpos = new BlockPos(vec3d);
				// }

				template.entities.add(new Template.EntityInfo(vec3d, blockpos, CompoundNBT));
			} catch (Throwable t) {
				Reference.logger.error("Entity {} failed to save, skipping!", entity, t);
			}
		}

		template.writeToNBT(tagCompound);
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
