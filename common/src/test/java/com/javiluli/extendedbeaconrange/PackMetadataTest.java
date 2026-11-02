package com.javiluli.extendedbeaconrange;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PackMetadataTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@ParameterizedTest
	@EnumSource(PackType.class)
	void bundledPackSupportsTheConfiguredMinecraftVersion(PackType type) throws Exception {
		try (var stream = getClass().getClassLoader().getResourceAsStream("pack.mcmeta")) {
			assertNotNull(stream);
			var pack = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
					.getAsJsonObject().getAsJsonObject("pack");
			var metadata = PackMetadataSection.forPackType(type).codec().parse(JsonOps.INSTANCE, pack).getOrThrow();
			var expected = SharedConstants.getCurrentVersion().packVersion(type);
			assertTrue(metadata.supportedFormats().isValueInRange(expected), type + " requires pack format " + expected);
		}
	}
}
