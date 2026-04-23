package dev.xani.tntcartcatguard.client;

import dev.xani.tntcartcatguard.TntCartCatGuardMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CatOverlayTextureManager {
	private static final Identifier DEFAULT_TEXTURE = Identifier.of(TntCartCatGuardMod.MOD_ID, "textures/gui/cat.png");
	private static final Identifier CUSTOM_TEXTURE = Identifier.of(TntCartCatGuardMod.MOD_ID, "dynamic/cat_overlay");

	private NativeImageBackedTexture customTexture;
	private Path loadedPath;
	private Identifier activeTexture = DEFAULT_TEXTURE;

	public Identifier getTexture(CatGuardConfig config) {
		Path desiredPath = config.resolveImagePath();
		if (desiredPath == null) {
			clearCustomTexture();
			activeTexture = DEFAULT_TEXTURE;
			return activeTexture;
		}

		if (desiredPath.equals(loadedPath) && customTexture != null) {
			return activeTexture;
		}

		try (InputStream inputStream = Files.newInputStream(desiredPath)) {
			NativeImage image = NativeImage.read(inputStream);
			NativeImageBackedTexture newTexture = new NativeImageBackedTexture(() -> "tntcartcatguard-custom", image);
			clearCustomTexture();
			MinecraftClient.getInstance().getTextureManager().registerTexture(CUSTOM_TEXTURE, newTexture);
			customTexture = newTexture;
			loadedPath = desiredPath;
			activeTexture = CUSTOM_TEXTURE;
		} catch (IOException exception) {
			TntCartCatGuardMod.LOGGER.warn("Failed to load custom cat overlay texture from {}", desiredPath, exception);
			clearCustomTexture();
			activeTexture = DEFAULT_TEXTURE;
		}

		return activeTexture;
	}

	public void clearCustomTexture() {
		if (customTexture != null) {
			customTexture.close();
			customTexture = null;
		}

		loadedPath = null;
	}
}
