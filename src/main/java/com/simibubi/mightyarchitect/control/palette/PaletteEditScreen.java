package com.simibubi.mightyarchitect.control.palette;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.Schematic;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.foundation.utility.Color;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.foundation.utility.LerpedFloat;
import com.simibubi.mightyarchitect.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.mightyarchitect.gui.GradientBoxRenderer;
import com.simibubi.mightyarchitect.gui.GuiGameElement;
import com.simibubi.mightyarchitect.gui.ScreenResources;
import com.simibubi.mightyarchitect.gui.SimpleScreen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class PaletteEditScreen extends SimpleScreen {

	private static final Color BLACK_50 = new Color(0, 0, 0, 50);
	private static final Color BLACK_150 = new Color(0, 0, 0, 150);
	private static final Color BLACK_120 = new Color(0, 0, 0, 120);
	private static final Color NONE = new Color(0, 0, 0, 0);

	private PaletteDefinition palette;
	private PalettePickerItemList itemPicker = new PalettePickerItemList(this);

	private EditBox searchBox;
	private boolean focusBox;

	private EditBox nameBox;

	private EnumMap<Palette, PaletteEntryButton> buttons = new EnumMap<>(Palette.class);

	public Palette selectedEntry;
	private SimplePaletteMapping newMappings;

	private LerpedFloat itemScroll = LerpedFloat.linear()
		.startWithValue(0);

	public PaletteEditScreen(PaletteDefinition palette) {
		this.palette = palette;
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (selectedEntry != null && isHoveringTinyButton(x, y, topLeftX + 278, topLeftY + 46)) {
			for (PaletteEntryButton btn : buttons.values())
				btn.highlighted = false;
			selectedEntry = null;
			newMappings = null;
			searchBox.setFocus(false);
			searchBox.setVisible(false);
			nameBox.setVisible(true);
			return true;
		}

		if (selectedEntry == null) {
			if (isHoveringTinyButton(x, y, topLeftX - 23 + 270, nameBox.y - 3)) {
				minecraft.setScreen(null);
				return true;
			}

			if (isHoveringTinyButton(x, y, topLeftX - 23 + 286, nameBox.y - 3)) {
				ArchitectManager.finishPalette(nameBox.getValue());
				minecraft.setScreen(null);
				return true;
			}

			if (isHoveringTinyButton(x, y, topLeftX - 23 + 302, nameBox.y - 3)) {
				ArchitectManager.enterPhase(ArchitectPhases.Previewing);
				minecraft.setScreen(null);
				return true;
			}
		}

		int hoveredSlot = getHoveredSlot((int) x, (int) y);
		if (hoveredSlot == -1)
			return super.mouseClicked(x, y, button);

		newMappings.append(itemPicker.getValidBlockStateFor(itemPicker.get(hoveredSlot)));

		if (getCurrentlyRequiredShape() != null) {
			searchBox.setValue("");
			refreshSearchResults();
			return true;
		}

		Schematic model = ArchitectManager.getModel();

		PaletteMapping original = model.getPrimary()
			.getDefinition()
			.get(selectedEntry);

		buttons.get(selectedEntry).changed = !original.toNBT()
			.toString()
			.equals(newMappings.toNBT()
				.toString());
		for (PaletteEntryButton btn : buttons.values())
			btn.highlighted = false;

		palette.put(selectedEntry, newMappings);
		model.updatePalettePreview();
		MightyClient.renderer.update();
		selectedEntry = null;
		newMappings = null;
		searchBox.setFocus(false);
		searchBox.setVisible(false);
		nameBox.setVisible(true);
		return true;
	}

	private int getHoveredSlot(int x, int y) {
		int itemsX = topLeftX - 23;
		int itemsY = topLeftY + 75;
		if (selectedEntry == null)
			return -1;
		if (x < itemsX || x >= itemsX + 17 * 18)
			return -1;
		if (y < itemsY || y >= itemsY + 4 * 18)
			return -1;
		if (!itemScroll.settled())
			return -1;
		int row = (y - itemsY) / 18 + (int) itemScroll.getChaseTarget();
		int col = (x - itemsX) / 18;
		int slot = row * 17 + col;
		if (itemPicker.size() <= slot)
			return -1;
		return slot;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		float newTarget = itemScroll.getChaseTarget() + (int) Math.signum(-delta);
		newTarget = Mth.clamp(newTarget, 0, Math.max(0, Mth.ceil(itemPicker.size() / 17f) - 4));
		itemScroll.chase(newTarget, 0.5, Chaser.EXP);
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		itemScroll.tickChaser();
		if (Math.abs(itemScroll.getValue() - itemScroll.getChaseTarget()) < 1 / 16f)
			itemScroll.setValue(itemScroll.getChaseTarget());
		if (focusBox) {
			focusBox = false;
			if (searchBox.isFocused())
				return;
			searchBox.changeFocus(true);
			setFocused(searchBox);
		}
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = topLeftX - 23;
		int y = topLeftY + 170;

		bgBox(ms, x - 10, y - 8, 324, 80, BLACK_50);

		font.draw(ms, Component.literal("Palette Entries"), x, y - 2, 0xeeeeee);

		for (Palette key : Palette.values()) {
			if (!buttons.containsKey(key) || !buttons.get(key)
				.isHoveredOrFocused())
				continue;
			drawCenteredString(ms, font, key.getDisplayName(), topLeftX + 128, y - 2, 0x9999aa);
			break;
		}

		int searchWidth = searchBox.getWidth();
		int searchHeight = searchBox.getHeight();

		if (selectedEntry == null) {
			bgBox(ms, x + 121, nameBox.y - 1, nameBox.getWidth() + 24, nameBox.getHeight() + 1, BLACK_150);
			bgBox(ms, x + 272, nameBox.y - 1, searchHeight + 1, searchHeight + 1, BLACK_120);
			bgBox(ms, x + 288, nameBox.y - 1, searchHeight + 1, searchHeight + 1, BLACK_120);
			bgBox(ms, x + 304, nameBox.y - 1, searchHeight + 1, searchHeight + 1, BLACK_120);

			ScreenResources.I8_EYE.draw(ms, this, x + 273, nameBox.y);
			ScreenResources.I8_FLOPPY.draw(ms, this, x + 289, nameBox.y);
			ScreenResources.I8_BIN.draw(ms, this, x + 305, nameBox.y);

			if (isHoveringTinyButton(mouseX, mouseY, x + 270, nameBox.y - 3))
				renderTooltip(ms, Lang.text("Preview")
					.component(), mouseX, mouseY);

			if (isHoveringTinyButton(mouseX, mouseY, x + 286, nameBox.y - 3))
				renderTooltip(ms, Lang.text("Save")
					.component(), mouseX, mouseY);

			if (isHoveringTinyButton(mouseX, mouseY, x + 302, nameBox.y - 3))
				renderTooltip(ms, Lang.text("Discard")
					.component(), mouseX, mouseY);

			return;
		}

		bgBox(ms, x - 10, y - 100, 324, 80, BLACK_50);
		bgBox(ms, x - 10, searchBox.y - 1, searchWidth + 24, searchHeight + 1, BLACK_150);
		bgBox(ms, x + searchWidth + 22, searchBox.y - 1, 193, searchHeight + 1, BLACK_150);
		bgBox(ms, x + 303, searchBox.y - 1, searchHeight + 2, searchHeight + 1, BLACK_120);

		if (isHoveringTinyButton(mouseX, mouseY, topLeftX + 278, topLeftY + 46))
			renderTooltip(ms, Lang.text("Cancel")
				.component(), mouseX, mouseY);

		PaletteBlockShape shape = getCurrentlyRequiredShape();
		MutableComponent header = Component.literal("Choose new " + selectedEntry.getDisplayName());
		if (shape != PaletteBlockShape.REGULAR)
			header = Component.literal("Requires additional for " + shape.name()
				.toLowerCase(Locale.ROOT) + " shape");
		font.drawShadow(ms, Lang.text("x")
			.component(), x + 306, searchBox.y - 1, 0xeeeeee);
		font.draw(ms, header, x + searchWidth + 30, searchBox.y, 0xeeeeee);

		if (searchBox.getValue()
			.isEmpty())
			font.draw(ms, Component.literal("Search Blocks..."), searchBox.x, searchBox.y, 0x888888);

		if (itemPicker.isEmpty())
			return;

		int cols = 17;
		float currentScroll = itemScroll.getValue(minecraft.getDeltaFrameTime());
		int startRow = Math.max(0, Mth.floor(currentScroll) - 1);

		ms.pushPose();
		ms.translate(0, -currentScroll * 18, 0);
		int hoveredSlot = getHoveredSlot(mouseX, mouseY);

		for (int row = startRow; row < startRow + 6; row++) {
			float scale = 1;
			if (row < currentScroll)
				scale = Mth.clamp(1 - (currentScroll - row), 0, 1);
			if (row > currentScroll + 3)
				scale = Mth.clamp((currentScroll + 4) - row, 0, 1);
			scale *= scale;
			if (scale < 0.1)
				continue;

			for (int col = 0; col < cols; col++) {
				int index = row * cols + col;
				if (itemPicker.size() <= index)
					break;

				ms.pushPose();
				ms.translate(x + col * 18, y + row * 18 - 95, 0);

				float scaleFromHover = 1;

				ms.translate(0, 0, 200);
				if (itemPicker.superHighlightedSlots.contains(index) && scale == 1)
					font.drawShadow(ms, Component.literal("^"), 11, 12, 0xFFFF55);
				if (itemPicker.highlightedSlots.contains(index) && scale == 1)
					font.drawShadow(ms, Component.literal(".."), 11, 8, 0xbbbbbb);
				ms.translate(0, 0, -200);

				if (index == hoveredSlot) {
					Color color = new Color(255, 255, 255, 10);
					Color color2 = new Color(255, 255, 255, 50);
					ms.translate(-.5f, -.5f, 0);
					GradientBoxRenderer.renderBox(ms, 0, 0, 17, 17, color, color2, color2, 1f);
					ms.translate(.5f, .5f, 0);
					scaleFromHover += .05f;
				}

				ms.translate(9, 9, 0);
				ms.scale(scale, scale, scale);
				ms.scale(scaleFromHover, scaleFromHover, scaleFromHover);
				ms.translate(-9, -9, 0);
				GuiGameElement.of(itemPicker.get(index))
					.render(ms);

				ms.popPose();
			}
		}

		ms.popPose();

		int rows = Mth.ceil(itemPicker.size() / 17f);
		int barSize = Mth.floor(80f * 4f / rows);
		if (barSize < 80) {
			ms.pushPose();
			ms.translate(0, currentScroll * (80 - barSize) / Math.max(1, rows - 4), 0);
			Color color = new Color(255, 255, 255, 50);
			GradientBoxRenderer.renderBox(ms, x + 323 - 10, y - 100, 1, barSize, BLACK_50, color, color, 1f);
			ms.popPose();
		}

		if (hoveredSlot != -1) {
			ArrayList<Component> list = Lists.newArrayList(itemPicker.get(hoveredSlot)
				.getHoverName());
			if (itemPicker.highlightedSlots.contains(hoveredSlot))
				list.add(Lang.text("Covers some shapes")
					.color(0xdddddd)
					.component());
			if (itemPicker.superHighlightedSlots.contains(hoveredSlot))
				list.add(Lang.text("Covers all shapes")
					.color(0xFFFF55)
					.component());
			renderComponentTooltip(ms, list, mouseX, mouseY);
		}
	}

	@Override
	public void renderBackground(PoseStack pPoseStack) {
		fillGradient(pPoseStack, 0, 0, width, height, BLACK_50.getRGB(), BLACK_150.getRGB());
	}

	@Override
	protected void init() {
		super.init();
		setWindowSize(256, 256);
		widgets.clear();

		int x = topLeftX - 23;
		int y = topLeftY + 180;
		int GRID = 14;
		int line1 = y + GRID * 0;
		int line2 = y + GRID * 2;

		addPaletteEntryButton(x + GRID * 0, line1, Palette.FOUNDATION_EDGE);
		addPaletteEntryButton(x + GRID * 0, line2, Palette.FOUNDATION_FILL);
		addPaletteEntryButton(x + GRID * 4, line1, Palette.FOUNDATION_DECO);
		addPaletteEntryButton(x + GRID * 4, line2, Palette.FOUNDATION_WINDOW);
		addPaletteEntryButton(x + GRID * 6, line1, Palette.STANDARD_EDGE);
		addPaletteEntryButton(x + GRID * 6, line2, Palette.STANDARD_FILL);
		addPaletteEntryButton(x + GRID * 10, line1, Palette.STANDARD_DECO);
		addPaletteEntryButton(x + GRID * 10, line2, Palette.STANDARD_WINDOW);
		addPaletteEntryButton(x + GRID * 12, line1, Palette.INTERIOR_FLOOR);
		addPaletteEntryButton(x + GRID * 12, line2, Palette.LIGHT_POST);
		addPaletteEntryButton(x + GRID * 14, line1, Palette.HEAVY_POST);
		addPaletteEntryButton(x + GRID * 14, line2, Palette.PANEL);
		addPaletteEntryButton(x + GRID * 16, line1, Palette.ROOF_EDGE);
		addPaletteEntryButton(x + GRID * 16, line2, Palette.ROOF_FILL);
		addPaletteEntryButton(x + GRID * 20, line1, Palette.ROOF_DECO);
		addPaletteEntryButton(x + GRID * 20, line2, Palette.EXTERIOR_FLOOR);

		minecraft.keyboardHandler.setSendRepeatsToGui(true);

		searchBox = new EditBox(this.font, x, y - 130, 80, 9, Component.translatable("itemGroup.search"));
		searchBox.setValue("");
		searchBox.setMaxLength(50);
		searchBox.setBordered(false);
		searchBox.setVisible(selectedEntry != null);
		searchBox.setTextColor(16777215);
		searchBox.setResponder(s -> refreshSearchResults());
		widgets.add(searchBox);

		nameBox = new EditBox(this.font, x + 132, y - 38, 120, 9, Lang.empty());
		nameBox.setValue("New Palette");
		nameBox.setMaxLength(50);
		nameBox.setBordered(false);
		nameBox.setVisible(selectedEntry == null);
		nameBox.setTextColor(16777215);
		widgets.add(nameBox);
	}

	private void addPaletteEntryButton(int x, int y, Palette key) {
		boolean simple = key.getShapes()
			.size() == 1;
		PaletteEntryButton button = new PaletteEntryButton(x, y, simple, ms -> {
			ms.pushPose();
			ms.translate(3.75f, 17.5f, 100);
			for (PaletteBlockShape paletteBlockShape : key.getShapes()) {
				BlockState blockState = selectedEntry == key && getCurrentlyRequiredShape() != PaletteBlockShape.REGULAR
					? newMappings.provide(paletteBlockShape)
					: palette.get(key, paletteBlockShape);
				if (blockState == null)
					continue;
				if (key == Palette.PANEL)
					ms.translate(3, 1, 0);
				if (blockState.getBlock() instanceof IronBarsBlock ibb)
					blockState = blockState.setValue(CrossCollisionBlock.EAST, true)
						.setValue(CrossCollisionBlock.WEST, true);
				GuiGameElement.of(blockState)
					.rotate(22.5, 45, 0)
					.scale(11f)
					.render(ms);
				ms.translate(11, 0, 0);
			}
			ms.popPose();
		});
		button.setCallback((mx, my) -> {
			for (AbstractWidget abstractWidget : widgets)
				if (abstractWidget instanceof PaletteEntryButton peb)
					peb.highlighted = false;

			if (selectedEntry == key) {
				selectedEntry = null;
				newMappings = null;
				searchBox.changeFocus(false);
				searchBox.setVisible(false);
				nameBox.setVisible(true);
				return;
			}

			button.highlighted = true;
			selectedEntry = key;
			newMappings = new SimplePaletteMapping();
			searchBox.setValue("");
			searchBox.setVisible(true);
			nameBox.setVisible(false);
			if (nameBox.isFocused())
				nameBox.setFocus(false);
			focusBox = true;
			refreshSearchResults();
		});
		widgets.add(button);
		buttons.put(key, button);
	}

	private void refreshSearchResults() {
		String s = searchBox.getValue();
		itemPicker.clear();
		itemScroll.startWithValue(0);

		if (s.isEmpty()) {
			for (Item item : ForgeRegistries.ITEMS)
				item.fillItemCategory(CreativeModeTab.TAB_SEARCH, itemPicker);
			return;
		}

		itemPicker.addAll(minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES)
			.search(s.toLowerCase(Locale.ROOT)));
	}

	PaletteBlockShape getCurrentlyRequiredShape() {
		for (PaletteBlockShape paletteBlockShape : selectedEntry.getShapes())
			if (!newMappings.verify(paletteBlockShape))
				return paletteBlockShape;
		return null;
	}

	private boolean isHoveringTinyButton(double mouseX, double mouseY, int buttonX, int buttonY) {
		return mouseX >= buttonX && mouseX < buttonX + 14 && mouseY >= buttonY && mouseY < buttonY + 14;
	}

	private void bgBox(PoseStack ms, int x, int y, int width, int height, Color background) {
		GradientBoxRenderer.renderBox(ms, x, y, width, height, background, NONE, NONE, 1f);
	}

}
