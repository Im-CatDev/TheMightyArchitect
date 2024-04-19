package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.foundation.utility.Color;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class DesignExporterScreen extends SimpleScreen {

	List<NewSuperIconButton> designLayerRow = new ArrayList<>();
	List<NewSuperIconButton> designTypeRow = new ArrayList<>();
	List<NewSuperIconButton> specialParameterRow = new ArrayList<>();

	DesignLayer selectedLayer;
	DesignType selectedType;
	int selectedParameter;

	Runnable deferredCallback;

	private BlockPos anchorPos;

	public DesignExporterScreen(BlockPos anchorPos, boolean resaving) {
		this.anchorPos = anchorPos;
		selectedLayer = DesignExporter.layer;
		selectedType = DesignExporter.type;
		selectedParameter = DesignExporter.designParameter;
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack ms = graphics.pose();
		int w = 72 * Math.max(4, Math.max(designLayerRow.size(), designTypeRow.size())) + 4;
		int y = topLeftY + 3;
		MutableComponent header = Component.literal("Category");
		GradientBoxRenderer.renderBox(ms, topLeftX + (128 - w / 2), y, w, 50, new Color(0, 0, 0, 50),
			Color.TRANSPARENT_BLACK, Color.TRANSPARENT_BLACK, 1f);
		drawCenteredString(ms, font, header, topLeftX + 128, y + 3, 0x9999aa);

		y += 60;
		header = Component.literal("Type of Design");
		GradientBoxRenderer.renderBox(ms, topLeftX + (128 - w / 2), y, w, 50, new Color(0, 0, 0, 50),
			Color.TRANSPARENT_BLACK, Color.TRANSPARENT_BLACK, 1f);
		drawCenteredString(ms, font, header, topLeftX + 128, y + 3, 0x9999aa);

		if (!selectedType.hasAdditionalData())
			return;

		y += 60;
		header = Component.literal(selectedType.getAdditionalDataName());
		GradientBoxRenderer.renderBox(ms, topLeftX + (128 - w / 2), y, w, specialParameterRow.size() > 10 ? 56 : 36,
			new Color(0, 0, 0, 50), Color.TRANSPARENT_BLACK, Color.TRANSPARENT_BLACK, 1f);
		drawCenteredString(ms, font, header, topLeftX + 128, y + 3, 0x9999aa);
	}

	@Override
	public void tick() {
		super.tick();
		if (deferredCallback != null) {
			deferredCallback.run();
			deferredCallback = null;
		}
	}

	@Override
	protected void init() {
		setWindowSize(256, 200);
		super.init();

		widgets.removeAll(designLayerRow);
		designLayerRow.clear();

		DesignTheme theme = DesignExporter.theme;
		List<DesignLayer> layers = theme.getLayers();

		int buttonWidth = 68;
		int y = topLeftY + 20;
		int x = topLeftX + sWidth / 2;
		x -= (layers.size() * (buttonWidth + 4) - 4) / 2;

		for (DesignLayer layer : layers) {
			NewSuperIconButton button =
				new NewSuperIconButton(x, y, buttonWidth, layer.getIcon(), Component.literal(layer.getDisplayName()));
			if (layer == selectedLayer)
				button.highlighted = true;

			button.setCallback((mx, my) -> {
				if (selectedLayer == layer)
					return;
				selectedLayer = layer;
				for (NewSuperIconButton other : designLayerRow)
					other.highlighted = other == button;
				deferredCallback = this::initTypeRow;
			});

			designLayerRow.add(button);
			widgets.add(button);
			x += buttonWidth + 4;
		}

		initTypeRow();
	}

	private void initTypeRow() {
		widgets.removeAll(designTypeRow);
		designTypeRow.clear();

		DesignTheme theme = DesignExporter.theme;
		List<DesignType> types = new ArrayList<>(theme.getTypes());
		if (selectedLayer == DesignLayer.ROOFING)
			types.retainAll(DesignType.roofTypes());
		else
			types.removeAll(DesignType.roofTypes());

		// Fallback if previous type is not selectable anymore
		if (!types.contains(selectedType)) {
			selectedType = DesignType.WALL;
			if (selectedLayer == DesignLayer.ROOFING) {
				for (DesignType dt : DesignType.roofTypes()) {
					if (!types.contains(dt))
						continue;
					selectedType = dt;
					break;
				}
			}
		}

		int buttonWidth = 68;
		int y = topLeftY + 80;
		int x = topLeftX + sWidth / 2;
		x -= (types.size() * (buttonWidth + 4) - 4) / 2;

		for (DesignType type : types) {
			NewSuperIconButton button =
				new NewSuperIconButton(x, y, buttonWidth, type.getIcon(), Component.literal(type.getDisplayName()));
			if (type == selectedType)
				button.highlighted = true;

			button.setCallback((mx, my) -> {
				if (selectedType == type)
					return;
				selectedType = type;
				for (NewSuperIconButton other : designTypeRow)
					other.highlighted = other == button;
				deferredCallback = this::initParamsRow;
			});

			designTypeRow.add(button);
			widgets.add(button);
			x += buttonWidth + 4;
		}

		initParamsRow();
	}

	private void initParamsRow() {
		widgets.removeAll(specialParameterRow);
		specialParameterRow.clear();

		if (!selectedType.hasAdditionalData())
			return;

		selectedParameter = Mth.clamp(selectedParameter, selectedType.getMinSize(), selectedType.getMaxSize());
		int increment = 1;
		if (selectedType == DesignType.GABLE_ROOF) {
			increment = 2;
			if (selectedParameter % 2 == 0)
				selectedParameter++;
		}

		List<Integer> validOptions = new ArrayList<>();
		List<Component> labels = new ArrayList<>();
		for (int i = selectedType.getMinSize(); i <= selectedType.getMaxSize(); i += increment) {
			validOptions.add(i);
			labels.add(Component.literal(selectedType.formatData(i)));
		}

		int maxWidth = labels.stream()
			.mapToInt(font::width)
			.max()
			.orElse(0);
		int buttonWidth = maxWidth + 9;
		int y = topLeftY + 140;
		int rowWidth = ((labels.size() > 10 ? labels.size() / 2 : labels.size()) * (buttonWidth + 4) - 4);
		int x = topLeftX + 128 - rowWidth / 2;

		for (int i = 0; i < labels.size(); i++) {
			if (labels.size() > 10 && i == labels.size() / 2) {
				y += 20;
				x = topLeftX + 128 - rowWidth / 2;
			}

			Component label = labels.get(i);
			NewSuperIconButton button = new NewSuperIconButton(x, y, buttonWidth, null, label);
			int parameter = validOptions.get(i)
				.intValue();
			if (parameter == selectedParameter)
				button.highlighted = true;

			button.setCallback((mx, my) -> {
				if (parameter == selectedParameter)
					return;
				selectedParameter = parameter;
				for (NewSuperIconButton other : specialParameterRow)
					other.highlighted = other == button;
			});

			specialParameterRow.add(button);
			widgets.add(button);
			x += buttonWidth + 4;
		}
	}

//	private void initAdditionalDataScrollArea(DesignType type) {
//		if (widgets.contains(scrollAreaAdditionalData))
//			widgets.remove(scrollAreaAdditionalData);
//
//		if (type.hasAdditionalData()) {
//
//			additionalDataKey = type.getAdditionalDataName();
//
//			if (type.hasSizeData()) {
//
//				if (type == DesignType.ROOF) {
//					if (additionalDataValue % 2 == 0)
//						additionalDataValue++;
//				}
//				if (additionalDataValue < type.getMinSize())
//					additionalDataValue = type.getMinSize();
//				if (additionalDataValue > type.getMaxSize())
//					additionalDataValue = type.getMaxSize();
//				labelAdditionalData.setText(additionalDataValue + "m");
//
//				if (type == DesignType.ROOF) {
//					int min = (type.getMinSize() - 1) / 2;
//					int max = (type.getMaxSize() - 1) / 2;
//
//					scrollAreaAdditionalData = new ScrollInput(topLeftX + 93, topLeftY + 85, 90, 14).withRange(min, max)
//						.setState((additionalDataValue - 1) / 2)
//						.writingTo(labelAdditionalData)
//						.calling(position -> {
//							additionalDataValue = position * 2 + 1;
//							labelAdditionalData.setText(position * 2 + 1 + "m");
//						});
//					labelAdditionalData.setText(additionalDataValue + "m");
//
//				} else {
//					int min = type.getMinSize();
//					int max = type.getMaxSize();
//
//					scrollAreaAdditionalData =
//						new ScrollInput(topLeftX + 93, topLeftY + 85, 90, 14).withRange(min, max + 1)
//							.setState(additionalDataValue)
//							.writingTo(labelAdditionalData)
//							.calling(position -> {
//								additionalDataValue = position;
//								labelAdditionalData.setText(position + "m");
//							});
//				}
//
//			} else if (type.hasSubtypes()) {
//				if (additionalDataValue == -1)
//					additionalDataValue = 0;
//
//				List<String> subtypeOptions = type.getSubtypeOptions();
//				if (additionalDataValue >= subtypeOptions.size())
//					additionalDataValue = 0;
//
//				labelAdditionalData.setText(subtypeOptions.get(additionalDataValue));
//				scrollAreaAdditionalData =
//					new SelectionScrollInput(topLeftX + 93, topLeftY + 85, 90, 14).forOptions(subtypeOptions)
//						.writingTo(labelAdditionalData)
//						.setState(additionalDataValue)
//						.calling(p -> additionalDataValue = p);
//			}
//
//			scrollAreaAdditionalData.titled(additionalDataKey);
//			widgets.add(scrollAreaAdditionalData);
//
//		} else {
//
//			additionalDataValue = -1;
//			additionalDataKey = "";
//			labelAdditionalData.setText("");
//			scrollAreaAdditionalData = null;
//
//		}
//	}
	@Override
	public void removed() {
		DesignExporter.layer = selectedLayer;
		DesignExporter.type = selectedType;
		DesignExporter.designParameter = selectedParameter;
		DesignExporter.exportDesign(minecraft.level, anchorPos);
	}

}
