package com.tmtravlr.colourfulportalsmod;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Shared geometry for the standalone colourful portal (ported from the 1.7.10 RenderStandaloneCP).
 * Used both by the in-world TESR and the inventory/hand item renderer. Client render thread only,
 * so the static scratch state is safe.
 */
@SideOnly(Side.CLIENT)
public final class StandaloneRender {

	private static final double inSet = 0.09D;

	private static BufferBuilder buffer;
	private static double xMin, xMax, yMin, yMax, zMin, zMax;
	private static double uMin, uMax, vMin, vMax;
	private static float cr = 1F, cg = 1F, cb = 1F, ca = 1F;

	private StandaloneRender() {
	}

	/** Draws the block at the current matrix origin (a 1x0.8x1 block in local [0,1] space). */
	public static void draw(Block frameBlock, int meta, boolean polygonOffset) {
		if (frameBlock == null) {
			frameBlock = Blocks.WOOL;
		}
		int color = EnumDyeColor.byMetadata(meta).getColorValue();
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;

		Minecraft mc = Minecraft.getMinecraft();
		TextureAtlasSprite frameSprite = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(frameBlock.getStateFromMeta(meta));
		TextureAtlasSprite ironSprite = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.IRON_BLOCK.getDefaultState());
		TextureAtlasSprite goldSprite = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.GOLD_BLOCK.getDefaultState());
		TextureAtlasSprite portalSprite = mc.getTextureMapBlocks().getAtlasSprite(ColourfulPortalsMod.MODID + ":blocks/portal_colour");

		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		GlStateManager.disableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		Tessellator tess = Tessellator.getInstance();
		buffer = tess.getBuffer();

		// Shift the whole block a hair toward the camera so it stops z-fighting/flickering against
		// neighbouring terrain (and the back/bottom swirl curtains stop being occluded by walls/floor).
		// This is depth-only - it does not change any colour, and keeps body/swirl relationship intact.
		if (polygonOffset) {
			GlStateManager.enablePolygonOffset();
			GlStateManager.doPolygonOffset(-3.0F, -3.0F);
		}

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		renderMainBlock(frameSprite, ironSprite, goldSprite, red, green, blue);
		tess.draw();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		renderPortal(portalSprite);
		tess.draw();

		if (polygonOffset) {
			GlStateManager.disablePolygonOffset();
		}
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.enableLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private static double p(double percent, double min, double max) {
		return (max - min) * percent + min;
	}

	private static void setColor(float r, float g, float b) {
		cr = r > 1F ? 1F : r;
		cg = g > 1F ? 1F : g;
		cb = b > 1F ? 1F : b;
		ca = 1F;
	}

	private static void setColorA(float r, float g, float b, float a) {
		cr = r;
		cg = g;
		cb = b;
		ca = a;
	}

	private static void vert(double px, double py, double pz, double pu, double pv) {
		buffer.pos(px, py, pz).tex(pu, pv).color(cr, cg, cb, ca).endVertex();
	}

	private static void useSprite(TextureAtlasSprite sprite) {
		uMin = sprite.getMinU();
		uMax = sprite.getMaxU();
		vMin = sprite.getMinV();
		vMax = sprite.getMaxV();
	}

	private static void renderMainBlock(TextureAtlasSprite frameSprite, TextureAtlasSprite ironSprite, TextureAtlasSprite goldSprite, float red, float green, float blue) {
		xMin = 0; xMax = 1; yMin = 0; yMax = 0.8D; zMin = 0; zMax = 1;
		useSprite(frameSprite);

		setColor(0.5F * red, 0.5F * green, 0.5F * blue);
		// Full-size underside (not inset 0.05) so there is no dark square seam showing through the swirl panel below it.
		vert(xMax, yMin, zMax, uMax, vMax);
		vert(xMin, yMin, zMax, uMin, vMax);
		vert(xMin, yMin, zMin, uMin, vMin);
		vert(xMax, yMin, zMin, uMax, vMin);

		setColor(0.6F * red, 0.6F * green, 0.6F * blue);
		vert(p(0.95, xMin, xMax), yMin, p(0.05, zMin, zMax), p(0.05, uMin, uMax), vMin);
		vert(p(0.95, xMin, xMax), yMax, p(0.05, zMin, zMax), p(0.05, uMin, uMax), p(0.8, vMin, vMax));
		vert(p(0.95, xMin, xMax), yMax, p(0.95, zMin, zMax), p(0.95, uMin, uMax), p(0.8, vMin, vMax));
		vert(p(0.95, xMin, xMax), yMin, p(0.95, zMin, zMax), p(0.95, uMin, uMax), vMin);

		vert(p(0.05, xMin, xMax), yMin, p(0.05, zMin, zMax), p(0.05, uMin, uMax), vMin);
		vert(p(0.05, xMin, xMax), yMin, p(0.95, zMin, zMax), p(0.95, uMin, uMax), vMin);
		vert(p(0.05, xMin, xMax), yMax, p(0.95, zMin, zMax), p(0.95, uMin, uMax), p(0.8, vMin, vMax));
		vert(p(0.05, xMin, xMax), yMax, p(0.05, zMin, zMax), p(0.05, uMin, uMax), p(0.8, vMin, vMax));

		setColor(0.8F * red, 0.8F * green, 0.8F * blue);
		vert(p(0.05, xMin, xMax), yMin, p(0.95, zMin, zMax), p(0.05, uMin, uMax), vMin);
		vert(p(0.95, xMin, xMax), yMin, p(0.95, zMin, zMax), p(0.95, uMin, uMax), vMin);
		vert(p(0.95, xMin, xMax), yMax, p(0.95, zMin, zMax), p(0.95, uMin, uMax), p(0.8, vMin, vMax));
		vert(p(0.05, xMin, xMax), yMax, p(0.95, zMin, zMax), p(0.05, uMin, uMax), p(0.8, vMin, vMax));

		vert(p(0.05, xMin, xMax), yMin, p(0.05, zMin, zMax), p(0.05, uMin, uMax), vMin);
		vert(p(0.05, xMin, xMax), yMax, p(0.05, zMin, zMax), p(0.05, uMin, uMax), p(0.8, vMin, vMax));
		vert(p(0.95, xMin, xMax), yMax, p(0.05, zMin, zMax), p(0.95, uMin, uMax), p(0.8, vMin, vMax));
		vert(p(0.95, xMin, xMax), yMin, p(0.05, zMin, zMax), p(0.95, uMin, uMax), vMin);

		setColor(red, green, blue);
		double s = 0.05D, e = 0.95D;
		renderTopFace(s, s, s, 0.25D, 0.25D, 0.25D, 0.25D, s);
		renderTopFace(0.25D, s, 0.25D, 0.25D, 0.5D, 0.25D - inSet, 0.5D, s);
		renderTopFace(0.5D, s, 0.5D, 0.25D - inSet, 0.75D, 0.25D, 0.75D, s);
		renderTopFace(0.75D, s, 0.75D, 0.25D, e, 0.25D, e, s);
		renderTopFace(s, 0.25D, s, 0.5D, 0.25D - inSet, 0.5D, 0.25D, 0.25D);
		renderTopFace(s, 0.5D, s, 0.75D, 0.25D, 0.75D, 0.25D - inSet, 0.5D);
		renderTopFace(0.75D, 0.25D, 0.75D + inSet, 0.5D, e, 0.5D, e, 0.25D);
		renderTopFace(0.75D + inSet, 0.5D, 0.75D, 0.75D, e, 0.75D, e, 0.5D);
		renderTopFace(s, 0.75D, s, e, 0.25D, e, 0.25D, 0.75D);
		renderTopFace(0.25D, 0.75D, 0.25D, e, 0.5D, e, 0.5D, 0.75D + inSet);
		renderTopFace(0.5D, 0.75D + inSet, 0.5D, e, 0.75D, e, 0.75D, 0.75D);
		renderTopFace(0.75D, 0.75D, 0.75D, e, e, e, e, 0.75D);

		setColor(1F, 1F, 1F);
		useSprite(ironSprite);
		renderOctagon(0.2D, 1.0D);

		useSprite(goldSprite);
		renderOctagon(0.3D, 0.75D);
	}

	private static void renderTopFace(double xMin1, double zMin1, double xMin2, double zMax2, double xMax3, double zMax3, double xMax4, double zMin4) {
		vert(p(xMin1, xMin, xMax), yMax, p(zMin1, zMin, zMax), p(xMin1, uMin, uMax), p(zMin1, vMin, vMax));
		vert(p(xMin2, xMin, xMax), yMax, p(zMax2, zMin, zMax), p(xMin2, uMin, uMax), p(zMax2, vMin, vMax));
		vert(p(xMax3, xMin, xMax), yMax, p(zMax3, zMin, zMax), p(xMax3, uMin, uMax), p(zMax3, vMin, vMax));
		vert(p(xMax4, xMin, xMax), yMax, p(zMin4, zMin, zMax), p(xMax4, uMin, uMax), p(zMin4, vMin, vMax));
	}

	private static void renderOctagon(double depth, double scale) {
		for (int i = 0; i < 8; i++) {
			double x1 = 0, x2 = 0, z1 = 0, z2 = 0, u1 = 0, u2 = 0, v1 = 0, v2 = 0;
			if (i == 0) { x1 = 0.25 - inSet; z1 = 0.5; x2 = 0.25; z2 = 0.75; u1 = 0.0; v1 = 0.5; u2 = 0.0; v2 = 1.0; }
			if (i == 1) { x1 = 0.5; z1 = 0.75 + inSet; x2 = 0.75; z2 = 0.75; u1 = 0.5; v1 = 1.0; u2 = 1.0; v2 = 1.0; }
			if (i == 2) { x1 = 0.75 + inSet; z1 = 0.5; x2 = 0.75; z2 = 0.25; u1 = 1.0; v1 = 0.5; u2 = 1.0; v2 = 0.0; }
			if (i == 3) { x1 = 0.5; z1 = 0.25 - inSet; x2 = 0.25; z2 = 0.25; u1 = 0.5; v1 = 0.0; u2 = 0.0; v2 = 0.0; }
			if (i == 4) { x1 = 0.25; z1 = 0.75; x2 = 0.5; z2 = 0.75 + inSet; u1 = 0.0; v1 = 1.0; u2 = 0.5; v2 = 1.0; }
			if (i == 5) { x1 = 0.75; z1 = 0.75; x2 = 0.75 + inSet; z2 = 0.5; u1 = 1.0; v1 = 1.0; u2 = 1.0; v2 = 0.5; }
			if (i == 6) { x1 = 0.75; z1 = 0.25; x2 = 0.5; z2 = 0.25 - inSet; u1 = 1.0; v1 = 0.0; u2 = 0.5; v2 = 0.0; }
			if (i == 7) { x1 = 0.25; z1 = 0.25; x2 = 0.25 - inSet; z2 = 0.5; u1 = 0.0; v1 = 0.0; u2 = 0.0; v2 = 0.5; }
			double cs = 0.5D * (1.0D - scale);
			vert(p(0.5, xMin, xMax), p(depth, yMin, yMax), p(0.5, zMin, zMax), p(0.5, uMin, uMax), p(0.5, vMin, vMax));
			vert(p(x1 * scale + cs, xMin, xMax), p(1.0 * scale + cs, yMin, yMax), p(z1 * scale + cs, zMin, zMax), p(u1, uMin, uMax), p(v1, vMin, vMax));
			vert(p(x2 * scale + cs, xMin, xMax), p(1.0 * scale + cs, yMin, yMax), p(z2 * scale + cs, zMin, zMax), p(u2, uMin, uMax), p(v2, vMin, vMax));
			vert(p(x2 * scale + cs, xMin, xMax), p(1.0 * scale + cs, yMin, yMax), p(z2 * scale + cs, zMin, zMax), p(u2, uMin, uMax), p(v2, vMin, vMax));
		}
	}

	private static void renderPortal(TextureAtlasSprite portalSprite) {
		// yMin sits a hair below the block so the side curtains and the underside panel meet at the
		// same height (no gap between them) while staying just clear of the opaque frame underside at y=0.
		xMin = 0; xMax = 1; yMin = -0.01D; yMax = 0.8D; zMin = 0; zMax = 1;
		useSprite(portalSprite);
		for (int i = 0; i < 8; i++) {
			double u1 = 0, u2 = 0, v1 = 0, v2 = 0;
			if (i == 0) { u1 = 0.0; v1 = 0.5; u2 = 0.0; v2 = 1.0; }
			if (i == 1) { u1 = 0.5; v1 = 1.0; u2 = 1.0; v2 = 1.0; }
			if (i == 2) { u1 = 1.0; v1 = 0.5; u2 = 1.0; v2 = 0.0; }
			if (i == 3) { u1 = 0.5; v1 = 0.0; u2 = 0.0; v2 = 0.0; }
			if (i == 4) { u1 = 0.0; v1 = 1.0; u2 = 0.5; v2 = 1.0; }
			if (i == 5) { u1 = 1.0; v1 = 1.0; u2 = 1.0; v2 = 0.5; }
			if (i == 6) { u1 = 1.0; v1 = 0.0; u2 = 0.5; v2 = 0.0; }
			if (i == 7) { u1 = 0.0; v1 = 0.0; u2 = 0.0; v2 = 0.5; }
			setColorA(1F, 1F, 1F, 1F);
			vert(p(0.5, xMin, xMax), p(1.2, yMin, yMax), p(0.5, zMin, zMax), p(0.5, uMin, uMax), p(0.5, vMin, vMax));
			setColorA(1F, 1F, 1F, 0.6F);
			vert(p(u1, xMin, xMax), yMax, p(v1, zMin, zMax), p(u1, uMin, uMax), p(v1, vMin, vMax));
			vert(p(u2, xMin, xMax), yMax, p(v2, zMin, zMax), p(u2, uMin, uMax), p(v2, vMin, vMax));
			vert(p(u2, xMin, xMax), yMax, p(v2, zMin, zMax), p(u2, uMin, uMax), p(v2, vMin, vMax));
		}
		setColorA(1F, 1F, 1F, 0.6F);
		vert(xMax, yMax, zMin, uMin, p(0.8, vMin, vMax));
		vert(xMax, yMax, zMax, uMax, p(0.8, vMin, vMax));
		setColorA(1F, 1F, 1F, 0.5F);
		vert(xMax, yMin, zMax, uMax, vMin);
		vert(xMax, yMin, zMin, uMin, vMin);

		vert(xMin, yMin, zMin, uMin, vMin);
		vert(xMin, yMin, zMax, uMax, vMin);
		setColorA(1F, 1F, 1F, 0.6F);
		vert(xMin, yMax, zMax, uMax, p(0.8, vMin, vMax));
		vert(xMin, yMax, zMin, uMin, p(0.8, vMin, vMax));

		vert(xMax, yMax, zMax, uMax, p(0.8, vMin, vMax));
		vert(xMin, yMax, zMax, uMin, p(0.8, vMin, vMax));
		setColorA(1F, 1F, 1F, 0.5F);
		vert(xMin, yMin, zMax, uMin, vMin);
		vert(xMax, yMin, zMax, uMax, vMin);

		vert(xMax, yMin, zMin, uMax, vMin);
		vert(xMin, yMin, zMin, uMin, vMin);
		setColorA(1F, 1F, 1F, 0.6F);
		vert(xMin, yMax, zMin, uMin, p(0.8, vMin, vMax));
		vert(xMax, yMax, zMin, uMax, p(0.8, vMin, vMax));

		// Underside panel, so the swirl shell also covers the bottom of the block
		// (the original only had side curtains + the top pool, leaving the bottom bare when viewed from below).
		// Sit it clearly below the frame's dark bottom face (avoids z-fighting) and use a stronger alpha so
		// the swirl reads over the dark underside rather than the frame showing through.
		double yb = yMin;
		setColorA(1F, 1F, 1F, 0.7F);
		vert(xMin, yb, zMin, uMin, vMin);
		vert(xMin, yb, zMax, uMin, vMax);
		vert(xMax, yb, zMax, uMax, vMax);
		vert(xMax, yb, zMin, uMax, vMin);
	}
}
