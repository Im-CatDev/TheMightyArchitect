package com.simibubi.mightyarchitect.control.weathering;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.simibubi.mightyarchitect.control.TemplateBlockAccess;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class TreeFeatureFoliage implements StepwiseWeathering {

	@Override
	public void step(TemplateBlockAccess level) {
		if (random().nextInt(10) != 0)
			return;
		level.treeMode(true);

		try {
			BlockPos pos = randomExteriorAirNearBottom(level);
			List<ConfiguredFeature<?, ?>> validFeatures = getValidTrees(level, pos);
			int index = validFeatures.size() > 1 ? random().nextInt(validFeatures.size()) : 0;
			if (!validFeatures.isEmpty())
				validFeatures.get(index)
					.place(level, new ChunkGeneratorExtension(null), random(),
						pos);

		} catch (Exception e) {
			e.printStackTrace();
		}

		level.treeMode(false);
	}

	@Override
	public boolean isDone() {
		return false;
	}

	private final class ChunkGeneratorExtension extends ChunkGenerator {

		public ChunkGeneratorExtension(BiomeSource pBiomeSource) {
			super(pBiomeSource);
		}

		@Override
		public void spawnOriginalMobs(WorldGenRegion pLevel) {}

		@Override
		public int getSeaLevel() {
			return 0;
		}

		@Override
		public int getMinY() {
			return 0;
		}

		@Override
		public int getGenDepth() {
			return 0;
		}

		@Override
		public int getBaseHeight(int pX, int pZ, Types pType, LevelHeightAccessor pLevel, RandomState pRandom) {
			return 0;
		}

		@Override
		public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor pHeight, RandomState pRandom) {
			return null;
		}

		@Override
		public CompletableFuture<ChunkAccess> fillFromNoise(Executor pExecutor, Blender pBlender, RandomState pRandom,
			StructureManager pStructureManager, ChunkAccess pChunk) {
			return null;
		}

		@Override
		protected Codec<? extends ChunkGenerator> codec() {
			return null;
		}

		@Override
		public void buildSurface(WorldGenRegion pLevel, StructureManager pStructureManager, RandomState pRandom,
			ChunkAccess pChunk) {}

		@Override
		public void applyCarvers(WorldGenRegion pLevel, long pSeed, RandomState pRandom, BiomeManager pBiomeManager,
			StructureManager pStructureManager, ChunkAccess pChunk, Carving pStep) {}

		@Override
		public void addDebugScreenInfo(List<String> pInfo, RandomState pRandom, BlockPos pPos) {}
	}

}
