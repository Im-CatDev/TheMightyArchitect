package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.ArchitectMenu;
import com.simibubi.mightyarchitect.control.ArchitectMenu.KeyBindList;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.StringTextComponent;

public class ArchitectMenuScreen extends Screen {

	private KeyBindList keybinds;
	private String title;
	private List<String> tooltip;
	private boolean focused;
	private boolean visible;

	private int menuWidth;
	private int menuHeight;

	private int targetY;
	private float movingY;

	public ArchitectMenuScreen() {
		super(new StringTextComponent("Architect Menu"));
		keybinds = new KeyBindList();
		tooltip = new ArrayList<>();
		title = "";
		focused = false;
		visible = false;
		movingY = 0;
		targetY = 0;
		adjustTarget();
	}

	public void updateContents() {
		int textRendererheight = Minecraft.getInstance().font.lineHeight;

		// update tooltips and keybinds
		tooltip = ArchitectManager.getPhase()
			.getPhaseHandler()
			.getToolTip();
		keybinds = ArchitectMenu.getKeybinds();
		title = ArchitectManager.getPhase()
			.getDisplayTitle();

		menuWidth = 158;
		menuHeight = 4;

		menuHeight += 12; // title
		menuHeight += 4 + (keybinds.size() * textRendererheight); // keybinds

		menuHeight += 4;
		for (String s : tooltip)
			menuHeight += Minecraft.getInstance().font.wordWrapHeight(s, menuWidth - 8) + 2;

		adjustTarget();
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		// FOCUSED
		super.render(ms, mouseX, mouseY, partialTicks);
		draw(ms, partialTicks);
	}

	public void drawPassive() {
		if (isFocused())
			return;

		// NOT FOCUSED
		draw(new MatrixStack(), Minecraft.getInstance()
			.getFrameTime());
	}

	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
		boolean hideOnClose =
			ArchitectManager.inPhase(ArchitectPhases.Empty) || ArchitectManager.inPhase(ArchitectPhases.Paused);

		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (hideOnClose)
				setVisible(false);
			minecraft.setScreen(null);
			return true;
		}

		if (keyCode == MightyClient.COMPOSE.getKey()
			.getValue()) {
			if (hideOnClose)
				setVisible(false);
			minecraft.setScreen(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		boolean hideOnClose =
			ArchitectManager.inPhase(ArchitectPhases.Empty) || ArchitectManager.inPhase(ArchitectPhases.Paused);
		if (ArchitectMenu.handleMenuInput(p_charTyped_1_)) {
			if (ArchitectManager.inPhase(ArchitectPhases.Paused))
				setVisible(false);
			minecraft.setScreen(null);
			return true;
		}
		if (p_charTyped_1_ == 'e') {
			if (hideOnClose)
				setVisible(false);
			minecraft.setScreen(null);
			return true;
		}

		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	private void draw(MatrixStack ms, float partialTicks) {
		MainWindow mainWindow = Minecraft.getInstance()
			.getWindow();
		int x = mainWindow.getGuiScaledWidth() - menuWidth - 10;
		int y = mainWindow.getGuiScaledHeight() - menuHeight;

		int mouseX = (int) (Minecraft.getInstance().mouseHandler.xpos() / mainWindow.getGuiScale());
		int mouseY = (int) (Minecraft.getInstance().mouseHandler.ypos() / mainWindow.getGuiScale());

		boolean sideways = false;
		if ((mainWindow.getGuiScaledWidth() - 182) / 2 < menuWidth + 20) {
			sideways = true;
			y -= 24;
		}

		RenderSystem.pushMatrix();
		float shift = yShift(partialTicks);
		float sidewaysShift =
			shift * ((float) menuWidth / (float) menuHeight) + (!focused ? 40 + menuHeight / 4f : 0) + 8;
		RenderSystem.translatef(sideways ? sidewaysShift : 0, sideways ? 0 : shift, 0);
		mouseX -= sideways ? sidewaysShift : 0;
		mouseY -= sideways ? 0 : shift;

		ScreenResources gray = ScreenResources.GRAY;
		RenderSystem.enableBlend();
		RenderSystem.color4f(1, 1, 1, 3 / 4f);

		Minecraft.getInstance()
			.getTextureManager()
			.bind(gray.location);
		RenderSystem.enableTexture();
		blit(ms, x, y, gray.startX, gray.startY, menuWidth, menuHeight, gray.width, gray.height);
		RenderSystem.color4f(1, 1, 1, 1);

		int yPos = y + 4;
		int xPos = x + 4;

		FontRenderer textRenderer = Minecraft.getInstance().font;
		String compose = MightyClient.COMPOSE.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
		if (!focused) {
			if (sideways) {
				if (visible) {
					String string = "Press " + compose.toUpperCase() + " for Menu";
					textRenderer.drawShadow(ms, string,
						mainWindow.getGuiScaledWidth() - textRenderer.width(string) - 15 - sidewaysShift,
						yPos - 14, 0xEEEEEE);
				}
			} else {
				textRenderer.drawShadow(ms, "Press " + compose.toUpperCase() + " to focus", xPos, yPos - 14,
					0xEEEEEE);
			}
		} else {
			String string = "Press " + compose + " to close";
			textRenderer.drawShadow(ms, string,
				sideways
					? Math.min(xPos,
						mainWindow.getGuiScaledWidth() - textRenderer.width(string) - 15 - sidewaysShift)
					: xPos,
				yPos - 14, 0xDDDDDD);
		}
		textRenderer.drawShadow(ms, title, xPos, yPos, 0xEEEEEE);

		boolean hoveredHorizontally = x <= mouseX && mouseX <= x + menuWidth && focused;

		yPos += 4;
		for (String key : keybinds.getKeys()) {
			if (key.isEmpty()) {
				yPos += textRenderer.lineHeight / 2;
				continue;
			}

			yPos += textRenderer.lineHeight;
			int color =
				hoveredHorizontally && yPos < mouseY && mouseY <= yPos + textRenderer.lineHeight ? 0xFFFFFF : 0xCCDDFF;
			textRenderer.drawShadow(ms, "[" + key + "] " + keybinds.get(key), xPos, yPos, color);
			textRenderer.drawShadow(ms, ">", xPos - 12, yPos, color);
		}

		yPos += 4;
		yPos += textRenderer.lineHeight;
		for (String text : tooltip) {
			int height = Minecraft.getInstance().font.wordWrapHeight(text, menuWidth - 8);
			int lineY = yPos;
			for (IReorderingProcessor iro : textRenderer.split(new StringTextComponent(text), menuWidth - 8)) {
				textRenderer.draw(ms, iro, xPos, lineY, 0xEEEEEE);				
				lineY += textRenderer.lineHeight;
			}
			yPos += height + 2;
		}

		RenderSystem.popMatrix();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !visible || !focused)
			return super.mouseClicked(mouseX, mouseY, button);

		MainWindow mainWindow = Minecraft.getInstance()
			.getWindow();
		int x = mainWindow.getGuiScaledWidth() - menuWidth - 10;
		int y = mainWindow.getGuiScaledHeight() - menuHeight;

		boolean sideways = false;
		if ((mainWindow.getGuiScaledWidth() - 182) / 2 < menuWidth + 20) {
			sideways = true;
			mouseY += 24;
		}

		float shift = yShift(0);
		mouseX -= sideways ? shift * 2 : 0;
		mouseY -= sideways ? 0 : shift;

		boolean hoveredHorizontally = x <= mouseX && mouseX <= x + menuWidth;

		int yPos = y + 4;
		yPos += 4;
		for (String key : keybinds.getKeys()) {
			if (key.isEmpty()) {
				yPos += font.lineHeight / 2;
				continue;
			}

			yPos += font.lineHeight;
			if (hoveredHorizontally && yPos < mouseY && mouseY <= yPos + font.lineHeight) {
				charTyped(key.toLowerCase()
					.charAt(0), GLFW.GLFW_PRESS);
			}
		}

		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
		adjustTarget();
	}

	protected void adjustTarget() {
		targetY = visible ? (focused ? 0 : menuHeight - 14) : menuHeight + 20;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		adjustTarget();
	}

	@Override
	public void removed() {
		super.removed();
		setFocused(false);
	}

	private float yShift(float partialTicks) {
		return (movingY + (targetY - movingY) * 0.2f * partialTicks);
	}

	public void onClientTick() {
		if (movingY != targetY) {
			movingY += (targetY - movingY) * 0.2;
		}
	}

}
