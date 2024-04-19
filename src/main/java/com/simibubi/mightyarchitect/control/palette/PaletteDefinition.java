package com.simibubi.mightyarchitect.control.palette;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.foundation.utility.BlockHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class PaletteDefinition {

	private Map<Palette, PaletteMapping> definition;

	private String name;
	private BlockState clear;
	private static PaletteDefinition defaultPalette;

	public static PaletteDefinition defaultPalette() {
		if (defaultPalette == null) {
			defaultPalette = new PaletteDefinition("Standard Palette");
			defaultPalette

				.put(Palette.FOUNDATION_EDGE, Blocks.STONE_BRICKS)
				.put(Palette.FOUNDATION_FILL, Blocks.COBBLESTONE)
				.put(Palette.FOUNDATION_DECO, Blocks.BASALT)
				.put(Palette.FOUNDATION_WINDOW, Blocks.IRON_BARS)

				.put(Palette.STANDARD_EDGE,
					new SimplePaletteMapping(Blocks.SPRUCE_WOOD.defaultBlockState(),
						Blocks.DARK_OAK_PLANKS.defaultBlockState()))

				.put(Palette.STANDARD_FILL,
					new GradientPaletteMapping(List.of(new SimplePaletteMapping(Blocks.DARK_OAK_PLANKS.defaultBlockState()),
						new SimplePaletteMapping(Blocks.SPRUCE_PLANKS.defaultBlockState()),
						new SimplePaletteMapping(Blocks.OAK_PLANKS.defaultBlockState())), List.of(10, 5, 3)))

				.put(Palette.STANDARD_DECO, Blocks.STRIPPED_SPRUCE_LOG)
				.put(Palette.STANDARD_WINDOW, Blocks.GLASS_PANE)

				.put(Palette.ROOF_EDGE, Blocks.BRICKS)
				.put(Palette.ROOF_FILL, Blocks.GRANITE)
				.put(Palette.ROOF_DECO, Blocks.OAK_LOG)

				.put(Palette.HEAVY_POST, Blocks.MUD_BRICK_WALL)
				.put(Palette.LIGHT_POST, Blocks.SPRUCE_FENCE)
				.put(Palette.PANEL, Blocks.SPRUCE_TRAPDOOR)
				.put(Palette.INTERIOR_FLOOR, Blocks.OAK_PLANKS)
				.put(Palette.EXTERIOR_FLOOR, Blocks.TUFF)
				.put(Palette.CLEAR, Blocks.BARRIER);

		}
		return defaultPalette;
	}

	public PaletteDefinition clone() {
		PaletteDefinition clone = new PaletteDefinition(name);
		clone.clear = defaultPalette().clear();
		clone.definition = new HashMap<>(defaultPalette().getDefinition());
		definition.forEach((key, value) -> clone.definition.put(key, value));
		return clone;
	}

	public PaletteDefinition(String name) {
		definition = new HashMap<>();
		put(Palette.CLEAR, Blocks.BARRIER.defaultBlockState());
		this.name = name;
	}

	public PaletteDefinition put(Palette key, Block block) {
		return put(key, block.defaultBlockState());
	}

	public PaletteDefinition put(Palette key, BlockState block) {
		if (block.getBlock() instanceof TrapDoorBlock)
			block = block.setValue(TrapDoorBlock.FACING, Direction.SOUTH)
				.setValue(TrapDoorBlock.OPEN, true);
		return put(key, new SimplePaletteMapping(block));
	}

	public PaletteDefinition put(Palette key, PaletteMapping mapping) {
		definition.put(key, mapping);
		return this;
	}

	public Map<Palette, PaletteMapping> getDefinition() {
		return definition;
	}

	public BlockState clear() {
		if (clear == null)
			clear = get(Palette.CLEAR, PaletteBlockShape.REGULAR);
		return clear;
	}

	public BlockState get(PaletteBlockInfo paletteInfo) {
		BlockState state = paletteInfo.applyOrientations(definition.get(paletteInfo.palette)
			.provideWithContext(paletteInfo.blockShape, paletteInfo.pos, ArchitectManager.getModel().random));

		Collection<Property<?>> properties = state.getProperties();

		for (Property<?> property : properties) {
			if (property instanceof DirectionProperty) {
				Direction facing = (Direction) state.getValue(property);
				if (facing.getAxis() == Axis.Y)
					continue;

				if ((paletteInfo.mirrorZ && facing.getAxis() != Axis.Z)
					|| (paletteInfo.mirrorX && facing.getAxis() != Axis.X))
					state = state.setValue((DirectionProperty) property, facing.getOpposite());
			}
		}

		return state;
	}

	public BlockState get(Palette key, PaletteBlockShape shape) {
		return definition.get(key)
			.provide(shape);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public CompoundTag writeToNBT(CompoundTag compound) {
		compound = (compound == null) ? new CompoundTag() : compound;
		CompoundTag palette = new CompoundTag();
		palette.putString("Name", getName());
		for (Palette key : Palette.values())
			palette.put(key.name(), definition.get(key)
				.toNBT());
		compound.put("Palette", palette);
		return compound;
	}

	public static PaletteDefinition fromNBT(CompoundTag compound) {
		PaletteDefinition palette = defaultPalette().clone();

		if (compound == null)
			return palette;
		if (!compound.contains("Palette"))
			return palette;

		CompoundTag paletteTag = compound.getCompound("Palette");
		palette.name = paletteTag.getString("Name");
		for (Palette key : Palette.values())
			if (paletteTag.contains(key.name()))
				palette.put(key, NbtUtils.readBlockState(BlockHelper.lookup(), paletteTag.getCompound(key.name())));

		return palette;
	}

	public String getDuplicates() {
		for (Palette key : definition.keySet()) {
			Pair<Palette, PaletteBlockShape> other = scan(definition.get(key)
				.provide(PaletteBlockShape.REGULAR));
			if (other == null)
				continue;
			if (key != other.getKey())
				return key.getDisplayName() + " = " + other.getKey()
					.getDisplayName();
		}
		return "";
	}

	public boolean hasDuplicates() {
		return !getDuplicates().isEmpty();
	}

	public Pair<Palette, PaletteBlockShape> scan(BlockState state) {
		if (state.getBlock() == Blocks.AIR)
			return null;

		for (Entry<Palette, PaletteMapping> entry : definition.entrySet()) {
			PaletteBlockShape shape = entry.getValue()
				.find(state);
			if (shape != null)
				return Pair.of(entry.getKey(), shape);
		}

		return null;
	}

}
