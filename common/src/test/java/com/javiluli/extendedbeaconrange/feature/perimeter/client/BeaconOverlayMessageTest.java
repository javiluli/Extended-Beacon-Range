package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BeaconOverlayMessageTest {

	@ParameterizedTest
	@CsvSource({"true, true", "true, false", "false, true", "false, false"})
	void toggleMessagesTranslateScopeAndStateWhilePreservingColors(boolean all, boolean enabled) throws Exception {
		Component message = BeaconOverlayToggle.toggleMessage(all, enabled);
		var contents = assertInstanceOf(TranslatableContents.class, message.getContents());
		assertEquals("message.extendedbeaconrange.perimeters." + (all ? "all" : "single"), contents.getKey());
		assertEquals(TextColor.fromLegacyFormat(ChatFormatting.WHITE), message.getStyle().getColor());
		assertEquals(1, contents.getArgs().length);
		var state = assertInstanceOf(Component.class, contents.getArgs()[0]);
		var stateContents = assertInstanceOf(TranslatableContents.class, state.getContents());
		assertEquals(enabled ? "options.on" : "options.off", stateContents.getKey());
		assertEquals(TextColor.fromLegacyFormat(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), state.getStyle().getColor());

		var resource = getClass().getClassLoader().getResource("assets/extendedbeaconrange/lang");
		assertNotNull(resource);
		Path directory = Path.of(resource.toURI());
		try (var reader = Files.newBufferedReader(directory.resolve("en_us.json"), StandardCharsets.UTF_8);
				var files = Files.list(directory)) {
			var defaults = JsonParser.parseReader(reader).getAsJsonObject();
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
				try (var translationReader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
					var translations = JsonParser.parseReader(translationReader).getAsJsonObject();
					assertEquals(defaults.keySet(), translations.keySet(), file + ": incomplete translation");
					for (var entry : translations.entrySet()) {
						assertTrue(entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString(), file + ": " + entry.getKey());
						assertFalse(entry.getValue().getAsString().isBlank(), file + ": " + entry.getKey());
					}
					String pattern = translations.get(contents.getKey()).getAsString();
					assertEquals(1, Pattern.compile("%(?:1\\$)?s").matcher(pattern).results().count(), file + ": missing state placeholder");
					String translated = String.format(Locale.ROOT, pattern, "STATE");
					assertTrue(translated.contains("STATE"), file + ": missing translated state");
					assertFalse(translated.contains("%"), file + ": invalid translation format");
				}
			}
		}
	}
}
