package com.simibubi.mightyarchitect.control.weathering;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public interface StepwiseWeathering {

	void step(TemplateBlockAccess level);

	boolean isDone();

	default RandomSource random() {
		return Minecraft.getInstance().level.random;
	}

	default BlockPos randomExteriorAirNearBottom(TemplateBlockAccess level) {
		for (int attempts = 0; attempts < 50; attempts++) {
			BlockPos wallPos = randomPosWithBlock(level, true);
			for (Direction side : Iterate.directions) {
				BlockPos relative = wallPos.relative(side);
				if (!get(level, relative).isAir())
					continue;
				if (level.getBounds().y > relative.getY())
					continue;
				if (isInterior(relative))
					continue;
				return relative;
			}
		}
		return null;
	}

	default BlockPos randomPosWithBlock(TemplateBlockAccess level) {
		return randomPosWithBlock(level, false);
	}

	default BlockPos randomPosWithBlock(TemplateBlockAccess level, boolean nearGround) {
		Set<BlockPos> keys = nearGround ? level.getPositionsNearGroundLevel() : level.blocks.keySet();
		if (keys.isEmpty())
			return null;
		int index = random().nextInt(keys.size());
		for (BlockPos pos : keys) {
			if (index == 0)
				return pos;
			index--;
		}
		return null;
	}

	default boolean isInterior(BlockPos pos) {
		return ArchitectManager.getModel()
			.getAssembledSketch()
			.isInterior(pos);
	}

	default void set(TemplateBlockAccess level, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof LeavesBlock)
			state = state.setValue(LeavesBlock.PERSISTENT, true);
		level.setBlock(pos, state, 0);
	}

	default BlockState get(TemplateBlockAccess level, BlockPos pos) {
		return level.getBlockState(pos);
	}

	default GroundPlan groundPlan() {
		return ArchitectManager.getModel()
			.getGroundPlan();
	}

	default void applyGravity(TemplateBlockAccess level, BlockPos pos, boolean canSlipOffSides) {
		Level realLevel = Minecraft.getInstance().level;
		BlockState blockState = get(level, pos);
		set(level, pos, Blocks.AIR.defaultBlockState());

		if (blockState.getBlock() instanceof IronBarsBlock && canSlipOffSides)
			return;
		if (blockState.getBlock() instanceof SlabBlock)
			return;
		if (blockState.getBlock() instanceof FenceBlock)
			return;
		if (blockState.getBlock() instanceof TrapDoorBlock)
			return;

		if (random().nextInt(3) == 0)
			return;

		MutableBlockPos dropPos = pos.mutable();

		Predicate<BlockPos> supportCheck = p -> {
			BlockState realBlockStateBelow = realLevel.getBlockState(p.below()
				.offset(ArchitectManager.getModel()
					.getAnchor()));
			BlockState blockStateBelow = get(level, p.below());
			return !blockStateBelow.isAir() || !realBlockStateBelow.canBeReplaced();
		};

		Drop: while (dropPos.getY() > level.getBounds().y - 20 && dropPos.getY() > realLevel.getMinBuildHeight()) {
			if (!supportCheck.test(dropPos)) {
				dropPos.move(Direction.DOWN);
				continue;
			}

			if (!canSlipOffSides) {
				set(level, dropPos.immutable(), blockState);
				break;
			}

			Direction[] horizontaldirections = Iterate.horizontalDirections;
			int indexOffset = random().nextInt(4);
			for (int i = 0; i < horizontaldirections.length; i++) {
				Direction side = horizontaldirections[(i + indexOffset) % horizontaldirections.length];
				if (!supportCheck.test(dropPos.relative(side)) && !supportCheck.test(dropPos.relative(side)
					.above())) {
					dropPos.move(side)
						.move(Direction.DOWN);
					continue Drop;
				}
			}

			set(level, dropPos.immutable(), blockState);
			break;
		}
	}

	default List<ConfiguredFeature<?, ?>> getValidTrees(TemplateBlockAccess level, BlockPos pos) {
		List<ConfiguredFeature<?, ?>> validFeatures = new ArrayList<>();
		Holder<Biome> biome = Minecraft.getInstance().level.getBiome(pos.offset(ArchitectManager.getModel()
			.getAnchor()));

		for (HolderSet<PlacedFeature> set : biome.get()
			.getGenerationSettings()
			.features())
			for (Holder<PlacedFeature> holder : set)
				for (ConfiguredFeature<?, ?> feature : holder.get()
					.getFeatures()
					.toList())
					if (feature.feature() instanceof TreeFeature)
						validFeatures.add(feature);
		return validFeatures;
	}

}
