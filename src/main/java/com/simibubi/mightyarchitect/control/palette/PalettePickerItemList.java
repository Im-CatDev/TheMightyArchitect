package com.simibubi.mightyarchitect.control.palette;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PalettePickerItemList extends NonNullList<ItemStack> {

	private final PaletteEditScreen paletteEditScreen;
	Set<Integer> superHighlightedSlots = new HashSet<>();
	Set<Integer> highlightedSlots = new HashSet<>();

	protected PalettePickerItemList(PaletteEditScreen paletteEditScreen) {
		super(Lists.newArrayList(), null);
		this.paletteEditScreen = paletteEditScreen;
	}

	@Override
	public void clear() {
		super.clear();
		highlightedSlots.clear();
		superHighlightedSlots.clear();
	}

	@Override
	public void add(int pIndex, ItemStack pValue) {
		BlockState validBlockStateFor = getValidBlockStateFor(pValue);
		if (validBlockStateFor == null)
			return;

		super.add(pIndex, pValue);

		EnumSet<PaletteBlockShape> shapes = this.paletteEditScreen.selectedEntry.getShapes();
		if (shapes.size() == 1)
			return;

		boolean isSuperGood = true;
		boolean isSlightlyGood = false;
		boolean currentFound = false;
		int effectiveSize = 0;

		PaletteBlockShape currentShape = this.paletteEditScreen.getCurrentlyRequiredShape();
		for (PaletteBlockShape shape : shapes) {
			if (shape == currentShape) {
				currentFound = true;
				continue;
			}

			if (!currentFound)
				continue;

			effectiveSize++;

			if (BlockLookup.find(validBlockStateFor, shape) != null) {
				isSlightlyGood = true;
				continue;
			}
			isSuperGood = false;
		}

		if (effectiveSize == 0)
			return;
		if (isSuperGood)
			superHighlightedSlots.add(pIndex);
		else if (isSlightlyGood)
			highlightedSlots.add(pIndex);
	}

	public BlockState getValidBlockStateFor(ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem bi))
			return null;
		Block block = bi.getBlock();
		if (block == null || block instanceof EntityBlock)
			return null;
		BlockState blockState = block.defaultBlockState();
		if (blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))
			return null;
		if (blockState.isAir() || blockState.canBeReplaced())
			return null;
		if (!blockState.blocksMotion())
			return null;

		PaletteBlockShape shape = paletteEditScreen.getCurrentlyRequiredShape();
		if (BlockLookup.find(blockState, shape) == null)
			return null;

		if (paletteEditScreen.selectedEntry == Palette.LIGHT_POST
			|| paletteEditScreen.selectedEntry == Palette.HEAVY_POST)
			if (!(blockState.getBlock() instanceof FenceBlock) && !(blockState.getBlock() instanceof WallBlock))
				return null;

		if (paletteEditScreen.selectedEntry == Palette.PANEL)
			if (!(blockState.getBlock() instanceof TrapDoorBlock))
				return null;
			else
				return blockState.setValue(TrapDoorBlock.OPEN, true)
					.setValue(TrapDoorBlock.FACING, Direction.SOUTH);

		if (paletteEditScreen.selectedEntry.getShapes()
			.size() > 1 && shape == PaletteBlockShape.REGULAR) {
			try {
				if (!Block.isShapeFullBlock(blockState.getShape(Minecraft.getInstance().level, BlockPos.ZERO)))
					return null;
			} catch (Exception e) {
			}
		}

		return blockState;
	}

}