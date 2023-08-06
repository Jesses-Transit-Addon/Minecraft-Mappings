package org.mtr.mapping.mapper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.mtr.mapping.annotation.MappedMethod;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.tool.ColorHelper;
import org.mtr.mapping.tool.DummyClass;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public final class GraphicsHolder extends DummyClass {

	private int matrixPushes;

	final MatrixStack matrixStack;
	final VertexConsumerProvider vertexConsumerProvider;
	private final VertexConsumerProvider.Immediate immediate;

	public static final int DEFAULT_LIGHT = 0xF000F0;

	public GraphicsHolder(@Nullable MatrixStack matrixStack, @Nullable VertexConsumerProvider vertexConsumerProvider) {
		this.matrixStack = matrixStack;
		this.vertexConsumerProvider = vertexConsumerProvider;
		immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		push();
	}

	@MappedMethod
	public void push() {
		if (matrixStack != null) {
			matrixStack.push();
			matrixPushes++;
		}
	}

	@MappedMethod
	public void pop() {
		if (matrixStack != null && matrixPushes > 0) {
			matrixStack.pop();
			matrixPushes--;
		}
	}

	@MappedMethod
	public void popAll() {
		while (matrixPushes > 0) {
			pop();
		}
	}

	@MappedMethod
	public void translate(double x, double y, double z) {
		if (matrixStack != null) {
			matrixStack.translate(x, y, z);
		}
	}

	@MappedMethod
	public void scale(float x, float y, float z) {
		if (matrixStack != null) {
			matrixStack.scale(x, y, z);
		}
	}

	@MappedMethod
	public void rotateXRadians(float angle) {
		if (matrixStack != null) {
			matrixStack.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(angle));
		}
	}

	@MappedMethod
	public void rotateYRadians(float angle) {
		if (matrixStack != null) {
			matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(angle));
		}
	}

	@MappedMethod
	public void rotateZRadians(float angle) {
		if (matrixStack != null) {
			matrixStack.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(angle));
		}
	}

	@MappedMethod
	public void rotateXDegrees(float angle) {
		if (matrixStack != null) {
			matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(angle));
		}
	}

	@MappedMethod
	public void rotateYDegrees(float angle) {
		if (matrixStack != null) {
			matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(angle));
		}
	}

	@MappedMethod
	public void rotateZDegrees(float angle) {
		if (matrixStack != null) {
			matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(angle));
		}
	}

	@MappedMethod
	public void drawText(MutableText mutableText, int x, int y, int color, boolean shadow, int light) {
		if (matrixStack != null && immediate != null) {
			getInstance().textRenderer.draw(mutableText.data, x, y, color, shadow, matrixStack.peek().getModel(), immediate, false, 0, light);
		}
	}

	@MappedMethod
	public void drawText(OrderedText orderedText, int x, int y, int color, boolean shadow, int light) {
		if (matrixStack != null && immediate != null) {
			getInstance().textRenderer.draw(orderedText.data, x, y, color, shadow, matrixStack.peek().getModel(), immediate, false, 0, light);
		}
	}

	@MappedMethod
	public void drawText(String text, int x, int y, int color, boolean shadow, int light) {
		if (matrixStack != null && immediate != null) {
			getInstance().textRenderer.draw(text, x, y, color, shadow, matrixStack.peek().getModel(), immediate, false, 0, light);
		}
	}

	@MappedMethod
	public static int getTextWidth(MutableText mutableText) {
		return getInstance().textRenderer.getWidth(mutableText.data);
	}

	@MappedMethod
	public static int getTextWidth(OrderedText orderedText) {
		return getInstance().textRenderer.getWidth(orderedText.data);
	}

	@MappedMethod
	public static int getTextWidth(String text) {
		return getInstance().textRenderer.getWidth(text);
	}

	@MappedMethod
	public static List<OrderedText> wrapLines(MutableText mutableText, int width) {
		return getInstance().textRenderer.wrapLines(mutableText.data, width).stream().map(OrderedText::new).collect(Collectors.toList());
	}

	private static MinecraftClient getInstance() {
		return MinecraftClient.getInstance();
	}

	@MappedMethod
	public void drawImmediate() {
		if (immediate != null) {
			immediate.draw();
		}
	}

	@MappedMethod
	public void drawLineInWorld(float x1, float y1, float z1, float x2, float y2, float z2, int color) {
		if (matrixStack != null) {
			ColorHelper.unpackColor(color, (a, r, g, b) -> {
				final VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(net.minecraft.client.render.RenderLayer.LINES);

				final MatrixStack.Entry entry = matrixStack.peek();
				final Matrix4f matrix4f = entry.getModel();
				final Matrix3f matrix3f = entry.getNormal();

				vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a).normal(matrix3f, 0, 1, 0).next();
				vertexConsumer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a).normal(matrix3f, 0, 1, 0).next();
			});
		}
	}

	@MappedMethod
	public void drawTextureInWorld(RenderLayer renderLayer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, Direction facing, int color, int light) {
		if (matrixStack != null) {
			ColorHelper.unpackColor(color, (a, r, g, b) -> {
				final VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer.data);

				final Vector3i vector3i = facing.getVector();
				final int x = vector3i.getX();
				final int y = vector3i.getY();
				final int z = vector3i.getZ();

				final MatrixStack.Entry entry = matrixStack.peek();
				final Matrix4f matrix4f = entry.getModel();
				final Matrix3f matrix3f = entry.getNormal();

				vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a).texture(u1, v2).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, x, y, z).next();
				vertexConsumer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a).texture(u2, v2).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, x, y, z).next();
				vertexConsumer.vertex(matrix4f, x3, y3, z3).color(r, g, b, a).texture(u2, v1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, x, y, z).next();
				vertexConsumer.vertex(matrix4f, x4, y4, z4).color(r, g, b, a).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, x, y, z).next();
			});
		}
	}
}
