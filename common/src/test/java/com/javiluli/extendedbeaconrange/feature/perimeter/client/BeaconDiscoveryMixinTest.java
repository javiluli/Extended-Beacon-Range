package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import com.javiluli.extendedbeaconrange.mixin.perimeter.client.BeaconClientLevelMixin;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.Inject;

/** Comprueba los eventos nativos de descubrimiento sin arrancar un cliente grafico. */
class BeaconDiscoveryMixinTest {
	@Test
	void discoveryHooksStillMatchTheNativeClientLifecycle() throws IOException {
		var level = readClass("net/minecraft/client/multiplayer/ClientLevel");
		int hooks = 0;
		for (var handler : BeaconClientLevelMixin.class.getDeclaredMethods()) {
			var injection = handler.getAnnotation(Inject.class);
			if (injection == null) { continue; }
			var parameters = handler.getParameterTypes();
			String descriptor = MethodType.methodType(void.class, Arrays.copyOf(parameters, parameters.length - 1))
					.toMethodDescriptorString();
			for (String target : injection.method()) {
				assertTrue(level.methods().stream().anyMatch(method -> method.methodName().equalsString(target)
						&& method.methodType().equalsString(descriptor)), target + descriptor);
				hooks++;
			}
		}
		assertEquals(4, hooks, "Chunk load/unload and beacon placement/removal must remain observed");
	}

	@Test
	void newBlockEntitiesAreStoredBeforeNotifyingTheDiscoveryHook() throws IOException {
		var addition = readClass("net/minecraft/world/level/chunk/LevelChunk").methods().stream()
				.filter(method -> method.methodName().equalsString("addAndRegisterBlockEntity"))
				.findFirst().orElseThrow().code().orElseThrow();
		boolean stored = false;
		boolean notified = false;
		for (var element : addition) {
			if (!(element instanceof InvokeInstruction call)) { continue; }
			if (call.name().equalsString("setBlockEntity")) { stored = true; }
			if (call.name().equalsString("onBlockEntityAdded")) {
				assertTrue(stored, "The beacon must be available when its chunk is queued for discovery");
				notified = true;
			}
		}
		assertTrue(notified, "New beacons in loaded chunks must notify the client lifecycle hook");
	}

	private static ClassModel readClass(String name) throws IOException {
		try (var input = BeaconDiscoveryMixinTest.class.getClassLoader().getResourceAsStream(name + ".class")) {
			assertNotNull(input);
			return ClassFile.of().parse(input.readAllBytes());
		}
	}
}
