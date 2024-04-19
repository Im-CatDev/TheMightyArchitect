package com.simibubi.mightyarchitect.control.weathering;

import java.util.List;

import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.control.phase.PhaseBase;

public class PhaseWeathering extends PhaseBase {

	private WeathererCollection foliageTicker;
	private WeathererCollection destructionTicker;

	private WeathererCollection foliage() {
		if (foliageTicker != null)
			return foliageTicker;
		foliageTicker = new WeathererCollection();
		for (int i = 0; i < 4; i++)
			foliageTicker.add(new ClimbingFoliage());
		foliageTicker.add(new TreeFeatureFoliage());
		return foliageTicker;
	}

	private WeathererCollection destruction() {
		if (destructionTicker != null)
			return destructionTicker;
		destructionTicker = new WeathererCollection();
		destructionTicker.add(new BlockShapeWeatherer());
		destructionTicker.add(new HolePunchingWeatherer());
		destructionTicker.add(new CornerErosionWeatherer());
		destructionTicker.add(new FloatingBlockCleanup());
		return destructionTicker;
	}

	@Override
	public boolean onScroll(int amount) {
		TemplateBlockAccess schematic = getModel().getMaterializedSketch();

		schematic.localMode(true);
		if (amount > 0) {
			foliage().step(schematic);
			int climbingFoliage = 0;
			for (StepwiseWeathering weathering : foliage().weatherers)
				if (weathering instanceof ClimbingFoliage)
					climbingFoliage++;
			if (climbingFoliage < 2)
				foliageTicker.add(new ClimbingFoliage());
		}
		if (amount < 0)
			destruction().step(schematic);
		schematic.localMode(false);

		MightyClient.renderer.update();
		return true;
	}

	@Override
	public void update() {}

	@Override
	public void whenEntered() {
		MightyClient.renderer.display(getModel());
	}

	@Override
	public void whenExited() {
		foliageTicker = null;
		destructionTicker = null;
	}

	@Override
	public List<String> getToolTip() {
		return List.of("Scroll up to Grow Foliage", "Scroll down to Destroy");
	}

}
