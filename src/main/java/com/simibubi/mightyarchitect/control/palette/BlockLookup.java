package com.simibubi.mightyarchitect.control.palette;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockLookup {

	public static BlockState find(BlockState baseState, PaletteBlockShape shape) {
		return switch (shape) {
		case SLAB -> BlockLookup.findSlab(baseState);
		case STAIRS -> BlockLookup.findStairs(baseState);
		case WALL -> BlockLookup.findWall(baseState);
		default -> baseState;
		};
	}
	
	@Nullable
	public static BlockState findSlab(BlockState baseState) {
		if (baseState.hasProperty(SlabBlock.TYPE))
			return baseState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
		return findSuffix(baseState, "_slab");
	}
	
	@Nullable
	public static BlockState findStairs(BlockState baseState) {
		if (baseState.getBlock() instanceof StairBlock)
			return baseState;
		return findSuffix(baseState, "_stairs");
	}
	
	@Nullable
	public static BlockState findWall(BlockState baseState) {
		if (baseState.getBlock() instanceof WallBlock)
			return baseState;
		if (baseState.getBlock() instanceof FenceBlock)
			return baseState;
		return findSuffix(baseState, "_wall");
	}
	
	@Nullable
	private static BlockState findSuffix(BlockState baseState, String... suffixes) {
		Block block = baseState.getBlock();
		if (block == null)
			return null;
		
		ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
		String namespace = rl.getNamespace();
		String blockName = rl.getPath();
		int nameLength = blockName.length();
		
		List<String> possibleStairLocations = new ArrayList<>();
		
		for (String suffix : suffixes) {
			possibleStairLocations.add(blockName + suffix);
			if (blockName.endsWith("s") && nameLength > 1)
				possibleStairLocations.add(blockName.substring(0, nameLength - 1) + suffix);
			if (blockName.endsWith("planks") && nameLength > 7)
				possibleStairLocations.add(blockName.substring(0, nameLength - 7) + suffix);
		}
		
		for (String locationAttempt : possibleStairLocations) {
			Optional<Block> result = ForgeRegistries.BLOCKS.getHolder(new ResourceLocation(namespace, locationAttempt))
				.map(slabHolder -> slabHolder.value());
			if (result.isEmpty())
				continue;
			return result.get()
				.defaultBlockState();
		}
		
		return null;
	}
	
}
