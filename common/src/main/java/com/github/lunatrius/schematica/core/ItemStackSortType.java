package com.github.lunatrius.schematica.core;

import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public enum ItemStackSortType {
	SIZE_DESC("amount", Glyph.DESC,
			(ItemStack itemStackA, ItemStack itemStackB) ->
					Integer.compare(itemStackB.getCount(), itemStackA.getCount())),
	SIZE_ASC("amount", Glyph.ASC,
			Comparator.comparingInt(ItemStack::getCount)),
	NAME_ASC("name", Glyph.ASC,
			(ItemStack itemStackA, ItemStack itemStackB) -> {
				String nameA = itemStackA.getDisplayName().getString();
				String nameB = itemStackB.getDisplayName().getString();

				return nameA.compareTo(nameB);
			}),
	NAME_DESC("name", Glyph.DESC,
			(ItemStack itemStackA, ItemStack itemStackB) -> {
				String nameA = itemStackA.getDisplayName().getString();
				String nameB = itemStackB.getDisplayName().getString();
				return nameB.compareTo(nameA);
			});


	public final String label;
	public final Glyph glyph;
	private final Comparator<ItemStack> comparator;

	ItemStackSortType(String label, Glyph glyph, Comparator<ItemStack> comparator) {
		this.label = label;
		this.glyph = glyph;
		this.comparator = comparator;
	}

	public void sort(List<ItemStack> blockList) {
		try {
			blockList.sort(this.comparator);
		} catch (Exception e) {
			Reference.logger.error("Could not sort the block list!", e);
		}
	}

	public <T> void sort(List<T> list, Function<T, ItemStack> itemRetriever) {
		try {
			list.sort(Comparator.comparing(itemRetriever, this.comparator));
		} catch (Exception e) {
			Reference.logger.error("Could not sort the block list!", e);
		}
	}

	@Contract(pure = true)
	public @NotNull String toString() {
		return String.format("%s %s", label, glyph.glyph);
	}
}