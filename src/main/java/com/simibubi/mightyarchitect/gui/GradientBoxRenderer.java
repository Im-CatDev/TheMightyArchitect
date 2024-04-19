package com.simibubi.mightyarchitect.gui;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.mightyarchitect.foundation.utility.Color;

import net.minecraft.client.renderer.GameRenderer;

public class GradientBoxRenderer {

	static final int BORDER_OFFSET = 0;
	
	// by Zelophed
	// total box width = 1 * 2 (outer border) + 1 * 2 (inner color border) + 2 * borderOffset + width
	// defaults to 2 + 2 + 4 + 16 = 24px
	// batch everything together to save a bunch of gl calls over ScreenUtils
	
	public static void renderBox(PoseStack ms, int x, int y, int width, int height, Color background, Color borderTop,
		Color borderBot, float alpha) {
		
		/*
		*          _____________
		*        _|_____________|_
		*       | | ___________ | |
		*       | | |  |      | | |
		*       | | |  |      | | |
		*       | | |--*   |  | | |
		*       | | |      h  | | |
		*       | | |  --w-+  | | |
		*       | | |         | | |
		*       | | |_________| | |
		*       |_|_____________|_|
		*         |_____________|
		*
		* */
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Matrix4f model = ms.last().pose();
		int f = BORDER_OFFSET;
		int z = 0;
		Color c1 = background.copy().scaleAlpha(alpha);
		Color c2 = borderTop.copy().scaleAlpha(alpha);
		Color c3 = borderBot.copy().scaleAlpha(alpha);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder b = tessellator.getBuilder();

		b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		//outer top
		b.vertex(model, x - f - 1        , y - f - 2         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f - 2         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		//outer left
		b.vertex(model, x - f - 2        , y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 2        , y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		//outer bottom
		b.vertex(model, x - f - 1        , y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f + 2 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 2 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		//outer right
		b.vertex(model, x + f + 1 + width, y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 2 + width, y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 2 + width, y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		//inner background - also render behind the inner edges
		b.vertex(model, x - f - 1        , y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		tessellator.end();
		b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		//inner top - includes corners
		b.vertex(model, x - f - 1        , y - f - 1         , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f - 1         , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		//inner left - excludes corners
		b.vertex(model, x - f - 1        , y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x - f            , y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x - f            , y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		//inner bottom - includes corners
		b.vertex(model, x - f - 1        , y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f + 1 + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 1 + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		//inner right - excludes corners
		b.vertex(model, x + f     + width, y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x + f     + width, y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();

		tessellator.end();

		RenderSystem.disableBlend();
	}

}
