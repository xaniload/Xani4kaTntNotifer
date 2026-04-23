package dev.xani.tntcartcatguard;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TntCartCatGuardMod implements ModInitializer {
	public static final String MOD_ID = "tntcartcatguard";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("TNT Cart Cat Guard loaded.");
	}
}
