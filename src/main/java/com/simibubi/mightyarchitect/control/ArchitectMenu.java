package com.simibubi.mightyarchitect.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.ThemeStorage;
import com.simibubi.mightyarchitect.control.design.ThemeValidator;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.gui.GuiEditTheme;
import com.simibubi.mightyarchitect.gui.GuiOpener;
import com.simibubi.mightyarchitect.gui.GuiTextPrompt;

import net.minecraft.client.Minecraft;

public class ArchitectMenu {

	public static boolean handleMenuInput(int key, char c) {
		switch (ArchitectManager.getPhase()) {

		case Composing:
			switch (c) {

			case 'f':
				ArchitectManager.design();
				return true;

			case 'u':
				ArchitectManager.unload();
				return true;
			}
			break;

		case CreatingPalette:
			switch (c) {

			case 'f':
				GuiTextPrompt gui = new GuiTextPrompt(result -> ArchitectManager.finishPalette(result), result -> {
				});
				gui.setButtonTextConfirm("Save and Apply");
				gui.setButtonTextAbort("Cancel");
				gui.setTitle("Enter a name for your Palette:");
				GuiOpener.open(gui);

			case 'r':
				ArchitectManager.design();
				return true;

			case 'c':
				ArchitectManager.pickPalette();
				return true;

			case 'u':
				ArchitectManager.unload();
				return true;
			}
			break;

		case Empty:
			switch (c) {
			
			case 'c':
				ArchitectManager.unload();
				return true;
				
			case 'm':
				ArchitectManager.manageThemes();
				return false;
				
			default:
				int index = c - '1';
				ThemeStorage.reloadExternal();
				List<DesignTheme> themes = ThemeStorage.getAllThemes();
				if (index < themes.size() && index >= 0) {
					ArchitectManager.compose(themes.get(index));
					return true;
				}
			}
			break;

		case Previewing:
			boolean test = ArchitectManager.testRun;
			switch (c) {

			case 'c':
				ArchitectManager.pickPalette();
				return true;

			case 'r':
				ArchitectManager.design();
				return false;

			case 'e':
				ArchitectManager.compose();
				return true;

			case 's':
				if (test) return false;
				GuiTextPrompt gui = new GuiTextPrompt(result -> ArchitectManager.writeToFile(result), result -> {
				});
				gui.setButtonTextConfirm("Save Schematic");
				gui.setButtonTextAbort("Cancel");
				gui.setTitle("Enter a name for your Build:");

				GuiOpener.open(gui);
				return true;

			case 'p':
				if (test) return false;
				if (!Minecraft.getMinecraft().isSingleplayer())
					return false;
				ArchitectManager.print();
				return true;

			case 'u':
				ArchitectManager.unload();
				return true;
			}
			break;

		case ManagingThemes:
			switch (c) {
			case 'n':
				ArchitectManager.createTheme();
				return true;
			case 'e':
				ArchitectManager.enterPhase(ArchitectPhases.ListForEdit);
				return false;
			case 'c':
				ArchitectManager.unload();
				return true;
			}
			break;

		case ListForEdit:
			switch (c) {

			case 'c':
				ArchitectManager.enterPhase(ArchitectPhases.ManagingThemes);
				return false;

			default:
				int index = c - '1';
				List<DesignTheme> themes = ThemeStorage.getImported();
				if (index < themes.size() && index >= 0) {
					ArchitectManager.editTheme(themes.get(index));
					return false;
				}
			}
			break;

		case EditingThemes:
			switch (c) {
			
			case '1':
				ArchitectKits.ExporterToolkit();
				return true;
				
			case '2':
				ArchitectKits.FoundationToolkit();
				return true;
				
			case '3':
				ArchitectKits.RegularToolkit();
				return true;
				
			case '4':
				ArchitectKits.RoofingToolkit();
				return true;

			case 'c':
				ArchitectManager.pickScanPalette();
				return true;
			
			case 'e':
				GuiOpener.open(new GuiEditTheme());
				return true;
				
			case 'r':
				DesignExporter.theme.clearDesigns();
				ThemeStorage.exportTheme(DesignExporter.theme);
				ArchitectManager.testRun = true;
				ArchitectManager.compose(DesignExporter.theme);
				return true;

			case 'f':
				DesignExporter.theme.clearDesigns();
				ThemeStorage.exportTheme(DesignExporter.theme);
				ArchitectManager.unload();
				return true;

			case 'v':
				ThemeValidator.check(DesignExporter.theme);
				return true;
			}
			break;

		default:
			break;
		}
		return false;
	}

	public static KeyBindList getKeybinds() {
		KeyBindList keybinds = new KeyBindList();

		switch (ArchitectManager.getPhase()) {
		
		case Composing:
			keybinds.put("F", "Finish");
			keybinds.lineBreak();
			keybinds.put("U", "Unload");
			break;
			
		case CreatingPalette:
			keybinds.put("F", "Save Palette");
			keybinds.lineBreak();
			keybinds.put("R", "Re-roll Designs");
			keybinds.put("C", "Return to Picker");
			keybinds.put("U", "Unload");
			break;
			
		case Empty:
			List<DesignTheme> allThemes = ThemeStorage.getAllThemes();
			for (DesignTheme theme : allThemes) {
				keybinds.put("" + (allThemes.indexOf(theme) + 1), theme.getDisplayName());
			}
			keybinds.lineBreak();
			keybinds.put("M", "Manage Themes...");
			keybinds.put("C", "Cancel");
			break;
			
		case Previewing:
			keybinds.put("E", "Edit Ground Plan");
			keybinds.put("R", "Re-Roll Designs");
			keybinds.put("C", "Choose a Palette");
			keybinds.lineBreak();
			
			if (!ArchitectManager.testRun) {
				keybinds.put("S", "Save as Schematic");
				if (Minecraft.getMinecraft().isSingleplayer())
					keybinds.put("P", "Print blocks into world");
				keybinds.lineBreak();
				keybinds.put("U", "Unload");				
			} else {
				keybinds.put("U", "Exit Test Run");								
			}
			
			break;
			
		case ManagingThemes:
			keybinds.put("N", "Create new Theme");
			keybinds.put("E", "Edit an existing Theme");
			keybinds.lineBreak();
			keybinds.put("C", "Cancel");
			break;
			
		case ListForEdit:
			allThemes = ThemeStorage.getImported();
			for (DesignTheme theme : allThemes) {
				keybinds.put("" + (allThemes.indexOf(theme) + 1), theme.getDisplayName());
			}
			keybinds.lineBreak();
			keybinds.put("C", "Cancel");
			break;
			
		case EditingThemes:
			keybinds.put("1", "Equip Exporter Tools");
			keybinds.put("2", "Equip Foundation Blocks");
			keybinds.put("3", "Equip Regular Blocks");
			keybinds.put("4", "Equip Roofing Blocks");
			keybinds.lineBreak();
			keybinds.put("C", "Change default palette");
			keybinds.put("E", "Edit Theme settings");
			keybinds.lineBreak();
			keybinds.put("V", "Validate Theme");
			keybinds.put("R", "Run a Test");
			keybinds.put("F", "Finish editing");
			break;
		default:
			break;
		}

		return keybinds;
	}

	public static class KeyBindList {
		private List<String> keys;
		private Map<String, String> descriptions;

		public KeyBindList() {
			keys = new ArrayList<>();
			descriptions = new HashMap<>();
		}

		public void put(String key, String description) {
			keys.add(key);
			descriptions.put(key, description);
		}

		public void lineBreak() {
			put("", "");
		}

		public void foreach(BiConsumer<String, String> biConsumer) {
			keys.forEach(key -> biConsumer.accept(key, descriptions.get(key)));
		}

		public float size() {
			float size = 0;
			for (String key : keys) {
				size += (key.isEmpty())? 0.5f : 1;
			}
			return size;
		}
		
		public List<String> getKeys() {
			return keys;
		}
		
		public String get(String key) {
			return descriptions.get(key);
		}

	}

}