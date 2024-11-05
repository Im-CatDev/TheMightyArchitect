package com.simibubi.mightyarchitect.control.weathering;

import java.util.List;

import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.control.palette.BlockLookup;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockShape;
import com.simibubi.mightyarchitect.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public class BlockShapeWeatherer implements StepwiseWeathering {

	boolean isDone = false;

	@Override
	public void step(TemplateBlockAccess level) {
		for (int iterations = 0; iterations < 5; iterations++) {
			for (int attempt = 0; attempt < 20; attempt++) {
				BlockPos pos = randomPosWithBlock(level);
				if (!apply(level, pos))
					continue;
				for (Direction d : Iterate.directions)
					if (random().nextBoolean())
						apply(level, pos.relative(d));
				return;
			}
		}
	}

	protected boolean apply(TemplateBlockAccess level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		if (!Block.isShapeFullBlock(blockState.getShape(level, pos))) {
			level.removeBlock(pos, false);
			return true;
		}

		PaletteBlockShape shape = List.of(PaletteBlockShape.STAIRS, PaletteBlockShape.SLAB, PaletteBlockShape.WALL)
			.get(random().nextInt(3));
		BlockState newState = BlockLookup.find(blockState, shape);
		if (newState == null || newState == blockState)
			return false;

		if (newState.hasProperty(StairBlock.HALF))
			if (random().nextBoolean())
				newState.cycle(StairBlock.HALF);
		if (newState.hasProperty(SlabBlock.TYPE))
			if (random().nextBoolean())
				newState.setValue(SlabBlock.TYPE, SlabType.TOP);
		if (newState.hasProperty(StairBlock.FACING)) {
			int limit = random().nextInt(4);
			for (int i = 0; i < limit; i++)
				newState.cycle(StairBlock.FACING);
		}
		level.setBlock(pos, newState, 0);
		return true;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

}
