package com.simibubi.mightyarchitect.control.weathering;

import java.util.List;

import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class ClimbingFoliage implements StepwiseWeathering {

	static final int BRANCH_CHANCE = 10;
	static final int BRANCH_DEPTH = 3;

	int steps;
	int depth;
	BlockPos currentPos;
	BlockPos prevOffset;
	ClimbingFoliage offBranch;
	int totalBranches;
	int momentum;
	boolean dead;
	boolean initLeaves;
	BlockState leafBlock;

	public ClimbingFoliage() {
		leafBlock = Blocks.OAK_LEAVES.defaultBlockState();
		steps = 0;
		depth = 0;
		totalBranches = 0;
		momentum = 0;
		dead = false;
	}

	@Override
	public void step(TemplateBlockAccess level) {
		if (offBranch != null) {
			offBranch.step(level);
			if (offBranch.isDone())
				offBranch = null;
		}

		if (dead)
			return;

		if (currentPos == null) {
			findStartingLocation(level);
			return;
		}

		if (!initLeaves && currentPos != null) {
			initLeaves = true;
			List<ConfiguredFeature<?, ?>> validTrees = getValidTrees(level, currentPos);
			int offset = Math.abs(random().nextInt());
			for (int i = 0; i < validTrees.size(); i++) {
				ConfiguredFeature<?, ?> feature = validTrees.get((i + offset) % validTrees.size());
				if (feature.config() instanceof TreeConfiguration tc) {
					leafBlock = tc.foliageProvider.getState(random(), currentPos);
					break;
				}
			}
		}

		if (shouldStop()) {
			dead = true;
			return;
		}

		steps++;

		if (prevOffset != null && totalBranches < 4 && offBranch == null && random().nextInt(BRANCH_CHANCE) == 0
			&& depth < BRANCH_DEPTH) {
			offBranch = new ClimbingFoliage();
			offBranch.steps = steps;
			offBranch.depth = depth + 1;
			offBranch.currentPos = currentPos;
			momentum = random().nextInt(2) + 2;
			offBranch.momentum = random().nextInt(2) + 2;
			offBranch.prevOffset = new BlockPos(-prevOffset.getX(), prevOffset.getY(), -prevOffset.getZ());
			offBranch.initLeaves = true;
			totalBranches++;
		}

		if (momentum > 0) {
			boolean success = tryGrowTo(level, prevOffset);
			momentum--;
			if (success)
				return;
		}

		for (Direction verticalOffset : List.of(Direction.UP, Direction.DOWN)) {
			int randomOffset = random().nextInt(20);
			Direction[] horizontaldirections = Iterate.horizontalDirections;
			for (int i = 0; i < horizontaldirections.length; i++) {
				Direction simple = horizontaldirections[(i + randomOffset) % horizontaldirections.length];
				BlockPos target = currentPos.relative(verticalOffset)
					.relative(simple);
				if (tryGrowTo(level, target))
					return;
			}

			for (int i = 0; i < horizontaldirections.length; i++) {
				Direction notSimple = horizontaldirections[(i + randomOffset) % horizontaldirections.length];
				BlockPos target = currentPos.relative(verticalOffset)
					.relative(notSimple)
					.relative(notSimple.getClockWise());
				if (tryGrowTo(level, target))
					return;
			}

			// growing down is less likely to succeed
			if (random().nextInt(2) == 0)
				break;
		}

		dead = true;
	}

	private boolean shouldStop() {
		if (steps > 100)
			return true;
		if (steps > 50)
			return random().nextInt(2) == 0;
		if (steps > 15)
			return random().nextInt(6) == 0;
		return false;
	}

	private boolean tryGrowTo(TemplateBlockAccess level, BlockPos target) {
//		if (isInterior(target))
//			return false;
		if (level.getBounds().y > target.getY())
			return false;
		if (!canReplace(get(level, target)))
			return false;
		if (momentum == 0 && prevOffset != null && random().nextInt(4) != 0
			&& prevOffset.equals(target.subtract(currentPos)))
			return false;

		for (Direction side : Iterate.horizontalDirections) {
			if (!isValidSupport(get(level, target.relative(side))))
				continue;
			set(level, target, leaf());
			if (random().nextInt(6) > 0 && (canReplace(get(level, target.below()))
				|| random().nextBoolean() && get(level, target.below()).getBlock() instanceof LeavesBlock))
				set(level, target.below(), branch());
			prevOffset = target.subtract(currentPos);
			currentPos = target;
			return true;
		}

		return false;
	}

	private boolean isValidSupport(BlockState blockState) {
		if (blockState.isAir())
			return false;
		if (blockState.getBlock() instanceof TrapDoorBlock)
			return false;
		if (blockState.getBlock() instanceof LeavesBlock)
			return false;
		return true;
	}

	private boolean canReplace(BlockState blockState) {
		if (blockState.isAir())
			return true;
		if (blockState.getBlock() instanceof FenceBlock)
			return true;
		if (blockState.getBlock() instanceof TrapDoorBlock)
			return true;
		return false;
	}

	private void findStartingLocation(TemplateBlockAccess level) {
		BlockPos randomStart = randomExteriorAirNearBottom(level);
		if (randomStart == null)
			return;
		currentPos = randomStart;
		set(level, currentPos, leaf());
	}

	private BlockState branch() {
		return Blocks.DARK_OAK_FENCE.defaultBlockState();
	}

	private BlockState leaf() {
		return leafBlock;
	}

	@Override
	public boolean isDone() {
		return dead && (offBranch == null || offBranch.isDone());
	}

}
