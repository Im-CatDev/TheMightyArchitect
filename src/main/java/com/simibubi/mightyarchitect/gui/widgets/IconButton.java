package com.simibubi.mightyarchitect.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.gui.ScreenResources;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class IconButton extends SimpleWidget {

	protected ScreenResources icon;
	protected boolean pressed;

	public IconButton(int x, int y, ScreenResources icon) {
		super(x, y, 18, 18);
		this.icon = icon;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float pPartialTick) {
		if (this.visible) {
			int x = getX();
			int y = getY();
			this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;

			ScreenResources button = (pressed || !active) ? button = ScreenResources.BUTTON_DOWN
				: (isHovered) ? ScreenResources.BUTTON_HOVER : ScreenResources.BUTTON;

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			button.draw(graphics, x, y);
			icon.draw(graphics, x + 1, y + 1);
		}
	}

	@Override
	public void onClick(double p_onClick_1_, double p_onClick_3_) {
		super.onClick(p_onClick_1_, p_onClick_3_);
		this.pressed = true;
	}

	@Override
	public void onRelease(double p_onRelease_1_, double p_onRelease_3_) {
		super.onRelease(p_onRelease_1_, p_onRelease_3_);
		this.pressed = false;
	}

	public void setToolTip(String text) {
		setToolTip(Lang.text(text)
			.component());
	}

	public void setToolTip(Component text) {
		toolTip.clear();
		toolTip.add(text);
	}

}