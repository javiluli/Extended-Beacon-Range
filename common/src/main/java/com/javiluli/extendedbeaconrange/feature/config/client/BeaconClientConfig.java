package com.javiluli.extendedbeaconrange.feature.config.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.javiluli.extendedbeaconrange.Constants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

/** Guarda opciones del cliente y conserva los campos desconocidos y archivos invalidos. */
public final class BeaconClientConfig {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static BeaconClientConfig instance;
	private final Path file;
	private JsonObject values = new JsonObject();
	private boolean readFailed;
	/** Usa la paleta por beacon; false muestra todos los perimetros en blanco. */
	public boolean colorVisualPerimeter = true;
	/** Muestra el tiempo restante debajo de los efectos de beacon del HUD. */
	public boolean showEffectTimers = true;

	/** Carga una sola instancia desde el mismo JSON usado por todas las pantallas. */
	public static BeaconClientConfig get() {
		if (instance == null) {
			instance = new BeaconClientConfig(Minecraft.getInstance().gameDirectory.toPath()
					.resolve("config").resolve(Constants.MOD_ID + "-client.json"));
		}
		return instance;
	}

	/** Carga una ruta concreta, tambien util para probar persistencia sin abrir Minecraft. */
	BeaconClientConfig(Path file) {
		this.file = file;
		if (!Files.exists(file)) {
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(file)) {
			JsonElement root = JsonParser.parseReader(reader);
			if (!root.isJsonObject()) {
				throw new JsonParseException("Expected a JSON object");
			}
			values = root.getAsJsonObject();
			boolean color = readBoolean("colorVisualPerimeter", true);
			boolean timers = readBoolean("showEffectTimers", true);
			colorVisualPerimeter = color;
			showEffectTimers = timers;
		} catch (IOException | JsonParseException e) {
			// Preserve unreadable or invalid files instead of replacing the user's settings with defaults.
			readFailed = true;
			LOGGER.warn("Could not read beacon client config {}; using defaults", file, e);
		}
	}

	/** Valida cada opcion antes de sustituir los valores por defecto. */
	private boolean readBoolean(String key, boolean fallback) {
		JsonElement value = values.get(key);
		if (value == null) { return fallback; }
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
			throw new JsonParseException(key + " must be a boolean");
		}
		return value.getAsBoolean();
	}

	/** Persiste de forma atomica; devuelve false si el archivo no puede guardarse con seguridad. */
	public boolean save() {
		if (readFailed) {
			return false;
		}
		Path temporary = null;
		try {
			Files.createDirectories(file.toAbsolutePath().getParent());
			temporary = Files.createTempFile(file.toAbsolutePath().getParent(), "extendedbeaconrange-", ".tmp");
			values.addProperty("colorVisualPerimeter", colorVisualPerimeter);
			values.addProperty("showEffectTimers", showEffectTimers);
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(values, writer);
			}
			try {
				Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (IOException | JsonParseException e) {
			LOGGER.warn("Could not save beacon client config {}", file, e);
			return false;
		} finally {
			if (temporary != null) {
				try {
					Files.deleteIfExists(temporary);
				} catch (IOException e) {
					LOGGER.warn("Could not remove temporary beacon config {}", temporary, e);
				}
			}
		}
	}
}
