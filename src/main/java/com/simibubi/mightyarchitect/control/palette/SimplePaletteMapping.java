package com.simibubi.mightyarchitect.control.palette;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.UnaryOperator;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public class SimplePaletteMapping implements PaletteMapping {

	private List<BlockState> shapeProviderFallbacks;

	private final EnumMap<PaletteBlockShape, BlockState> cache = new EnumMap<>(PaletteBlockShape.class);

	public SimplePaletteMapping(BlockState... blockAndFallbackSeries) {
		shapeProviderFallbacks = Lists.newArrayList(blockAndFallbackSeries);
	}

	public SimplePaletteMapping(CompoundTag compound) {
		ListTag listTag = compound.getList("Blocks", Tag.TAG_COMPOUND);
		shapeProviderFallbacks = new ArrayList<>();
		for (Tag tag : listTag)
			if (tag instanceof CompoundTag cTag)
				shapeProviderFallbacks.add(NbtUtils.readBlockState(cTag));
	}

	@Override
	public void write(CompoundTag compound) {
		compound.putString("Type", "Simple");
		ListTag listTag = new ListTag();
		for (BlockState state : shapeProviderFallbacks)
			listTag.add(NbtUtils.writeBlockState(state));
		compound.put("Blocks", listTag);
	}

	@Override
	public BlockState provideWithContext(PaletteBlockShape shape, BlockPos pos, PositionalRandomFactory random) {
		return provide(shape);
	}
	
	public void append(BlockState fallback) {
		shapeProviderFallbacks.add(fallback);
		cache.clear();
	}
	
	public boolean verify(PaletteBlockShape shape) {
		return provide(shape) != null;
	}

	@Override
	public BlockState provide(PaletteBlockShape shape) {
		if (shapeProviderFallbacks.isEmpty())
			return null;
		
		return switch (shape) {
		case SLAB -> tryWithFallbacksCached(shape, BlockLookup::findSlab);
		case STAIRS -> tryWithFallbacksCached(shape, BlockLookup::findStairs);
		case WALL -> tryWithFallbacksCached(shape, BlockLookup::findWall);
		default -> shapeProviderFallbacks.get(0);
		};
	}

	private BlockState tryWithFallbacksCached(PaletteBlockShape shape, UnaryOperator<BlockState> lookup) {
		return cache.computeIfAbsent(shape, $ -> tryWithFallbacks(lookup));
	}

	private BlockState tryWithFallbacks(UnaryOperator<BlockState> lookup) {
		for (BlockState fallBack : shapeProviderFallbacks) {
			BlockState result = lookup.apply(fallBack);
			if (result != null)
				return result;
		}
		return null;
	}

}
