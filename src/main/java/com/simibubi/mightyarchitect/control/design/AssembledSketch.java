package com.simibubi.mightyarchitect.control.design;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.core.BlockPos;

public class AssembledSketch {

	public Map<BlockPos, PaletteBlockInfo> primaryBlocks;
	public Map<BlockPos, PaletteBlockInfo> secondaryBlocks;
	public List<Map<BlockPos, PaletteBlockInfo>> layers;

	public Set<BlockPos> interiorBlocks;
	public Set<Room> interiorRooms;

	public AssembledSketch(Collection<Room> rooms) {
		primaryBlocks = new HashMap<>();
		secondaryBlocks = new HashMap<>();
		interiorRooms = new HashSet<>();
		layers = List.of(primaryBlocks, secondaryBlocks);
		for (Room room : rooms)
			if (!room.designLayer.isExterior())
				interiorRooms.add(room);
	}

	public void clean() {
		Set<BlockPos> toRemove = new HashSet<>();
		interiorBlocks = new HashSet<>();

		for (Map<BlockPos, PaletteBlockInfo> paletteLayer : layers) {
			for (BlockPos pos : paletteLayer.keySet()) {
				for (Map<BlockPos, PaletteBlockInfo> map : layers)
					if (map.containsKey(pos) && map.get(pos).palette == Palette.CLEAR) {
						interiorBlocks.add(pos);
						toRemove.add(pos);
					}
				for (Room room : interiorRooms)
					if (room.contains(pos))
						toRemove.add(pos);
			}
		}

		toRemove.forEach(e -> layers.forEach(m -> m.remove(e)));
	}

	public boolean isInterior(BlockPos pos) {
		if (interiorBlocks != null && interiorBlocks.contains(pos))
			return true;
		for (Room room : interiorRooms)
			if (room.contains(pos))
				return true;
		return false;
	}

}
