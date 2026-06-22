package com.tmtravlr.colourfulportalsmod;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * A teleporter that simply drops the entity at an exact location, without
 * generating or searching for a vanilla portal.
 */
public class ColourfulTeleporter extends Teleporter {

	private final double x;
	private final double y;
	private final double z;

	public ColourfulTeleporter(WorldServer worldServer, double xToSet, double yToSet, double zToSet) {
		super(worldServer);
		this.x = xToSet;
		this.y = yToSet;
		this.z = zToSet;
	}

	private void place(Entity entity) {
		entity.setLocationAndAngles(this.x, this.y, this.z, entity.rotationYaw, 0.0F);
		entity.motionX = 0.0D;
		entity.motionY = 0.0D;
		entity.motionZ = 0.0D;
		entity.setSneaking(false);
	}

	@Override
	public void placeEntity(World world, Entity entity, float yaw) {
		place(entity);
	}

	@Override
	public void placeInPortal(Entity entity, float yaw) {
		place(entity);
	}

	@Override
	public boolean placeInExistingPortal(Entity entity, float yaw) {
		place(entity);
		return true;
	}

	@Override
	public boolean makePortal(Entity entity) {
		return true;
	}
}
