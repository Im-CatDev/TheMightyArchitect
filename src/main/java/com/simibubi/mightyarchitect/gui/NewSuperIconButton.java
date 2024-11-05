package com.simibubi.mightyarchitect.gui;

import java.util.function.BiConsumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.foundation.utility.Color;
import com.simibubi.mightyarchitect.gui.widgets.IconButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class NewSuperIconButton extends IconButton {

	protected Component label;
	private BiConsumer<Double, Double> callback;

	public boolean highlighted;

	public NewSuperIconButton(int x, int y, int width, ScreenResources icon, Component label) {
		super(x, y, icon);
		this.width = width;
		this.height = icon == null ? 15 : 30;
		this.label = label;
	}

	public void setCallback(BiConsumer<Double, Double> callback) {
		this.callback = callback;
	}

	@Override
	public void onClick(double p_onClick_1_, double p_onClick_3_) {
		super.onClick(p_onClick_1_, p_onClick_3_);
		callback.accept(p_onClick_1_, p_onClick_3_);
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;

		int x = getX();
		int y = getY();
		PoseStack matrixStack = graphics.pose();
		isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

		Color background = new Color(10, 10, 10, 120);
		Color border = getBorderColour();

		int bx = highlighted ? x - 1 : x;
		int by = highlighted ? y - 1 : y;
		int w = highlighted ? width + 2 : width;
		int h = highlighted ? height + 2 : height;

		GradientBoxRenderer.renderBox(matrixStack, bx, by + 1, w, h, background, border, border, 1f);
		GradientBoxRenderer.renderBox(matrixStack, bx, by, w, h, background, border, border, 1f);
		Color darker = border.copy()
			.darker()
			.darker()
			.scaleAlpha(.5f);
		GradientBoxRenderer.renderBox(matrixStack, bx + 1, by + 1, w - 2, h - 2, Color.TRANSPARENT_BLACK, darker,
			darker, 1f);

		drawContents(graphics, border);
	}

	protected Color getBorderColour() {
		return (highlighted || !active) ? new Color(240, 240, 240)
			: (isHovered) ? new Color(200, 200, 255) : new Color(130, 130, 140);
	}

	protected void drawContents(GuiGraphics graphics, Color borderColour) {
		int x = getX();
		int y = getY();
		Font font = Minecraft.getInstance().font;
		int labelWidth = font.width(label);
		graphics.drawString(font, label, x + 1 - labelWidth / 2 + width / 2, y + (icon == null ? 4 : 18), borderColour.getRGB(), false);

		if (icon != null)
			icon.draw(graphics, x + width / 2 - 8, y + 2);
	}

}
