package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
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

		for (String locale : List.of("en_us", "es_es")) {
			String path = "assets/extendedbeaconrange/lang/" + locale + ".json";
			try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
				assertNotNull(stream, path);
				var translations = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
				assertTrue(translations.has(contents.getKey()), locale + ": missing toggle translation");
				String pattern = translations.get(contents.getKey()).getAsString();
				String translated = String.format(Locale.ROOT, pattern, "STATE");
				assertTrue(translated.endsWith(" STATE"), locale + ": missing state placeholder");
				assertFalse(translated.contains("%"), locale + ": invalid translation format");
			}
		}
	}
}
