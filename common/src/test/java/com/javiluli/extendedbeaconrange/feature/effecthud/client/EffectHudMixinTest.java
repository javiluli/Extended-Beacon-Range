package com.javiluli.extendedbeaconrange.feature.effecthud.client;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.util.Arrays;
import com.javiluli.extendedbeaconrange.mixin.effecthud.client.BeaconEffectHudMixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Comprueba los puntos de inyeccion reales sin necesitar un contexto grafico. */
class EffectHudMixinTest {
	@ParameterizedTest
	@ValueSource(strings = {"extendedbeaconrange$effectBorder", "extendedbeaconrange$effectLabels"})
	void nativeHudRetainsTheRequiredCallsAndUnambiguousEffectLocal(String handler) throws IOException {
		var method = Arrays.stream(BeaconEffectHudMixin.class.getDeclaredMethods())
				.filter(candidate -> candidate.getName().equals(handler)).findFirst().orElseThrow();
		var injection = method.getAnnotation(WrapOperation.class);
		assertNotNull(injection);
		String target = injection.at()[0].target();
		int ownerEnd = target.indexOf(';');
		int descriptorStart = target.indexOf('(');
		String owner = target.substring(1, ownerEnd);
		String name = target.substring(ownerEnd + 1, descriptorStart);
		String descriptor = target.substring(descriptorStart);
		var hud = readClass("net/minecraft/client/gui/Hud").methods().stream()
				.filter(candidate -> candidate.methodName().equalsString("extractEffects"))
				.findFirst().orElseThrow().code().orElseThrow();
		var locals = hud.findAttribute(Attributes.localVariableTable()).orElseThrow().localVariables();
		int matches = 0;
		int offset = 0;
		for (var element : hud) {
			if (!(element instanceof Instruction instruction)) { continue; }
			if (instruction instanceof InvokeInstruction call && call.owner().asInternalName().equals(owner)
					&& call.name().equalsString(name) && call.type().equalsString(descriptor)) {
				matches++;
				int position = offset;
				assertEquals(1, locals.stream().filter(local ->
						local.type().equalsString("Lnet/minecraft/world/effect/MobEffectInstance;")
						&& local.startPc() <= position && position < local.startPc() + local.length()).count());
			}
			offset += instruction.sizeInBytes();
		}
		assertEquals(handler.endsWith("Border") ? 2 : 1, matches);
	}

	@Test
	void fourthIntegerLocalIsTheSingleInitialYAssignment() throws IOException {
		var hud = readClass("net/minecraft/client/gui/Hud").methods().stream()
				.filter(candidate -> candidate.methodName().equalsString("extractEffects"))
				.findFirst().orElseThrow().code().orElseThrow();
		var locals = hud.findAttribute(Attributes.localVariableTable()).orElseThrow().localVariables();
		var y = locals.stream().filter(local -> local.type().equalsString("I"))
				.sorted(java.util.Comparator.comparingInt(local -> local.slot())).skip(3).findFirst().orElseThrow();
		assertEquals("y", y.name().stringValue());
		int matches = 0;
		for (var instruction : hud) {
			if (instruction instanceof StoreInstruction store && store.slot() == y.slot()) { matches++; }
		}
		assertEquals(1, matches);
	}

	@Test
	void beaconPowerSyncTargetsTheNativeSuccessfulUpdate() throws IOException {
		var menu = readClass("net/minecraft/world/inventory/BeaconMenu");
		assertTrue(menu.methods().stream().anyMatch(method -> method.methodName().equalsString("updateEffects")
				&& method.methodType().equalsString("(Ljava/util/Optional;Ljava/util/Optional;)Z")));
		assertTrue(menu.fields().stream().anyMatch(field -> field.fieldName().equalsString("access")
				&& field.fieldType().equalsString("Lnet/minecraft/world/inventory/ContainerLevelAccess;")));
	}

	private static ClassModel readClass(String name) throws IOException {
		try (var input = EffectHudMixinTest.class.getClassLoader().getResourceAsStream(name + ".class")) {
			assertNotNull(input);
			return ClassFile.of().parse(input.readAllBytes());
		}
	}
}
