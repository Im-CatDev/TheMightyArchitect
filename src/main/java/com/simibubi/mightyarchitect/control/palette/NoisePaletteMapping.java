package com.simibubi.mightyarchitect.control.palette;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public class NoisePaletteMapping extends MultiPaletteMapping {

	protected List<Integer> weights;
	protected int totalWeight;

	public NoisePaletteMapping(List<PaletteMapping> mappings, List<Integer> weights) {
		super(mappings);
		this.weights = weights;
		totalWeight = weights.stream()
			.mapToInt(Integer::intValue)
			.sum();
	}

	public NoisePaletteMapping(CompoundTag compound) {
		super(compound);
		ListTag listTag = compound.getList("Weights", Tag.TAG_COMPOUND);
		weights = new ArrayList<>();
		for (Tag tag : listTag)
			if (tag instanceof CompoundTag cTag)
				weights.add(cTag.getInt("Value"));
		totalWeight = weights.stream()
			.mapToInt(Integer::intValue)
			.sum();
	}

	@Override
	public void write(CompoundTag compound) {
		super.write(compound);
		compound.putString("Type", "Noise");
		ListTag listTag = new ListTag();
		for (Integer integer : weights) {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.putInt("Value", integer);
			listTag.add(compoundTag);
		}
		compound.put("Weights", listTag);
	}

	@Override
	protected int chooseMapping(BlockPos pos, PositionalRandomFactory random) {
		int value = random.at(pos)
			.nextInt(totalWeight);
		int i = 0;
		for (; i < weights.size(); i++) {
			Integer weight = weights.get(i);
			value -= weight;
			if (value < 0)
				break;
		}
		return i;
	}

}
