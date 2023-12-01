package com.simibubi.mightyarchitect.control.design;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.design.partials.Corner;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.control.design.partials.Facade;
import com.simibubi.mightyarchitect.control.design.partials.FlatRoof;
import com.simibubi.mightyarchitect.control.design.partials.Roof;
import com.simibubi.mightyarchitect.control.design.partials.Tower;
import com.simibubi.mightyarchitect.control.design.partials.TowerFlatRoof;
import com.simibubi.mightyarchitect.control.design.partials.TowerRoof;
import com.simibubi.mightyarchitect.control.design.partials.Wall;
import com.simibubi.mightyarchitect.gui.ScreenResources;

public enum DesignType {

	WALL("wall", "Wall", new Wall(), ScreenResources.ICON_WALL),
	FACADE("facade", "Facade", new Facade(), ScreenResources.ICON_FACADE),
	CORNER("corner", "Corner", new Corner(), ScreenResources.ICON_CORNER),
	TOWER("tower", "Tower", new Tower(), ScreenResources.ICON_TOWER_NO_ROOF),
	GABLE_ROOF("roof", "Gable Roof", new Roof(), ScreenResources.ICON_NORMAL_ROOF),
	FLAT_ROOF("flatroof", "Flat Roof", new FlatRoof(), ScreenResources.ICON_FLAT_ROOF),
	TOWER_CONE("towerroof", "Tower Cone", new TowerRoof(), ScreenResources.ICON_TOWER_ROOF),
	TOWER_CAP("towerflatroof", "Tower Cap", new TowerFlatRoof(), ScreenResources.ICON_TOWER_FLAT_ROOF),

	NONE("none", "None", null, ScreenResources.ICON_NONE);

	private String filePath;
	private String displayName;
	private Design design;
	private ScreenResources icon;

	private DesignType(String filePath, String displayName, Design design, ScreenResources icon) {
		this.filePath = filePath;
		this.displayName = displayName;
		this.design = design;
		this.icon = icon;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Design getDesign() {
		return design;
	}

	public ScreenResources getIcon() {
		return icon;
	}

	public String getAdditionalDataName() {
		return switch (this) {
		case GABLE_ROOF -> "Roof Span";
		case FLAT_ROOF -> "Margin";
		case TOWER, TOWER_CAP, TOWER_CONE -> "Tower Radius";
		case WALL -> "Horizontal Repetition";
		default -> "";
		};
	}

	public boolean hasAdditionalData() {
		switch (this) {
		case WALL:
		case FLAT_ROOF:
		case GABLE_ROOF:
		case TOWER:
		case TOWER_CAP:
		case TOWER_CONE:
			return true;
		default:
			return false;
		}
	}

	public int getMaxSize() {
		return switch (this) {
		case GABLE_ROOF -> ThemeStatistics.MAX_ROOF_SPAN;
		case FLAT_ROOF -> ThemeStatistics.MAX_MARGIN;
		case TOWER, TOWER_CAP, TOWER_CONE -> ThemeStatistics.MAX_TOWER_RADIUS;
		case WALL -> Wall.ExpandBehaviour.values().length - 1;
		default -> 0;
		};
	}

	public int getMinSize() {
		return switch (this) {
		case GABLE_ROOF -> ThemeStatistics.MIN_ROOF_SPAN;
		case FLAT_ROOF -> ThemeStatistics.MIN_MARGIN;
		case TOWER, TOWER_CAP, TOWER_CONE -> ThemeStatistics.MIN_TOWER_RADIUS;
		default -> 0;
		};
	}

	public String formatData(int data) {
		return switch (this) {
		case WALL -> Wall.ExpandBehaviour.values()[data].getDisplayName();
		case GABLE_ROOF, FLAT_ROOF, TOWER, TOWER_CAP, TOWER_CONE -> data + "m";
		default -> "";
		};
	}

	public static List<DesignType> defaults() {
		return ImmutableList.of(WALL, FACADE, CORNER);
	}

	public static List<DesignType> roofTypes() {
		return ImmutableList.of(GABLE_ROOF, FLAT_ROOF, TOWER_CAP, TOWER_CONE);
	}

}
