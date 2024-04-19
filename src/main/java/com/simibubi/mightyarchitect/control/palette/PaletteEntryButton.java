package com.simibubi.mightyarchitect.control.palette;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.foundation.utility.Color;
import com.simibubi.mightyarchitect.gui.NewSuperIconButton;

import net.minecraft.client.gui.GuiGraphics;

public class PaletteEntryButton extends NewSuperIconButton {

	private Consumer<PoseStack> contentRenderer;

	public boolean changed;

	public PaletteEntryButton(int x, int y, boolean simple, Consumer<PoseStack> contentRenderer) {
		super(x, y, simple ? 24 : 52, null, null);
		this.contentRenderer = contentRenderer;
		height = 24;
	}

	@Override
	protected Color getBorderColour() {
		if (changed)
			return (highlighted || !active) ? new Color(240, 240, 240)
				: (isHovered) ? new Color(200, 200, 255) : new Color(130, 240, 200);
		return super.getBorderColour();
	}

	@Override
	protected void drawContents(GuiGraphics graphics, Color borderColour) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(getX(), getY(), 100);
		contentRenderer.accept(ms);
		ms.popPose();
	}

}
