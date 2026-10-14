package net.shadow.world_sync;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import net.shadow.world_sync.gamerules.WorldSyncGameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldSync implements ModInitializer {
	public static final String MOD_ID = "world-sync";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        WorldSyncGameRules.register();
		LOGGER.info("Your souls have been bound - The Artist");
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
