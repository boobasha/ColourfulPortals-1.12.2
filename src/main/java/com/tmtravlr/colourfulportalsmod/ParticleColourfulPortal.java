package com.tmtravlr.colourfulportalsmod;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleColourfulPortal extends Particle {

	private final float portalParticleScale;
	private final double portalPosX;
	private final double portalPosY;
	private final double portalPosZ;

	public ParticleColourfulPortal(World world, double x, double y, double z, double xVel, double yVel, double zVel) {
		this(world, x, y, z, xVel, yVel, zVel, false);
	}

	public ParticleColourfulPortal(World world, double x, double y, double z, double xVel, double yVel, double zVel, boolean large) {
		super(world, x, y, z, xVel, yVel, zVel);
		this.motionX = xVel;
		this.motionY = yVel;
		this.motionZ = zVel;
		this.portalPosX = this.posX = x;
		this.portalPosY = this.posY = y;
		this.portalPosZ = this.posZ = z;
		if (large) {
			this.portalParticleScale = this.particleScale = 2.0F;
		} else {
			this.portalParticleScale = this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F;
		}
		this.particleBlue = this.rand.nextFloat() * 0.7F + 0.3F;
		this.particleGreen = this.rand.nextFloat() * 0.5F;
		this.particleRed = this.rand.nextFloat() * 0.7F + 0.3F;
		this.particleMaxAge = (int) (Math.random() * 10.0D) + 40;
		this.canCollide = false;
		this.setParticleTextureIndex((int) (Math.random() * 8.0D));
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		float scaleFactor = ((float) this.particleAge + partialTicks) / (float) this.particleMaxAge;
		scaleFactor = 1.0F - scaleFactor;
		scaleFactor *= scaleFactor;
		scaleFactor = 1.0F - scaleFactor;
		this.particleScale = this.portalParticleScale * scaleFactor;
		super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	@Override
	public int getBrightnessForRender(float partialTick) {
		int i = super.getBrightnessForRender(partialTick);
		float f1 = (float) this.particleAge / (float) this.particleMaxAge;
		f1 *= f1;
		f1 *= f1;
		int j = i & 0xFF;
		int k = i >> 16 & 0xFF;
		k += (int) (f1 * 15.0F * 16.0F);
		if (k > 240) {
			k = 240;
		}
		return j | k << 16;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		float f = (float) this.particleAge / (float) this.particleMaxAge;
		float f1 = f;
		f = -f + f * f * 2.0F;
		f = 1.0F - f;
		this.posX = this.portalPosX + this.motionX * (double) f;
		this.posY = this.portalPosY + this.motionY * (double) f + (double) (1.0F - f1);
		this.posZ = this.portalPosZ + this.motionZ * (double) f;
		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}
	}
}
