package penguin.serpentine;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import penguin.serpentine.core.example.ExampleConfig;

public class Serpentine implements ModInitializer {
	public static final String MOD_ID = "serpentine";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ExampleConfig CONFIG = null;
	@Override
	public void onInitialize() {
		CONFIG = new ExampleConfig(MOD_ID);
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		penguin.serpentine.core.Serpentine.register(CONFIG);
		LOGGER.info("Hello Fabric world!");
	}
}