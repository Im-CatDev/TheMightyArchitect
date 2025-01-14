package com.simibubi.mightyarchitect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

@EventBusSubscriber(bus = Bus.FORGE)
@Mod(TheMightyArchitect.ID)
public class TheMightyArchitect {

	public static final String ID = "mightyarchitect";
	public static final String NAME = "The Mighty Architect";
	public static final String VERSION = "0.6";

	public static TheMightyArchitect instance;
	public static Logger logger = LogManager.getLogger();

	public TheMightyArchitect() {
		FMLJavaModLoadingContext.get()
			.getModEventBus()
			.addListener(this::init);
	}

	private void init(final FMLCommonSetupEvent event) {
		AllPackets.registerPackets();
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

	@EventBusSubscriber(bus = Bus.MOD)
	public static class RegistryListener {

		@SubscribeEvent
		public static void register(RegisterEvent event) {
			AllBlocks.registerBlocks(event);
			AllBlocks.registerItems(event);
		}

	}

}