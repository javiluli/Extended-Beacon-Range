package com.javiluli.extendedbeaconrange.feature.config.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BeaconClientConfigTest {

	@TempDir
	Path directory;

	@Test
	void createsDefaultConfigAndPersistsChangesAcrossReloads() throws IOException {
		Path file = directory.resolve("config/client.json");
		BeaconClientConfig config = new BeaconClientConfig(file);
		assertTrue(config.colorVisualPerimeter);
		assertTrue(config.showEffectTimers);
		assertTrue(JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("colorVisualPerimeter").getAsBoolean());
		config.colorVisualPerimeter = false;
		config.showEffectTimers = false;
		assertTrue(config.save());
		assertFalse(new BeaconClientConfig(file).colorVisualPerimeter);
		assertFalse(new BeaconClientConfig(file).showEffectTimers);
		config.colorVisualPerimeter = true;
		assertTrue(config.save());
		assertTrue(new BeaconClientConfig(file).colorVisualPerimeter);
		assertFalse(new BeaconClientConfig(file).showEffectTimers);
	}

	@Test
	void upgradesOldConfigWithoutResettingThePlayersPerimeterPreference() throws IOException {
		Path file = directory.resolve("client.json");
		Files.writeString(file, "{\"colorVisualPerimeter\":false}");
		BeaconClientConfig config = new BeaconClientConfig(file);
		assertFalse(config.colorVisualPerimeter);
		assertTrue(config.showEffectTimers);
		config.showEffectTimers = false;
		assertTrue(config.save());
		BeaconClientConfig reloaded = new BeaconClientConfig(file);
		assertFalse(reloaded.colorVisualPerimeter);
		assertFalse(reloaded.showEffectTimers);
	}

	@Test
	void defaultsMissingOptionAndPreservesOtherSettings() throws IOException {
		Path file = directory.resolve("client.json");
		Files.writeString(file, "{\"otherSetting\":{\"enabled\":true}}");
		BeaconClientConfig config = new BeaconClientConfig(file);
		assertTrue(config.colorVisualPerimeter);
		assertTrue(config.showEffectTimers);
		config.colorVisualPerimeter = false;
		assertTrue(config.save());
		var json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
		assertTrue(json.getAsJsonObject("otherSetting").get("enabled").getAsBoolean());
		assertFalse(json.get("colorVisualPerimeter").getAsBoolean());
	}

	@ParameterizedTest
	@ValueSource(strings = {"{", "[]", "null", "{\"colorVisualPerimeter\":\"false\"}",
			"{\"colorVisualPerimeter\":null}", "{\"colorVisualPerimeter\":0}",
			"{\"showEffectTimers\":\"false\"}", "{\"showEffectTimers\":null}",
			"{\"colorVisualPerimeter\":false,\"showEffectTimers\":0}"})
	void invalidConfigUsesDefaultWithoutOverwritingOriginal(String contents) throws IOException {
		Path file = directory.resolve("client.json");
		Files.writeString(file, contents);
		BeaconClientConfig config = new BeaconClientConfig(file);
		assertTrue(config.colorVisualPerimeter);
		assertTrue(config.showEffectTimers);
		config.colorVisualPerimeter = false;
		assertFalse(config.save());
		assertEquals(contents, Files.readString(file));
	}

	@Test
	void savingFailureDoesNotCrashOrRemoveExistingFiles() throws IOException {
		Path parentFile = directory.resolve("not-a-directory");
		Files.writeString(parentFile, "keep me");
		BeaconClientConfig config = new BeaconClientConfig(parentFile.resolve("client.json"));
		assertTrue(config.colorVisualPerimeter);
		assertFalse(config.save());
		assertEquals("keep me", Files.readString(parentFile));
	}

}
