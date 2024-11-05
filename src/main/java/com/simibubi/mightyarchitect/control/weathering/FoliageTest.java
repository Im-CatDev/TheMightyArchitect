package com.simibubi.mightyarchitect.control.weathering;

import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FoliageTest implements StepwiseWeathering {

	public FoliageTest() {}

	@Override
	public void step(TemplateBlockAccess level) {
		BlockPos pos = randomPosWithBlock(level);
		BlockState originState = get(level, pos);
		if (originState.is(BlockTags.LEAVES))
			return;
		for (Direction d : Iterate.directions) {
			BlockPos leafPos = pos.relative(d);
			BlockState blockState = get(level, leafPos);
			if (blockState.isAir() && !isInterior(leafPos))
				set(level, leafPos, Blocks.BIRCH_LEAVES.defaultBlockState());
		}
	}

	@Override
	public boolean isDone() {
		return false;
	}

}
