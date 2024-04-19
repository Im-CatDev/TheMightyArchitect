package com.simibubi.mightyarchitect.control.palette;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PaletteBlockInfo {

	public Palette palette;
	public PaletteBlockShape blockShape;
	public BlockOrientation templateOrientation;
	public BlockOrientation placedOrientation;

	public boolean mirrorX;
	public boolean mirrorZ;
	public boolean forceAxis;
	
	public BlockPos pos;

	public PaletteBlockInfo(Palette palette) {
		this(palette, PaletteBlockShape.REGULAR, BlockOrientation.NONE);
	}

	public PaletteBlockInfo(Palette palette, PaletteBlockShape blockShape, BlockOrientation orientation) {
		this.palette = palette;
		this.blockShape = blockShape;
		this.templateOrientation = orientation;
	}

	public BlockState applyOrientations(BlockState state) {
		return placedOrientation.apply(templateOrientation.apply(state, forceAxis), false);
	}

	public boolean preferredOver(PaletteBlockInfo other) {
		int positiveIfPreferred = blockShape.comparePriority(other.blockShape);
		return positiveIfPreferred > 0 || positiveIfPreferred == 0 && palette.comparePriority(other.palette) > 0;
	}

}