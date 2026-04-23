package dev.xani.tntcartcatguard.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.xani.tntcartcatguard.TntCartCatGuardMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CatGuardConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("tntcartcatguard.json");

	public boolean enabled = true;
	public double detectionRadius = 16.0D;
	public int overlayX = 12;
	public int overlayY = 12;
	public int overlayWidth = 96;
	public int overlayHeight = 96;
	public boolean anchorRight = true;
	public boolean drawWarningText = false;
	public boolean drawDistanceText = true;
	public int warningTextColor = 0xFFFF8080;
	public int countTextColor = 0xFFFFFFFF;
	public boolean playSound = true;
	public float soundVolume = 0.8F;
	public String imagePath = "";

	public static CatGuardConfig load() {
		if (Files.notExists(FILE)) {
			CatGuardConfig config = new CatGuardConfig();
			config.save();
			return config;
		}

		try (Reader reader = Files.newBufferedReader(FILE)) {
			CatGuardConfig config = GSON.fromJson(reader, CatGuardConfig.class);
			if (config == null) {
				config = new CatGuardConfig();
			}

			config.clamp();
			return config;
		} catch (IOException | JsonParseException exception) {
			TntCartCatGuardMod.LOGGER.warn("Failed to load config, using defaults.", exception);
			CatGuardConfig config = new CatGuardConfig();
			config.save();
			return config;
		}
	}

	public void save() {
		clamp();

		try {
			Files.createDirectories(FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FILE)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException exception) {
			TntCartCatGuardMod.LOGGER.error("Failed to save config.", exception);
		}
	}

	public Path resolveImagePath() {
		if (imagePath == null || imagePath.isBlank()) {
			return null;
		}

		Path path = Path.of(imagePath);
		if (!path.isAbsolute()) {
			path = FabricLoader.getInstance().getGameDir().resolve(path);
		}

		return path.normalize();
	}

	private void clamp() {
		detectionRadius = Math.clamp(detectionRadius, 2.0D, 128.0D);
		overlayWidth = Math.clamp(overlayWidth, 16, 512);
		overlayHeight = Math.clamp(overlayHeight, 16, 512);
		overlayX = Math.clamp(overlayX, 0, 4096);
		overlayY = Math.clamp(overlayY, 0, 4096);
		soundVolume = Math.clamp(soundVolume, 0.0F, 2.0F);
	}
}
