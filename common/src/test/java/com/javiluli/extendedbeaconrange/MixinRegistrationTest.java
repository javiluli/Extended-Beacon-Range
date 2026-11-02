package com.javiluli.extendedbeaconrange;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class MixinRegistrationTest {
	@Test
	void reservedMixinPackageContainsNoFeatureOrEntrypointClasses() throws Exception {
		ClassLoader loader = getClass().getClassLoader();
		try (var stream = loader.getResourceAsStream(Constants.MOD_ID + ".mixins.json")) {
			assertNotNull(stream);
			var config = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
			String prefix = config.get("package").getAsString();
			var registered = new HashSet<String>();
			for (String side : List.of("mixins", "client", "server")) {
				for (var mixin : config.getAsJsonArray(side)) { registered.add(prefix + '.' + mixin.getAsString()); }
			}
			Path classes = Path.of(Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			try (var paths = Files.walk(classes.resolve(prefix.replace('.', '/')))) {
				for (Path file : paths.filter(path -> path.toString().endsWith(".class")).toList()) {
					String name = classes.relativize(file).toString().replace('\\', '.').replace('/', '.');
					name = name.substring(0, name.length() - ".class".length()).split("\\$", 2)[0];
					assertTrue(registered.contains(name), "Normal class inside reserved mixin package: " + name);
				}
			}
		}
	}

	@Test
	void registeredMixinsResolveAfterFeatureMoves() throws IOException {
		ClassLoader loader = getClass().getClassLoader();
		try (var stream = loader.getResourceAsStream(Constants.MOD_ID + ".mixins.json")) {
			assertNotNull(stream);
			var config = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
			String prefix = config.get("package").getAsString().replace('.', '/') + '/';
			for (String side : List.of("mixins", "client", "server")) {
				for (var mixin : config.getAsJsonArray(side)) {
					String path = prefix + mixin.getAsString().replace('.', '/') + ".class";
					assertNotNull(loader.getResource(path), "Missing registered mixin: " + path);
				}
			}
		}
	}
}
