package com.simibubi.mightyarchitect.gui.widgets;

import java.util.function.Consumer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ScrollInput extends AbstractSimiWidget {

	protected Consumer<Integer> onScroll;
	protected int state;
	protected Label displayLabel;
	protected ITextComponent title = new StringTextComponent("Choose an Option");
	protected ITextComponent scrollToModify = new StringTextComponent("Scroll to Modify");
	protected int min, max;
	protected int shiftStep;

	public ScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		state = 0;
		min = 0;
		max = 1;
		shiftStep = 5;
	}

	public ScrollInput withRange(int min, int max) {
		this.min = min;
		this.max = max;
		return this;
	}

	public ScrollInput calling(Consumer<Integer> onScroll) {
		this.onScroll = onScroll;
		return this;
	}

	public ScrollInput removeCallback() {
		this.onScroll = null;
		return this;
	}

	public ScrollInput titled(String title) {
		this.title = new StringTextComponent(title);
		updateTooltip();
		return this;
	}

	public ScrollInput writingTo(Label label) {
		this.displayLabel = label;
		writeToLabel();
		return this;
	}

	public int getState() {
		return state;
	}

	public ScrollInput setState(int state) {
		this.state = state;
		clampState();
		updateTooltip();
		if (displayLabel != null)
			writeToLabel();
		return this;
	}

	public ScrollInput withShiftStep(int step) {
		shiftStep = step;
		return this;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (!isHovered)
			return false;

		int priorState = state;
		boolean shifted = Screen.hasShiftDown();
		int step = (int) Math.signum(delta);

		state += step;
		if (shifted)
			state -= state % shiftStep;

		clampState();

		if (priorState != state)
			onChanged();

		return priorState != state;
	}

	protected void clampState() {
		if (state >= max)
			state = max - 1;
		if (state < min)
			state = min;
	}

	public void onChanged() {
		if (displayLabel != null)
			writeToLabel();
		if (onScroll != null)
			onScroll.accept(state);
		updateTooltip();
	}

	protected void writeToLabel() {
		displayLabel.text = new StringTextComponent(String.valueOf(state));
	}

	protected void updateTooltip() {
		toolTip.clear();
		toolTip.add(title.plainCopy()
			.withStyle(TextFormatting.BLUE));
		toolTip.add(scrollToModify.plainCopy()
			.withStyle(TextFormatting.ITALIC, TextFormatting.DARK_GRAY));
	}

}
