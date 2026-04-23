package dev.xani.tntcartcatguard.client;

import dev.xani.tntcartcatguard.TntCartCatGuardMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class TntCartCatGuardClient implements ClientModInitializer {
	private static final Identifier BUILTIN_TEXTURE = Identifier.of(TntCartCatGuardMod.MOD_ID, "textures/gui/cat.png");

	private static CatGuardConfig config;
	private static CatGuardState state = CatGuardState.EMPTY;
	private static final CatOverlayTextureManager TEXTURE_MANAGER = new CatOverlayTextureManager();

	private KeyBinding toggleKeyBinding;
	private boolean hadThreat;

	@Override
	public void onInitializeClient() {
		config = CatGuardConfig.load();

		toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.tntcartcatguard.toggle",
			GLFW.GLFW_KEY_K,
			KeyBinding.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		HudRenderCallback.EVENT.register((drawContext, tickCounter) -> renderHud(drawContext));
	}

	private void onEndTick(MinecraftClient client) {
		while (toggleKeyBinding.wasPressed()) {
			config.enabled = !config.enabled;
			config.save();

			if (client.player != null) {
				Text text = Text.translatable(
					config.enabled ? "message.tntcartcatguard.enabled" : "message.tntcartcatguard.disabled"
				).formatted(config.enabled ? Formatting.GREEN : Formatting.RED);
				client.player.sendMessage(text, true);
			}
		}

		if (client.world == null || client.player == null) {
			state = CatGuardState.EMPTY;
			hadThreat = false;
			return;
		}

		state = scanThreats(client);

		if (state.hasThreat() && !hadThreat && config.playSound) {
			client.player.playSound(SoundEvents.ENTITY_CAT_HISS, config.soundVolume, 1.0F);
		}

		hadThreat = state.hasThreat();
	}

	private CatGuardState scanThreats(MinecraftClient client) {
		if (!config.enabled) {
			return CatGuardState.EMPTY;
		}

		Box searchBox = client.player.getBoundingBox().expand(config.detectionRadius);
		List<TntMinecartEntity> tntMinecarts = client.world.getEntitiesByClass(
			TntMinecartEntity.class,
			searchBox,
			entity -> entity.isAlive()
		);

		if (tntMinecarts.isEmpty()) {
			return CatGuardState.EMPTY;
		}

		double nearestDistance = Double.POSITIVE_INFINITY;
		for (TntMinecartEntity minecart : tntMinecarts) {
			double distance = minecart.distanceTo(client.player);
			if (distance < nearestDistance) {
				nearestDistance = distance;
			}
		}

		return new CatGuardState(tntMinecarts.size(), nearestDistance);
	}

	private void renderHud(DrawContext drawContext) {
		if (!config.enabled || !state.hasThreat()) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) {
			return;
		}

		Identifier texture = TEXTURE_MANAGER.getTexture(config);
		if (texture == null) {
			texture = BUILTIN_TEXTURE;
		}

		int x = config.overlayX;
		int y = config.overlayY;
		int width = config.overlayWidth;
		int height = config.overlayHeight;

		drawContext.fill(x - 4, y - 4, x + width + 4, y + height + 28, 0x66000000);
		drawContext.drawTexturedQuad(texture, x, y, x + width, y + height, 0.0F, 1.0F, 0.0F, 1.0F);

		int textY = y + height + 4;
		if (config.drawWarningText) {
			drawContext.drawText(
				client.textRenderer,
				Text.translatable("overlay.tntcartcatguard.warning"),
				x,
				textY,
				config.warningTextColor,
				true
			);
			textY += 10;
		}

		if (config.drawDistanceText) {
			String line = String.format(
				"%s %d | %s %.1f",
				Text.translatable("overlay.tntcartcatguard.count").getString(),
				state.nearbyCount(),
				Text.translatable("overlay.tntcartcatguard.distance").getString(),
				state.nearestDistance()
			);
			drawContext.drawText(client.textRenderer, line, x, textY, config.countTextColor, true);
		}
	}
}
