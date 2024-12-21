package com.github.lunatrius.schematica.util;

import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.reference.Reference;

import java.util.Comparator;
import java.util.List;

public enum ItemStackSortType {
	NAME_ASC("name", "↑",
	         (BlockList.WrappedItemStack wrappedItemStackA, BlockList.WrappedItemStack wrappedItemStackB) -> {
		         String nameA = String.valueOf(wrappedItemStackA.getItemStackDisplayName());
		         String nameB = String.valueOf(wrappedItemStackB.getItemStackDisplayName());

		         return nameA.compareTo(nameB);
	         }),
	NAME_DESC("name", "↓",
	          (BlockList.WrappedItemStack wrappedItemStackA, BlockList.WrappedItemStack wrappedItemStackB) -> {
		          String nameA = String.valueOf(wrappedItemStackA.getItemStackDisplayName());
		          String nameB = String.valueOf(wrappedItemStackB.getItemStackDisplayName());

		          return nameB.compareTo(nameA);
	          }),
	SIZE_ASC("amount", "↑",
	         Comparator.comparingInt((BlockList.WrappedItemStack wrappedItemStackA) -> wrappedItemStackA.total)),
	SIZE_DESC("amount", "↓",
	          (BlockList.WrappedItemStack wrappedItemStackA, BlockList.WrappedItemStack wrappedItemStackB) ->
			          wrappedItemStackB.total
					          - wrappedItemStackA.total);

	public final String label;
	public final String glyph;
	private final Comparator<BlockList.WrappedItemStack> comparator;

	ItemStackSortType(String label, String glyph, Comparator<BlockList.WrappedItemStack> comparator) {
		this.label = label;
		this.glyph = glyph;
		this.comparator = comparator;
	}

	public void sort(List<BlockList.WrappedItemStack> blockList) {
		try {
			blockList.sort(this.comparator);
		} catch (Exception e) {
			Reference.logger.error("Could not sort the block list!", e);
		}
	}

	public ItemStackSortType next() {
		ItemStackSortType[] values = values();
		return values[(ordinal() + 1) % values.length];
	}
}