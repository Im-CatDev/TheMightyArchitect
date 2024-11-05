package com.simibubi.mightyarchitect.control.weathering;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CornerErosionWeatherer implements StepwiseWeathering {

	@Override
	public void step(TemplateBlockAccess level) {
		List<BlockPos> toCheck = new ArrayList<>();
		toCheck.addAll(level.blocks.keySet());
		for (BlockPos blockPos : toCheck) {
			int minimumNeighbours = minimumNeighbours(get(level, blockPos));
			if (minimumNeighbours == 0)
				continue;
			int neighbours = 0;
			for (Direction side : Iterate.directions)
				if (!get(level, blockPos.relative(side)).isAir())
					neighbours++;
			if (neighbours < minimumNeighbours)
				applyGravity(level, blockPos, random().nextBoolean());
		}
	}

	protected int minimumNeighbours(BlockState state) {
		if (state.getBlock() instanceof IronBarsBlock)
			return 3;
		if (state.getBlock() instanceof FenceBlock)
			return 0;
		if (state.getBlock() instanceof TrapDoorBlock)
			return 0;
		return random().nextInt(3);
	}

	@Override
	public boolean isDone() {
		return false;
	}

}
