package com.simibubi.mightyarchitect.control;

import com.simibubi.mightyarchitect.AllBlocks;
import com.simibubi.mightyarchitect.AllPackets;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockShape;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.networking.SetHotbarItemPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ArchitectKits {

	public static void ExporterToolkit() {
		clearHotbarItem(0);
		setHotbarBlock(1, AllBlocks.DESIGN_ANCHOR.get());
		setHotbarBlock(2, AllBlocks.SLICE_MARKER.get());
		clearHotbarItem(3);
		setHotbarBlock(4, Palette.CLEAR);
		setHotbarBlock(5, Palette.INTERIOR_FLOOR);
		setHotbarBlock(6, Palette.EXTERIOR_FLOOR);
		clearHotbarItem(7);
		clearHotbarItem(8);
	}

	public static void FoundationToolkit() {
		setHotbarBlock(0, Palette.FOUNDATION_FILL);
		setHotbarBlock(1, Palette.FOUNDATION_FILL, PaletteBlockShape.STAIRS);
		setHotbarBlock(2, Palette.FOUNDATION_FILL, PaletteBlockShape.SLAB);
		setHotbarBlock(3, Palette.FOUNDATION_EDGE);
		setHotbarBlock(4, Palette.FOUNDATION_EDGE, PaletteBlockShape.STAIRS);
		setHotbarBlock(5, Palette.FOUNDATION_EDGE, PaletteBlockShape.SLAB);
		setHotbarBlock(6, Palette.FOUNDATION_EDGE, PaletteBlockShape.WALL);
		setHotbarBlock(7, Palette.FOUNDATION_WINDOW);
		setHotbarBlock(8, Palette.FOUNDATION_DECO);
	}

	public static void RegularToolkit() {
		setHotbarBlock(0, Palette.STANDARD_FILL);
		setHotbarBlock(1, Palette.STANDARD_FILL, PaletteBlockShape.STAIRS);
		setHotbarBlock(2, Palette.STANDARD_FILL, PaletteBlockShape.SLAB);
		setHotbarBlock(3, Palette.STANDARD_EDGE);
		setHotbarBlock(4, Palette.STANDARD_EDGE, PaletteBlockShape.STAIRS);
		setHotbarBlock(5, Palette.STANDARD_EDGE, PaletteBlockShape.SLAB);
		setHotbarBlock(6, Palette.STANDARD_WINDOW);
		setHotbarBlock(7, Palette.STANDARD_DECO);
		setHotbarBlock(8, Palette.LIGHT_POST);
	}

	public static void RoofingToolkit() {
		setHotbarBlock(0, Palette.ROOF_FILL);
		setHotbarBlock(1, Palette.ROOF_FILL, PaletteBlockShape.STAIRS);
		setHotbarBlock(2, Palette.ROOF_FILL, PaletteBlockShape.SLAB);
		setHotbarBlock(3, Palette.ROOF_FILL, PaletteBlockShape.WALL);
		setHotbarBlock(4, Palette.ROOF_EDGE);
		setHotbarBlock(5, Palette.ROOF_EDGE, PaletteBlockShape.STAIRS);
		setHotbarBlock(6, Palette.ROOF_EDGE, PaletteBlockShape.SLAB);
		setHotbarBlock(7, Palette.ROOF_EDGE, PaletteBlockShape.WALL);
		setHotbarBlock(8, Palette.ROOF_DECO);
	}

	private static void clearHotbarItem(int slot) {
		setHotbarItem(slot, ItemStack.EMPTY);
	}

	private static void setHotbarItem(int slot, Item item) {
		setHotbarItem(slot, new ItemStack(item));
	}

	private static void setHotbarItem(int slot, ItemStack stack) {
		AllPackets.channel.sendToServer(new SetHotbarItemPacket(slot, stack));
	}

	private static void setHotbarBlock(int slot, Block block) {
		setHotbarItem(slot, block.asItem());
	}

	private static void setHotbarBlock(int slot, Palette palette) {
		setHotbarBlock(slot, palette, PaletteBlockShape.REGULAR);
	}

	private static void setHotbarBlock(int slot, Palette palette, PaletteBlockShape shape) {
		PaletteDefinition paletteDefinition = DesignExporter.theme.getDefaultPalette();
		ItemStack stack = new ItemStack(paletteDefinition.get(palette, shape)
			.getBlock()
			.asItem());

		setHotbarItem(slot,
			stack.setHoverName(Lang
				.text(ChatFormatting.RESET + "" + ChatFormatting.GOLD + palette.getDisplayName() + ChatFormatting.WHITE
					+ " (" + ChatFormatting.GRAY + stack.getHoverName()
						.getString()
					+ ChatFormatting.WHITE + ")")
				.component()));
	}

}
