package com.simibubi.mightyarchitect.control.weathering;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FloatingBlockCleanup implements StepwiseWeathering {

	@Override
	public void step(TemplateBlockAccess level) {
		List<BlockPos> frontier = new LinkedList<>();
		Set<BlockPos> uncheckedBlocks = new HashSet<>();
		uncheckedBlocks.addAll(level.blocks.keySet());
		Level realLevel = Minecraft.getInstance().level;

		for (Iterator<BlockPos> iterator = uncheckedBlocks.iterator(); iterator.hasNext();) {
			BlockPos pos = iterator.next();
			for (int i : Iterate.zeroAndOne) {
				BlockState realBlockStateBelow = realLevel.getBlockState(pos.below(i)
					.offset(ArchitectManager.getModel()
						.getAnchor()));
				if (realBlockStateBelow.canBeReplaced())
					continue;
				iterator.remove();
				frontier.add(pos);
				break;
			}
		}

		while (!frontier.isEmpty()) {
			BlockPos pos = frontier.remove(0);
			for (Direction side : Iterate.directions) {
				BlockPos sidePos = pos.relative(side);
				if (uncheckedBlocks.remove(sidePos))
					frontier.add(sidePos);
			}
		}

		if (uncheckedBlocks.isEmpty())
			return;

		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (BlockPos pos : uncheckedBlocks) {
			minY = Math.min(pos.getY(), minY);
			maxY = Math.max(pos.getY(), maxY);
		}

		for (int y = minY; y <= maxY; y++) {
			for (Iterator<BlockPos> iterator = uncheckedBlocks.iterator(); iterator.hasNext();) {
				BlockPos pos = iterator.next();
				if (pos.getY() != y)
					continue;
				applyGravity(level, pos, random().nextInt(10) != 0);
				iterator.remove();
			}
		}

	}

	@Override
	public boolean isDone() {
		return false;
	}

}
