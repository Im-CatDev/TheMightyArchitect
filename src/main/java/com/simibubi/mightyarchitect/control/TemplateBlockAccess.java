package com.simibubi.mightyarchitect.control;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.foundation.WrappedWorld;
import com.simibubi.mightyarchitect.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

public class TemplateBlockAccess extends WrappedWorld implements WorldGenLevel {

	public Map<BlockPos, BlockState> blocks;

	public Set<BlockPos> blocksNearGroundLevel;

	private Cuboid bounds;
	private BlockPos anchor;
	private boolean localMode;
	private boolean treeMode;

	public TemplateBlockAccess(Map<BlockPos, BlockState> blocks, Cuboid bounds, BlockPos anchor) {
		super(Minecraft.getInstance().level);
		this.blocks = blocks;
		this.bounds = bounds;
		this.anchor = anchor;
		updateBlockstates();
	}

	public void localMode(boolean local) {
		this.localMode = local;
	}

	public void treeMode(boolean tree) {
		this.treeMode = tree;
	}

	private void updateBlockstates() {
		Set<BlockPos> keySet = new HashSet<>(blocks.keySet());
		keySet.forEach(pos -> {
			BlockState blockState = blocks.get(pos);
			if (blockState == null)
				return;
			BlockPos targetPos = pos.offset(anchor);
			BlockState newState = blockState;
			for (Direction direction : Iterate.directions) {
				BlockPos relative = targetPos.relative(direction);
				newState = newState.updateShape(direction, getBlockState(relative), this, targetPos, relative);
			}
			if (newState != blockState)
				setBlock(targetPos, newState, 0);
		});
	}

	public Set<BlockPos> getAllPositions() {
		return blocks.keySet();
	}

	public Set<BlockPos> getPositionsNearGroundLevel() {
		if (blocksNearGroundLevel != null)
			return blocksNearGroundLevel;

		PriorityQueue<BlockPos> sorter = new PriorityQueue<>((p1, p2) -> Integer.compare(p1.getY(), p2.getY()));
		sorter.addAll(blocks.keySet());

		blocksNearGroundLevel = new HashSet<>();
		for (int i = 0; i < Math.max(2, blocks.size() / 20); i++)
			blocksNearGroundLevel.add(sorter.poll());
		return blocksNearGroundLevel;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		if (treeMode)
			return Blocks.AIR.defaultBlockState();
		BlockPos pos = (localMode ? globalPos : globalPos.subtract(anchor)).immutable();
		if (getBounds().contains(pos) && blocks.containsKey(pos)) {
			return blocks.get(pos);
		} else {
			return Blocks.AIR.defaultBlockState();
		}
	}

	public Map<BlockPos, BlockState> getBlockMap() {
		return blocks;
	}

	@Override
	public Holder<Biome> getBiome(BlockPos pos) {
		return Holder.direct(registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)
			.get(Biomes.THE_VOID));
	}

	@Override
	public int getMaxLocalRawBrightness(BlockPos p_201696_1_) {
		return 0xF;
	}

	@Override
	public List<Entity> getEntities(Entity arg0, AABB arg1, Predicate<? super Entity> arg2) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesOfClass(Class<T> arg0, AABB arg1, Predicate<? super T> arg2) {
		return Collections.emptyList();
	}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

	@Override
	public int getBrightness(LightLayer lt, BlockPos p_226658_2_) {
		return lt == LightLayer.BLOCK ? 12 : 14;
	}

	@Override
	public int getLightEmission(BlockPos pos) {
		return super.getLightEmission(pos);
	}

	@Override
	public BlockPos getHeightmapPos(Types heightmapType, BlockPos pos) {
		return BlockPos.ZERO;
	}

	@Override
	public int getHeight(Types heightmapType, int x, int z) {
		return 256;
	}

	@Override
	public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
		return predicate.test(getBlockState(pos));
	}

	@Override
	public boolean destroyBlock(BlockPos arg0, boolean arg1) {
		return setBlock(arg0, Blocks.AIR.defaultBlockState(), 3);
	}

	@Override
	public boolean removeBlock(BlockPos arg0, boolean arg1) {
		return setBlock(arg0, Blocks.AIR.defaultBlockState(), 3);
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState state, int p_241211_3_, int p_241211_4_) {
		if (treeMode && state.is(BlockTags.DIRT))
			return true;
		if (treeMode && state.is(BlockTags.LEAVES) && !getBlockState(pos).isAir())
			return true;
		BlockPos key = (localMode ? pos : pos.subtract(anchor)).immutable();
		if (state.isAir())
			blocks.remove(key);
		else {
			blocks.put(key, state);
			bounds.include(key);
		}
		return true;
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		return setBlock(pos, newState, flags, 0);
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return BlackholeTickAccess.emptyLevelList();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyLevelList();
	}

	@Override
	public void updateNeighborsAt(BlockPos p_195593_1_, Block p_195593_2_) {}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

	@Override
	public void playSound(Player player, BlockPos pos, SoundEvent soundIn, SoundSource category, float volume,
		float pitch) {}

	@Override
	public void addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed,
		double zSpeed) {}

	@Override
	public void levelEvent(Player player, int type, BlockPos pos, int data) {}

	public Cuboid getBounds() {
		return bounds;
	}

	@Override
	public long getSeed() {
		return 0;
	}

	@Override
	public ServerLevel getLevel() {
		return null;
	}

}
