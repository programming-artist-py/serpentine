package penguin.serpentine;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import penguin.serpentine.core.Config;
import penguin.serpentine.core.example.ExampleConfig;
import penguin.serpentine.network.ConfigNetworking;
import penguin.serpentine.network.SerpentineServerTicker;

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


		SerpentineServerTicker.register();

        ConfigNetworking.registerPayloadTypes();

        ConfigNetworking.registerC2SHandlers();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			Config cfg = penguin.serpentine.core.Serpentine.getConfig(MOD_ID);
			// Send OP status to the client
			ConfigNetworking.sendS2COpStatus(player, player.hasPermissionLevel(2));
			ConfigNetworking.sendS2CSync(player, cfg, true);
		});

		// penguin.serpentine.core.Serpentine.register(CONFIG);
		// if (penguin.serpentine.core.Serpentine.getConfig(MOD_ID) != null) {
		// 	String message = penguin.serpentine.core.Serpentine.get(MOD_ID, ExampleConfig.class).snakeGreeting;
		// 	LOGGER.info(message);    
		// }
		LOGGER.info("Serpentine Loaded Correctly.");
	}
}