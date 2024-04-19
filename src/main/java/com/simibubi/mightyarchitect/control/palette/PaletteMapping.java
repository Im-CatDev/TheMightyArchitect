package com.simibubi.mightyarchitect.control.palette;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public interface PaletteMapping {

	public BlockState provide(PaletteBlockShape shape);
	
	public BlockState provideWithContext(PaletteBlockShape shape, BlockPos pos, PositionalRandomFactory random);
	
	public default PaletteBlockShape find(BlockState state) {
		for (PaletteBlockShape shape : PaletteBlockShape.values()) {
			BlockState provided = provide(shape);
			if (provided == null)
				continue;
			if (state.is(provided.getBlock()))
				return shape;
		}
		return null;
	}
	
	public static PaletteMapping fromNBT(CompoundTag compound) {
		String type = compound.getString("Type");
		if (type.equals("Simple"))
			return new SimplePaletteMapping(compound);
		if (type.equals("Noise"))
			return new NoisePaletteMapping(compound);
		if (type.equals("Gradient"))
			return new GradientPaletteMapping(compound);
		return null;
	}
	
	public default CompoundTag toNBT() {
		CompoundTag compound = new CompoundTag();
		write(compound);
		return compound;
	}
	
	public void write(CompoundTag compound);
	
}
