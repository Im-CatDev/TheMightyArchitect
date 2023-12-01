package com.simibubi.mightyarchitect.control.palette;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public abstract class MultiPaletteMapping implements PaletteMapping {

	protected List<PaletteMapping> mappings;

	public MultiPaletteMapping(List<PaletteMapping> mappings) {
		this.mappings = mappings;
	}

	public MultiPaletteMapping(CompoundTag compound) {
		ListTag listTag = compound.getList("Mappings", Tag.TAG_COMPOUND);
		mappings = new ArrayList<>();
		for (Tag tag : listTag)
			if (tag instanceof CompoundTag cTag)
				mappings.add(PaletteMapping.fromNBT(cTag));
	}

	@Override
	public void write(CompoundTag compound) {
		ListTag listTag = new ListTag();
		for (PaletteMapping paletteMapping : mappings)
			listTag.add(paletteMapping.toNBT());
		compound.put("Mappings", listTag);
	}

	@Override
	public final BlockState provide(PaletteBlockShape shape) {
		return mappings.get(0)
			.provide(shape);
	}

	@Override
	public final BlockState provideWithContext(PaletteBlockShape shape, BlockPos pos, PositionalRandomFactory random) {
		return mappings.get(chooseMapping(pos, random))
			.provideWithContext(shape, pos, random);
	}

	protected abstract int chooseMapping(BlockPos pos, PositionalRandomFactory random);

}
