package com.simibubi.mightyarchitect.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.gui.ScreenResources;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class Indicator extends SimpleWidget {

	public enum State {
		OFF, ON, RED, YELLOW, GREEN;
	}

	public State state;

	public Indicator(int x, int y, String tooltip) {
		this(x, y, Lang.text(tooltip)
			.component());
	}

	public Indicator(int x, int y, Component tooltip) {
		super(x, y, ScreenResources.INDICATOR.width, ScreenResources.INDICATOR.height);
		this.toolTip = toolTip.isEmpty() ? ImmutableList.of() : ImmutableList.of(tooltip);
		this.state = State.OFF;
	}

	@Override
	protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		ScreenResources toDraw;
		switch (state) {
		case ON:
			toDraw = ScreenResources.INDICATOR_WHITE;
			break;
		case OFF:
			toDraw = ScreenResources.INDICATOR;
			break;
		case RED:
			toDraw = ScreenResources.INDICATOR_RED;
			break;
		case YELLOW:
			toDraw = ScreenResources.INDICATOR_YELLOW;
			break;
		case GREEN:
			toDraw = ScreenResources.INDICATOR_GREEN;
			break;
		default:
			toDraw = ScreenResources.INDICATOR;
			break;
		}
		toDraw.draw(pGuiGraphics, getX(), getY());
	}

}
