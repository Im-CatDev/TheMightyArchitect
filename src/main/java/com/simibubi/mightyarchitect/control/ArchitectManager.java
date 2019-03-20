package com.simibubi.mightyarchitect.control;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.helpful.FilesHelper;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.palette.PaletteStorage;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.IArchitectPhase;
import com.simibubi.mightyarchitect.control.phase.IDrawBlockHighlights;
import com.simibubi.mightyarchitect.control.phase.IListenForBlockEvents;
import com.simibubi.mightyarchitect.control.phase.IRenderGameOverlay;
import com.simibubi.mightyarchitect.gui.GuiArchitectMenu;
import com.simibubi.mightyarchitect.gui.GuiOpener;
import com.simibubi.mightyarchitect.gui.GuiPalettePicker;
import com.simibubi.mightyarchitect.gui.GuiTextPrompt;
import com.simibubi.mightyarchitect.networking.PacketInstantPrint;
import com.simibubi.mightyarchitect.networking.PacketSender;
import com.simibubi.mightyarchitect.proxy.CombinedClientProxy;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT)
public class ArchitectManager {

	private static ArchitectPhases phase = ArchitectPhases.Empty;
	private static Schematic model = new Schematic();
	private static GuiArchitectMenu menu = new GuiArchitectMenu();

	// Commands

	public static void compose() {
		enterPhase(ArchitectPhases.Composing);
	}

	public static void compose(DesignTheme theme) {
		if (getModel().getGroundPlan() == null) {
			getModel().setGroundPlan(new GroundPlan(theme));
		}
		enterPhase(ArchitectPhases.Composing);
	}

	public static void pauseCompose() {
		status("Composer paused, use /compose to return.");
	}

	public static void unload() {
		enterPhase(ArchitectPhases.Empty);
		resetSchematic();
		menu.setVisible(false);
	}

	public static void design() {
		GroundPlan groundPlan = model.getGroundPlan();
		
		if (groundPlan.isEmpty()) {
			status("Draw some rooms before going to the next step!");
			return;
		}
		
		model.setSketch(groundPlan.theme.getDesignPicker().assembleSketch(groundPlan));
		enterPhase(ArchitectPhases.Previewing);
	}

	public static void createPalette(boolean primary) {
		getModel().startCreatingNewPalette(primary);
		enterPhase(ArchitectPhases.CreatingPalette);
	}

	public static void finishPalette(String name) {
		if (name.isEmpty())
			name = "My Palette";

		PaletteDefinition palette = getModel().getCreatedPalette();
		palette.setName(name);
		PaletteStorage.exportPalette(palette);
		PaletteStorage.loadAllPalettes();

		getModel().applyCreatedPalette();
		status("Your new palette has been saved.");
		enterPhase(ArchitectPhases.Previewing);
	}

	public static void print() {
		if (getModel().getSketch() == null)
			return;

		for (PacketInstantPrint packet : getModel().getPackets()) {
			PacketSender.INSTANCE.sendToServer(packet);
		}

		SchematicHologram.reset();
		status("Printed result into world.");
		unload();
	}

	public static void writeToFile(String name) {
		if (getModel().getSketch() == null)
			return;

		if (name.isEmpty())
			name = "My Build";

		String folderPath = "schematics";

		FilesHelper.createFolderIfMissing(folderPath);
		String filename = FilesHelper.findFirstValidFilename(name, folderPath, "nbt");
		String filepath = folderPath + "/" + filename;

		OutputStream outputStream = null;
		try {
			outputStream = Files.newOutputStream(Paths.get(filepath), StandardOpenOption.CREATE);
			NBTTagCompound nbttagcompound = getModel().writeToTemplate().writeToNBT(new NBTTagCompound());
			CompressedStreamTools.writeCompressed(nbttagcompound, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null)
				IOUtils.closeQuietly(outputStream);
		}
		status("Saved as " + filepath);
	}

	public static void status(String message) {
		Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(message), true);
	}

	public static void pickPalette() {
		if (getModel().getSketch() == null)
			return;

		if (inPhase(ArchitectPhases.CreatingPalette)) {
			getModel().stopPalettePreview();
			enterPhase(ArchitectPhases.Previewing);
		}

		GuiOpener.open(new GuiPalettePicker());
	}

	public static boolean inPhase(ArchitectPhases phase) {
		return ArchitectManager.phase == phase;
	}

	// Phases

	public static void enterPhase(ArchitectPhases newPhase) {
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		phaseHandler.whenExited();
		phaseHandler = newPhase.getPhaseHandler();
		phaseHandler.whenEntered();
		phase = newPhase;
		menu.updateContents();
	}

	public static Schematic getModel() {
		return model;
	}
	
	

	public static ArchitectPhases getPhase() {
		return phase;
	}
	
	// Events

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (Minecraft.getMinecraft().world != null) {
			phase.getPhaseHandler().update();
		}
		menu.onClientTick();
	}

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		if (Minecraft.getMinecraft().world != null) {
			phase.getPhaseHandler().render();
		}
	}

	@SubscribeEvent
	public static void onRightClick(MouseEvent event) {
		if (event.isButtonstate() && Mouse.isButtonDown(event.getButton())) {
			phase.getPhaseHandler().onClick(event.getButton());
		}
	}

	@SubscribeEvent
	public static void onKeyTyped(KeyInputEvent event) {
		if (!Keyboard.getEventKeyState())
			return;
		
		if (CombinedClientProxy.COMPOSE.isPressed()) {
			if (menu.isFocused())
				return;

			menu.updateContents();
			GuiOpener.open(menu);
			menu.setFocused(true);
			menu.setVisible(true);
			return;
		}

		phase.getPhaseHandler().onKey(Keyboard.getEventKey());
	}

	@SubscribeEvent
	public static void onBlockPlaced(PlayerInteractEvent.RightClickBlock event) {
		if (event.getItemStack() == ItemStack.EMPTY) {
			return;
		}

		Item item = event.getItemStack().getItem();
		if (item instanceof ItemBlock) {
			IArchitectPhase phaseHandler = phase.getPhaseHandler();
			if (phaseHandler instanceof IListenForBlockEvents) {
				Vec3d hitVec = event.getHitVec();
				IBlockState stateForPlacement = ((ItemBlock) item).getBlock().getStateForPlacement(event.getWorld(),
						event.getPos(), event.getFace(), (float) hitVec.x, (float) hitVec.y, (float) hitVec.z,
						event.getItemStack().getMetadata(), event.getEntityPlayer(), event.getHand());
				((IListenForBlockEvents) phaseHandler).onBlockPlaced(event.getPos().offset(event.getFace()), stateForPlacement);
			}
		}

	}

	@SubscribeEvent
	public static void onBlockBroken(PlayerInteractEvent.LeftClickBlock event) {
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IListenForBlockEvents) {
			((IListenForBlockEvents) phaseHandler).onBlockBroken(event.getPos());
		}
	}

	@SubscribeEvent
	public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IDrawBlockHighlights) {
			((IDrawBlockHighlights) phaseHandler).onBlockHighlight(event);
		}
	}

	@SubscribeEvent
	public static void onDrawGameOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.HOTBAR)
			return;
		
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IRenderGameOverlay) {
			((IRenderGameOverlay) phaseHandler).renderGameOverlay(event);
		}
		
		menu.drawPassive();
	}

	@SubscribeEvent
	public static void onItemRightClick(PlayerInteractEvent.RightClickBlock event) {
	}

	public static void resetSchematic() {
		model = new Schematic();
	}
	
	public static boolean handleMenuInput(int key, char character) {
		switch (phase) {
		case Composing:
			if (character == 'f') {
				design();
				return true;
			}
			if (character == 'u') {
				unload();
				return true;
			}
		case CreatingPalette:
			if (character == 'f') {
				GuiTextPrompt gui = new GuiTextPrompt(result -> ArchitectManager.finishPalette(result),
						result -> {});
				gui.setButtonTextConfirm("Save and Apply");
				gui.setButtonTextAbort("Cancel");
				gui.setTitle("Enter a name for your Palette:");
				GuiOpener.open(gui);
				return true;
			}
			if (character == 'r') {
				pickPalette();
				return true;
			}
			if (character == 'u') {
				unload();
				return true;
			}
		case Editing:
			break;
		case Empty:
			if (character == 'c') {
				unload();
				return true;
			}
			int ordinal = character - '1';
			if (ordinal < DesignTheme.values().length && ordinal >= 0) {
				compose(DesignTheme.values()[ordinal]);
				return true;
			} else 
				return false;
		case Previewing:
			if (character == 'c') {
				pickPalette();
				return true;
			}
			if (character == 'r') {
				design();
				return false;
			}
			if (character == 'e') {
				compose();
				return true;
			}
			if (character == 's') {
				GuiTextPrompt gui = new GuiTextPrompt(result -> ArchitectManager.writeToFile(result), result -> {});
				gui.setButtonTextConfirm("Save Schematic");
				gui.setButtonTextAbort("Cancel");
				gui.setTitle("Enter a name for your Build:");
				
				GuiOpener.open(gui);
				return true;
			}
			if (character == 'p') {
				if (!Minecraft.getMinecraft().isSingleplayer())
					return false;
				print();
				return true;
			}
			if (character == 'u') {
				unload();
				return true;
			}
		default:
			break;
		}
		return false;
	}
	
	public static Map<String, String> getKeybinds() {
		Map<String, String> keybinds = new HashMap<>();
		
		switch (phase) {
		case Composing:
			keybinds.put("U", "Unload");
			keybinds.put("F", "Finish");
			break;
		case CreatingPalette:
			keybinds.put("U", "Unload");
			keybinds.put("R", "Return to Picker");
			keybinds.put("F", "Save Palette");
			break;
		case Editing:
			break;
		case Empty:
			for (DesignTheme theme : DesignTheme.values()) {
				keybinds.put("" + (theme.ordinal() + 1), theme.getDisplayName());				
			}
			keybinds.put("...", "More Themes coming!");
			keybinds.put("C", "Cancel");
			break;
		case Previewing:
			keybinds.put("C", "Choose a Palette");
			keybinds.put("R", "Re-Roll Designs");
			keybinds.put("E", "Edit Ground Plan");
			keybinds.put("S", "Save as Schematic");
			if (Minecraft.getMinecraft().isSingleplayer())
				keybinds.put("P", "Print blocks into world");
			keybinds.put("U", "Unload");
			break;
		default:
			break;
		}
		
		return keybinds;
	}

}
