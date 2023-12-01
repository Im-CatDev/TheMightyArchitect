package com.simibubi.mightyarchitect.control.design;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.gui.ScreenResources;

public enum DesignLayer {
	
	FOUNDATION("foundation", "Foundation", ScreenResources.ICON_LAYER_FOUNDATION),
	REGULAR("regular", "Regular", ScreenResources.ICON_LAYER_REGULAR),
	ARCHWAYS("open", "Archways", ScreenResources.ICON_LAYER_OPEN),
	SPECIAL("special", "Special", ScreenResources.ICON_LAYER_SPECIAL),
	
	NONE("none", "None", ScreenResources.ICON_NONE),
	ROOFING("roofing", "Roofing", ScreenResources.ICON_LAYER_ROOF);
	
	private String filePath;
	private String displayName;
	private ScreenResources icon;
	
	private DesignLayer(String filePath, String displayName, ScreenResources icon) {
		this.filePath = filePath;
		this.displayName = displayName;
		this.icon = icon;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public ScreenResources getIcon() {
		return icon;
	}
	
	public boolean isExterior() {
		return this == ARCHWAYS;
	}
	
	public static List<DesignLayer> defaults() {
		return ImmutableList.of(ROOFING);
	}
	
	
}
