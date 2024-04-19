package com.simibubi.mightyarchitect.control.palette;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public class GradientPaletteMapping extends NoisePaletteMapping {

	public GradientPaletteMapping(List<PaletteMapping> mappings, List<Integer> heights) {
		super(mappings, heights);
	}

	public GradientPaletteMapping(CompoundTag compound) {
		super(compound);
	}

	@Override
	public void write(CompoundTag compound) {
		super.write(compound);
		compound.putString("Type", "Gradient");
	}

	@Override
	protected int chooseMapping(BlockPos pos, PositionalRandomFactory random) {
		int variance = Math.max(totalWeight / 3, 3);
		int randomValue = random.at(pos)
			.nextInt(variance) - variance;
		int value = Math.max(0, pos.getY() + randomValue);
		int i = 0;
		for (; i < weights.size(); i++) {
			Integer weight = weights.get(i);
			value -= weight;
			if (value < 0)
				break;
		}
		return Math.min(weights.size() - 1, i);
	}

}
