package com.simibubi.mightyarchitect.control.design;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.simibubi.mightyarchitect.control.palette.BlockOrientation;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockShape;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;

public class DesignSlice {

	public enum DesignSliceTrait implements StringRepresentable {
		Standard("-> Use this slice once"),
		CloneOnce("-> Duplicate this slice if necessary"),
		CloneThrice("-> Duplicate up to 3 times"),
		Optional("-> Ignore slice if necessary"),
		MaskAbove("-> Slice does not count towards effective Height"),
		MaskBelow("-> Add this slice onto lower layers");

		private String description;

		private DesignSliceTrait(String description) {
			this.description = description;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}

		public String getDescription() {
			return description;
		}

		public DesignSliceTrait cycle(int amount) {
			DesignSliceTrait[] values = values();
			return values[(this.ordinal() + amount + values.length) % values.length];
		}
	}

	private DesignSliceTrait trait;

	private Palette[][] blocks;
	private PaletteBlockShape[][] shapes;
	private BlockOrientation[][] orientations;

	public static DesignSlice fromNBT(CompoundTag sliceTag) {
		DesignSlice slice = new DesignSlice();
		slice.trait = DesignSliceTrait.valueOf(sliceTag.getString("Trait"));

		String[] blockTag = sliceTag.getString("Blocks")
			.split(",");
		String[] facingTag = sliceTag.getString("Facing")
			.split(",");
		String[] shapeTag = sliceTag.getString("Shape")
			.split(",");

		int width = blockTag[0].length();
		int length = blockTag.length;

		slice.blocks = new Palette[length][width];
		slice.shapes = new PaletteBlockShape[length][width];
		slice.orientations = new BlockOrientation[length][width];

		readSliced(blockTag, Palette::getByChar, slice.blocks);
		readSliced(shapeTag, PaletteBlockShape::getByChar, slice.shapes);
		readSliced(facingTag, BlockOrientation::getByChar, slice.orientations);

		return slice;
	}

	private static <T> void readSliced(String[] strips, Function<Character, T> reader, T[][] target) {
		int width = strips[0].length();
		int length = strips.length;

		for (int z = 0; z < length; z++)
			for (int x = 0; x < width; x++)
				target[z][x] = reader.apply(strips[z].charAt(x));
	}

	public PaletteBlockInfo getBlockAt(int x, int z, int rotation) {
		return getBlockAt(x, z, rotation, false);
	}

	public PaletteBlockInfo getBlockAt(int x, int z, int rotation, boolean mirrorX) {
		Palette palette = blocks[z][x];
		if (palette == null)
			return null;

		BlockOrientation blockOrientation = orientations[z][x];
		PaletteBlockShape blockShape = shapes[z][x];
		
		if (!blockOrientation.hasFacing())
			blockOrientation = BlockOrientation.valueOf(blockOrientation.getHalf(), Direction.NORTH);

		PaletteBlockInfo paletteBlockInfo = new PaletteBlockInfo(palette, blockShape, blockOrientation);
		paletteBlockInfo.placedOrientation = BlockOrientation.NORTH.withRotation(rotation);

		if (orientations[z][x].hasFacing() && orientations[z][x].getFacing()
			.getAxis() != Axis.Y)
			paletteBlockInfo.forceAxis = true;

		if (rotation % 180 == 0)
			paletteBlockInfo.mirrorZ = mirrorX;
		else
			paletteBlockInfo.mirrorX = mirrorX;
		
		return paletteBlockInfo;
	}

	public DesignSliceTrait getTrait() {
		return trait;
	}

	public Set<Integer> adjustHeigthsList(Set<Integer> heightsList) {
		Set<Integer> newHeights = new HashSet<>();
		for (Integer integer : heightsList) {
			switch (trait) {
			case Standard:
				newHeights.add(integer + 1);
				break;
			case CloneOnce:
				newHeights.add(integer + 1);
				newHeights.add(integer + 2);
				break;
			case CloneThrice:
				newHeights.add(integer + 1);
				newHeights.add(integer + 2);
				newHeights.add(integer + 3);
				newHeights.add(integer + 4);
				break;
			case Optional:
				newHeights.add(integer);
				newHeights.add(integer + 1);
				break;
			case MaskAbove:
			case MaskBelow:
				newHeights.add(integer);
				break;
			}
		}
		return newHeights;
	}

	public int adjustDefaultHeight(int defaultHeight) {
		switch (trait) {
		case MaskAbove:
		case MaskBelow:
			return defaultHeight;
		default:
			return defaultHeight + 1;
		}
	}

	public int addToPrintedLayers(List<DesignSlice> toPrint, int currentHeight, int targetHeight) {
		switch (trait) {

		case MaskAbove:
		case MaskBelow:
		case Standard:
			toPrint.add(this);
			return currentHeight;

		case Optional:
			if (currentHeight > targetHeight)
				return currentHeight - 1;
			toPrint.add(this);
			return currentHeight;

		case CloneOnce:
			toPrint.add(this);
			if (currentHeight >= targetHeight)
				return currentHeight;
			toPrint.add(this);
			return currentHeight + 1;

		case CloneThrice:
			toPrint.add(this);
			int i = 0;
			for (; i < 3 && currentHeight + i < targetHeight; i++)
				toPrint.add(this);
			return currentHeight + i;

		default:
			return currentHeight;
		}
	}

}
