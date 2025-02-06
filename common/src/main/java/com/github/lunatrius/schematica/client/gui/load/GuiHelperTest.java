package com.github.lunatrius.schematica.client.gui.load;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiHelperTest {
	private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");

	@SuppressWarnings("SuspiciousNameCombination")
	public static void drawItemStackWithSlot(GuiGraphics guiGraphics, ItemStack itemStack, int top, int left) {
		drawItemStackSlot(guiGraphics, top, left);

		if (itemStack != null) {
			drawItemStack(guiGraphics, itemStack, top + 1, left + 1);
		}
	}

	public static void drawItemStack(@NotNull GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
		guiGraphics.renderItem(itemStack, x, y);
	}

	public static void drawItemStackSlot(@NotNull GuiGraphics guiGraphics, int x, int y) {
		guiGraphics.blitSprite(RenderType::guiTextured, SLOT_SPRITE, x, y, 18, 18);
	}

	public static void drawTexturedRectangle(@NotNull BufferBuilder buffer, float x0, float y0, float x1, float y1,
	                                         float z, float u0, float v0, float u1, float v1) {
		buffer.addVertex(x0, y0, z).setUv(u0, v0);
		buffer.addVertex(x0, y1, z).setUv(u0, v1);
		buffer.addVertex(x1, y1, z).setUv(u1, v1);
		buffer.addVertex(x1, y0, z).setUv(u1, v0);
	}

	public static void drawTexturedRectangle(BufferBuilder buffer, float x0, float y0, float x1, float y1, float z,
	                                         double textureWidth, double textureHeight, int argb) {
		float u0 = (float) (x0 / textureWidth);
		float v0 = (float) (y0 / textureHeight);
		float u1 = (float) (x1 / textureWidth);
		float v1 = (float) (y1 / textureHeight);

		drawTexturedRectangle(buffer, x0, y0, x1, y1, z, u0, v0, u1, v1, argb);
	}

	public static void drawTexturedRectangle(BufferBuilder buffer, float x0, float y0, float x1, float y1, float z,
	                                         float u0, float v0, float u1, float v1, int argb) {
		int a = (argb >> 24) & 0xFF;
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;

		drawTexturedRectangle(buffer, x0, y0, x1, y1, z, u0, v0, u1, v1, r, g, b, a);
	}

	public static void drawTexturedRectangle(@NotNull BufferBuilder buffer, float x0, float y0, float x1, float y1,
	                                         float z, float u0, float v0, float u1, float v1, int r, int g, int b,
	                                         int a) {
		buffer.addVertex(x0, y0, z).setUv(u0, v0).setColor(r, g, b, a);
		buffer.addVertex(x0, y1, z).setUv(u0, v1).setColor(r, g, b, a);
		buffer.addVertex(x1, y1, z).setUv(u1, v1).setColor(r, g, b, a);
		buffer.addVertex(x1, y0, z).setUv(u1, v0).setColor(r, g, b, a);
	}

	public static void drawColoredRectangle(BufferBuilder buffer, float x0, float y0, float x1, float y1, float z,
	                                        int argb) {
		int a = (argb >> 24) & 0xFF;
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;

		drawColoredRectangle(buffer, x0, y0, x1, y1, z, r, g, b, a);
	}

	public static void drawColoredRectangle(BufferBuilder buffer, float x0, float y0, float x1, float y1, float z,
	                                        int r, int g, int b, int a) {
		drawVerticalGradientRectangle(buffer, x0, y0, x1, y1, z, r, g, b, a, r, g, b, a);
	}

	public static void drawVerticalGradientRectangle(@NotNull BufferBuilder buffer, float x0, float y0, float x1,
	                                                 float y1, float z, int sr, int sg, int sb, int sa, int er, int eg,
	                                                 int eb, int ea) {
		buffer.addVertex(x0, y0, z).setColor(sr, sg, sb, sa);
		buffer.addVertex(x0, y1, z).setColor(er, eg, eb, ea);
		buffer.addVertex(x1, y1, z).setColor(er, eg, eb, ea);
		buffer.addVertex(x1, y0, z).setColor(sr, sg, sb, sa);
	}

	public static void drawVerticalGradientRectangle(BufferBuilder buffer, float x0, float y0, float x1, float y1,
	                                                 float z, int startColor, int endColor) {
		ColorComponents components = getColorComponents(startColor, endColor);

		drawVerticalGradientRectangle(buffer,
		                              x0,
		                              y0,
		                              x1,
		                              y1,
		                              z,
		                              components.sr(),
		                              components.sg(),
		                              components.sb(),
		                              components.sa(),
		                              components.er(),
		                              components.eg(),
		                              components.eb(),
		                              components.ea());
	}

	private static @NotNull ColorComponents getColorComponents(int startColor, int endColor) {
		int sa = (startColor >> 24) & 255;
		int sr = (startColor >> 16) & 255;
		int sg = (startColor >> 8) & 255;
		int sb = startColor & 255;
		int ea = (endColor >> 24) & 255;
		int er = (endColor >> 16) & 255;
		int eg = (endColor >> 8) & 255;
		int eb = endColor & 255;
		return new ColorComponents(sa, sr, sg, sb, ea, er, eg, eb);
	}

	public static void drawHorizontalGradientRectangle(BufferBuilder buffer, float x0, float y0, float x1, float y1,
	                                                   float z, int startColor, int endColor) {
		ColorComponents components = getColorComponents(startColor, endColor);

		drawHorizontalGradientRectangle(buffer,
		                                x0,
		                                y0,
		                                x1,
		                                y1,
		                                z,
		                                components.sr(),
		                                components.sg(),
		                                components.sb(),
		                                components.sa(),
		                                components.er(),
		                                components.eg(),
		                                components.eb(),
		                                components.ea());
	}

	public static void drawHorizontalGradientRectangle(@NotNull BufferBuilder buffer, float x0, float y0, float x1,
	                                                   float y1, float z, int sr, int sg, int sb, int sa, int er,
	                                                   int eg, int eb, int ea) {
		buffer.addVertex(x0, y0, z).setColor(sr, sg, sb, sa);
		buffer.addVertex(x0, y1, z).setColor(sr, sg, sb, sa);
		buffer.addVertex(x1, y1, z).setColor(er, eg, eb, ea);
		buffer.addVertex(x1, y0, z).setColor(er, eg, eb, ea);
	}

	private record ColorComponents(int sa, int sr, int sg, int sb, int ea, int er, int eg, int eb) {}
}