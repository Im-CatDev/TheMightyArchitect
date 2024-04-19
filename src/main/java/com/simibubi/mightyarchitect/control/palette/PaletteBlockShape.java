package com.simibubi.mightyarchitect.control.palette;

import java.util.EnumSet;

public enum PaletteBlockShape {

	REGULAR(4), SLAB(2), STAIRS(3), WALL(1);

	public static final EnumSet<PaletteBlockShape>
		SINGLE = EnumSet.of(PaletteBlockShape.REGULAR),
		SLAB_STAIR = EnumSet.of(PaletteBlockShape.REGULAR, PaletteBlockShape.STAIRS, PaletteBlockShape.SLAB),
		SLAB_STAIR_WALL = EnumSet.of(PaletteBlockShape.REGULAR, PaletteBlockShape.STAIRS, PaletteBlockShape.SLAB,
			PaletteBlockShape.WALL);

	public int priority;

	private PaletteBlockShape(int priority) {
		this.priority = priority;
	}

	public final int comparePriority(PaletteBlockShape other) {
		return priority - other.priority;
	}

	public static PaletteBlockShape getByChar(char character) {
		if (character == ' ')
			return REGULAR;
		return values()[character - 'A'];
	}

	public char asChar() {
		return (char) ('A' + ordinal());
	}

}
