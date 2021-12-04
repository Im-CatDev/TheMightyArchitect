package com.simibubi.mightyarchitect.foundation.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;

public class FilesHelper {

	public static void createFolderIfMissing(String name) {
		if (!Files.isDirectory(Paths.get(name))) {
			try {
				Files.createDirectory(Paths.get(name));
			} catch (IOException e) {
				TheMightyArchitect.logger.warn("Could not create Folder: " + name);
			}
		}
	}

	public static String findFirstValidFilename(String name, String folderPath, String extension) {
		int index = 0;
		String filename;
		String filepath;
		do {
			filename = slug(name) + ((index == 0) ? "" : "_" + index) + "." + extension;
			index++;
			filepath = folderPath + "/" + filename;
		} while (Files.exists(Paths.get(filepath)));
		return filename;
	}

	public static String slug(String name) {
		return name.toLowerCase().replace(' ', '_');
	}

	public static boolean saveTagCompoundAsJson(CompoundNBT compound, String path) {
		try {
			Files.deleteIfExists(Paths.get(path));
			JsonWriter writer = new JsonWriter(Files.newBufferedWriter(Paths.get(path), StandardOpenOption.CREATE));
			writer.setIndent("  ");
			Streams.write(new JsonParser().parse(compound.toString()), writer);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	

	public static boolean saveTagCompoundAsJsonCompact(CompoundNBT compound, String path) {
		try {
			Files.deleteIfExists(Paths.get(path));
			JsonWriter writer = new JsonWriter(Files.newBufferedWriter(Paths.get(path), StandardOpenOption.CREATE));
			Streams.write(new JsonParser().parse(compound.toString()), writer);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
	}

	public static CompoundNBT loadJsonNBT(InputStream inputStream) {
		try {
			JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
			reader.setLenient(true);
			JsonElement element = Streams.parse(reader);
			reader.close();
			inputStream.close();
			return JsonToNBT.parseTag(element.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static CompoundNBT loadJsonResourceAsNBT(String filepath) {
		return loadJsonNBT(TheMightyArchitect.class.getClassLoader().getResourceAsStream(filepath));
	}

	public static CompoundNBT loadJsonAsNBT(String filepath) {
		try {
			return loadJsonNBT(Files.newInputStream(Paths.get(filepath), StandardOpenOption.READ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
