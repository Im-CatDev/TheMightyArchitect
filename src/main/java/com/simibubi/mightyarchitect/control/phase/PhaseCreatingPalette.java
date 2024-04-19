package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.Keybinds;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.palette.PaletteEditScreen;
import com.simibubi.mightyarchitect.gui.ScreenHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public class PhaseCreatingPalette extends PhaseBase {

	private PaletteEditScreen screen;

	@Override
	public void whenEntered() {
		screen = new PaletteEditScreen(getModel().getCreatedPalette());
		ScreenHelper.open(screen);
		MightyClient.renderer.setActive(true);
	}

	@Override
	public void onKey(int key, boolean released) {
		if (Minecraft.getInstance().screen == null && !released && Keybinds.ACTIVATE.matches(key)) {
			ScreenHelper.open(screen);
			return;
		}
		super.onKey(key, released);
	}

	@Override
	public void whenExited() {
		getModel().stopPalettePreview();
		MightyClient.renderer.setActive(false);
	}

	@Override
	public void update() {}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {}

	protected void notifyChange() {}

	@Override
	public List<String> getToolTip() {
		return List.of();
	}

}
