package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import com.mojang.blaze3d.vertex.CompactVectorArray;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class BeaconPerimeterRendererTest {
	@Test
	void preservesWallFadeAndOpaquePostsAcrossFrames() {
		List<float[]> vertices = new ArrayList<>();
		List<Float> alphas = new ArrayList<>();
		VertexConsumer sink = sink(vertices, alphas);
		var entry = BeaconRenderEntry.create(new BlockPos(0, 64, 0), 150,
				BeaconOverlayColors.getPositionColor(0, 0, true), -64, 320);
		List<BeaconRenderEntry> entries = List.of(entry);
		for (int frame = 0; frame < 2; frame++) {
			vertices.clear();
			alphas.clear();
			BeaconPerimeterRenderer.renderAll(entries, new Matrix4f(), sink, Vec3.ZERO);
			assertEquals(128, vertices.size());
			assertEquals(96, alphas.stream().filter(a -> a == 1.0f).count());
			assertEquals(8, alphas.stream().filter(a -> a == 0.0f).count());
			assertEquals(24, alphas.stream().filter(a -> a == ARGB.alphaFloat(ARGB.color(0.25f, 0))).count());
			for (float[] vertex : vertices) {
				assertTrue(vertex[0] >= entry.bounds().minX - 0.0001 && vertex[0] <= entry.bounds().maxX + 0.0001);
				assertTrue(vertex[1] >= -64 && vertex[1] <= 68);
				assertTrue(vertex[2] >= entry.bounds().minZ - 0.0001 && vertex[2] <= entry.bounds().maxZ + 0.0001);
			}
		}
	}

	@Test
	void cornersAreClosedPostsWithTheConfiguredWidthAndBeaconColor() {
		List<float[]> vertices = new ArrayList<>();
		List<Float> alphas = new ArrayList<>();
		var color = BeaconOverlayColors.getPositionColor(12, -30, true);
		int packedColor = ARGB.colorFromFloat(1, color.red(), color.green(), color.blue());
		var entry = BeaconRenderEntry.create(new BlockPos(12, 70, -30), 60, color, -64, 320);
		BeaconPerimeterRenderer.renderAll(List.of(entry), new Matrix4f(), sink(vertices, alphas), new Vec3(17.25, 12.5, -8.75));
		var opaque = IntStream.range(0, vertices.size()).filter(i -> alphas.get(i) == 1.0f).mapToObj(vertices::get).toList();
		assertEquals(96, opaque.size());
		for (int corner = 0; corner < 4; corner++) {
			var post = opaque.subList(corner * 24, (corner + 1) * 24);
			var x = post.stream().mapToDouble(v -> v[0]).summaryStatistics();
			var y = post.stream().mapToDouble(v -> v[1]).summaryStatistics();
			var z = post.stream().mapToDouble(v -> v[2]).summaryStatistics();
			assertEquals(BeaconAreaSettings.FULL_WALL_CORNER_WIDTH, x.getMax() - x.getMin(), 0.00001);
			assertEquals(BeaconAreaSettings.FULL_WALL_CORNER_WIDTH, z.getMax() - z.getMin(), 0.00001);
			assertEquals(-64 - 12.5, y.getMin());
			assertEquals(74 - 12.5, y.getMax());
			var corners = new HashSet<Vector3f>();
			var planes = new HashSet<String>();
			for (float[] vertex : post) {
				corners.add(new Vector3f(vertex[0], vertex[1], vertex[2]));
				assertEquals(ARGB.redFloat(packedColor), vertex[3]);
				assertEquals(ARGB.greenFloat(packedColor), vertex[4]);
				assertEquals(ARGB.blueFloat(packedColor), vertex[5]);
			}
			for (int face = 0; face < 6; face++) {
				var quad = post.subList(face * 4, face * 4 + 4);
				for (int axis = 0; axis < 3; axis++) {
					int dimension = axis;
					if (quad.stream().allMatch(v -> v[dimension] == quad.getFirst()[dimension])) {
						planes.add(axis + ":" + quad.getFirst()[axis]);
					}
				}
			}
			assertEquals(8, corners.size());
			assertEquals(6, planes.size(), "Each post must have six distinct closed faces");
		}
	}

	@Test
	void refreshesChangedEntriesAndHandlesRemovingOrRestoringBeacons() {
		List<float[]> vertices = new ArrayList<>();
		List<Float> alphas = new ArrayList<>();
		VertexConsumer sink = sink(vertices, alphas);
		var a = BeaconRenderEntry.create(new BlockPos(0, 64, 0), 60,
				BeaconOverlayColors.getPositionColor(0, 0, true), -64, 320);
		var b = BeaconRenderEntry.create(new BlockPos(200, 64, 200), 90,
				BeaconOverlayColors.getPositionColor(200, 200, true), -64, 320);
		var changed = BeaconRenderEntry.create(a.position(), 150,
				BeaconOverlayColors.getPositionColor(0, 0, false), -64, 320);
		List<List<BeaconRenderEntry>> frames = List.of(List.of(a, b), List.of(changed, b), List.of(b), List.of(), List.of(a));
		for (var entries : frames) {
			vertices.clear();
			alphas.clear();
			BeaconPerimeterRenderer.renderAll(entries, new Matrix4f(), sink, Vec3.ZERO);
			assertEquals(entries.size() * 128, vertices.size());
			for (int i = 0; i < entries.size(); i++) {
				var entry = entries.get(i);
				var rendered = vertices.subList(i * 128, (i + 1) * 128);
				var x = rendered.stream().mapToDouble(v -> v[0]).summaryStatistics();
				assertEquals(entry.bounds().minX, x.getMin(), 0.0001);
				assertEquals(entry.bounds().maxX, x.getMax(), 0.0001);
				int rgb = ARGB.colorFromFloat(1, entry.color().red(), entry.color().green(), entry.color().blue());
				for (float[] vertex : rendered) {
					assertEquals(ARGB.redFloat(rgb), vertex[3]);
					assertEquals(ARGB.greenFloat(rgb), vertex[4]);
					assertEquals(ARGB.blueFloat(rgb), vertex[5]);
				}
			}
		}
	}

	@Test
	void emitsOverlappingPerimetersForVanillaQuadSorting() {
		List<float[]> vertices = new ArrayList<>();
		var color = BeaconOverlayColors.getPositionColor(0, 0, true);
		var a = BeaconRenderEntry.create(new BlockPos(0, 64, 0), 60, color, -64, 320);
		var b = BeaconRenderEntry.create(new BlockPos(20, 64, 20), 90, color, -64, 320);
		BeaconPerimeterRenderer.renderAll(List.of(a, b), new Matrix4f(), sink(vertices, new ArrayList<>()), new Vec3(12, 50, 5));
		assertEquals(256, vertices.size());
		assertTrue(RenderTypes.debugQuads().sortOnUpload());
		CompactVectorArray centers = new CompactVectorArray(vertices.size() / 4);
		for (int quad = 0; quad < centers.size(); quad++) {
			float[] first = vertices.get(quad * 4), opposite = vertices.get(quad * 4 + 2);
			centers.set(quad, (first[0] + opposite[0]) * 0.5f,
					(first[1] + opposite[1]) * 0.5f, (first[2] + opposite[2]) * 0.5f);
		}
		double previous = Double.POSITIVE_INFINITY;
		Vector3f scratch = new Vector3f();
		for (int quad : VertexSorting.DISTANCE_TO_ORIGIN.sort(centers)) {
			double distance = centers.get(quad, scratch).lengthSquared();
			assertTrue(distance <= previous + 0.01, "Vanilla must sort all quads far to near");
			previous = distance;
		}
	}

	private static VertexConsumer sink(List<float[]> vertices, List<Float> alphas) {
		return (VertexConsumer) Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(), new Class<?>[] { VertexConsumer.class },
				(proxy, method, args) -> {
					if (method.getName().equals("addVertex") && args.length == 4) {
						vertices.add(new float[] {(float) args[1], (float) args[2], (float) args[3], 0, 0, 0});
					} else if (method.getName().equals("setColor")) {
						int color;
						if (args.length == 1) { color = (int) args[0]; }
						else if (args[0] instanceof Float) {
							// Reproduce la conversion del VertexConsumer vanilla para comparar ambos caminos.
							color = ARGB.color((int) ((float) args[3] * 255), (int) ((float) args[0] * 255),
									(int) ((float) args[1] * 255), (int) ((float) args[2] * 255));
						} else { color = ARGB.color((int) args[3], (int) args[0], (int) args[1], (int) args[2]); }
						float[] vertex = vertices.getLast();
						vertex[3] = ARGB.redFloat(color);
						vertex[4] = ARGB.greenFloat(color);
						vertex[5] = ARGB.blueFloat(color);
						alphas.add(ARGB.alphaFloat(color));
					}
					return method.getReturnType() == void.class ? null : proxy;
				});
	}
}
