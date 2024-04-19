package com.simibubi.mightyarchitect.control.design;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.partials.Design.DesignInstance;
import com.simibubi.mightyarchitect.control.palette.BlockOrientation;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.core.BlockPos;

public class Sketch {

	public List<DesignInstance> primary;
	public List<DesignInstance> secondary;
	public List<Room> interior;

	public Sketch() {
		primary = new LinkedList<>();
		secondary = new LinkedList<>();
		interior = new LinkedList<>();
	}

	public AssembledSketch assemble() {
		AssembledSketch assembled = new AssembledSketch(interior);

		for (DesignInstance design : secondary)
			design.getBlocks(assembled.secondaryBlocks);
		for (DesignInstance design : primary)
			design.getBlocks(assembled.primaryBlocks);

		assembled.clean();
		addFloors(assembled);
		return assembled;
	}

	private void addFloors(AssembledSketch assembled) {
		for (Room cuboid : interior) {

			boolean trimAbove = false;
			for (Room trim : interior) {
				if (trimAbove)
					continue;
				if (trim.height > 1)
					continue;
				if (trim.y != cuboid.y + cuboid.height)
					continue;
				if (trim.x <= cuboid.x && trim.z <= cuboid.z && trim.x + trim.width >= cuboid.x + cuboid.width
					&& trim.z + trim.length >= cuboid.z + cuboid.length)
					trimAbove = true;
			}
			if (trimAbove)
				continue;

			List<Room> checked = new LinkedList<>();

			interior.forEach(other -> {
				if (other == cuboid)
					return;
				if (!other.intersects(cuboid))
					return;
				if (other.width * other.length > cuboid.width * cuboid.length)
					return;
				checked.add(other);
			});

			int y = cuboid.height - 1;
			PaletteBlockInfo paletteBlockInfo = new PaletteBlockInfo(Palette.INTERIOR_FLOOR);
			paletteBlockInfo.placedOrientation = BlockOrientation.TOP_UP;
			Map<BlockPos, PaletteBlockInfo> blocks =
				cuboid.secondaryPalette ? assembled.secondaryBlocks : assembled.primaryBlocks;

			for (int x = 0; x < cuboid.width; x++) {
				for (int z = 0; z < cuboid.length; z++) {
					boolean contained = false;
					BlockPos pos = cuboid.getOrigin()
						.offset(x, y, z);

					for (Room other : checked) {
						if (other.contains(pos)) {
							contained = true;
							break;
						}
					}

					if (contained) {
						continue;
					}

					paletteBlockInfo.pos = pos;
					blocks.put(pos, paletteBlockInfo);
				}
			}

		}
	}

}
