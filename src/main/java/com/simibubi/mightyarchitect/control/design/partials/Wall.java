package com.simibubi.mightyarchitect.control.design.partials;

import java.util.Map;

import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class Wall extends Design {

	public enum ExpandBehaviour {
		MERGED("Merged"), ADJACENT("Adjacent"), NONE("None");

		private String displayName;

		private ExpandBehaviour(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public ExpandBehaviour expandBehaviour;

	@Override
	public Design fromNBT(CompoundTag compound) {
		Wall wall = new Wall();
		wall.applyNBT(compound);
		wall.expandBehaviour = ExpandBehaviour.valueOf(compound.getString("ExpandBehaviour"));
		return wall;
	}

	@Override
	public boolean fitsHorizontally(int width) {
		switch (expandBehaviour) {
		case MERGED:
			if (width == 1)
				return false;
			return (width % (this.defaultWidth - 1)) == 1;
		case ADJACENT:
			return (width % this.defaultWidth) == 0;
		default:
			return super.fitsHorizontally(width);
		}
	}

	@Override
	public void getBlocks(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks) {
		if (expandBehaviour == ExpandBehaviour.NONE) {
			super.getBlocks(instance, blocks);
			return;
		}
		boolean merge = expandBehaviour == ExpandBehaviour.MERGED;
		int instances = merge ? (instance.width - 1) / (defaultWidth - 1) : instance.width / defaultWidth;
		int multiplierWidth = (merge ? defaultWidth - 1 : defaultWidth);
		for (int i = 0; i < instances; i++) {
			BlockPos shift = new BlockPos(i * multiplierWidth, 0, 0);
			super.getBlocksShifted(instance, blocks, shift);
		}
	}

}
