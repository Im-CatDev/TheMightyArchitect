package com.simibubi.mightyarchitect.control.weathering;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.mightyarchitect.control.TemplateBlockAccess;

public class WeathererCollection implements StepwiseWeathering {

	List<StepwiseWeathering> weatherers = new ArrayList<>();

	public void add(StepwiseWeathering weatherer) {
		weatherers.add(weatherer);
	}
	
	@Override
	public void step(TemplateBlockAccess level) {
		weatherers.forEach(w -> w.step(level));
		weatherers.removeIf(StepwiseWeathering::isDone);
	}
	
	@Override
	public boolean isDone() {
		return weatherers.isEmpty();
	}

}
