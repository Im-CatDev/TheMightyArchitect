package com.simibubi.mightyarchitect.control.palette;

import static com.simibubi.mightyarchitect.control.palette.PaletteBlockShape.SINGLE;
import static com.simibubi.mightyarchitect.control.palette.PaletteBlockShape.SLAB_STAIR;
import static com.simibubi.mightyarchitect.control.palette.PaletteBlockShape.SLAB_STAIR_WALL;

import java.util.EnumSet;

public enum Palette {
	
	FOUNDATION_FILL("Foundation Walls", 8, SLAB_STAIR_WALL),
	FOUNDATION_EDGE("Foundation Wall Edges", 10, SLAB_STAIR_WALL),
	FOUNDATION_DECO("Foundation Details", 8, SINGLE),
	FOUNDATION_WINDOW("Foundation Windows", 3, SINGLE),
	
	STANDARD_FILL("Standard Walls", 7, SLAB_STAIR),
	STANDARD_EDGE("Standard Wall Edges", 9, SLAB_STAIR),
	STANDARD_DECO("Standard Wall Details", 7, SINGLE),
	STANDARD_WINDOW("Standard Windows", 6, SINGLE),
	
	ROOF_FILL("Roof Material", 4, SLAB_STAIR_WALL),
	ROOF_EDGE("Roof Edges", 5, SLAB_STAIR_WALL),
	ROOF_DECO("Roof Details", 4, SINGLE),
	
	HEAVY_POST("Heavy Posts", 2, SINGLE),
	LIGHT_POST("Light Posts / Fences", 1, SINGLE),
	PANEL("Detailing Panels", 2, SINGLE),
	INTERIOR_FLOOR("Interior Flooring", 9, SINGLE),
	EXTERIOR_FLOOR("Exterior Flooring", 9, SINGLE),
	
	CLEAR("Clearing Material", 100, SINGLE);

	private int priority;
	private String displayName;
	private EnumSet<PaletteBlockShape> shapes;
	
	private Palette(String displayName, int priority, EnumSet<PaletteBlockShape> shapes) {
		this.displayName = displayName;
		this.priority = priority;
		this.shapes = shapes;
	}
	
	public int comparePriority(Palette other) {
		return priority - other.priority;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public EnumSet<PaletteBlockShape> getShapes() {
		return shapes;
	}
	
	public static Palette getByChar(char character) {
		if (character == ' ')
			return null;
		return (values()[character - 'A']);
	}
	
	public char asChar() {
		return (char) ('A' + ordinal());
	}
	
	
	
	
}
