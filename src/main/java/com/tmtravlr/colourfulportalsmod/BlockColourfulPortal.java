package com.tmtravlr.colourfulportalsmod;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.tmtravlr.colourfulportalsmod.ColourfulPortalsMod.ColourfulPortalLocation;

public class BlockColourfulPortal extends BlockBreakable {

	public static final PropertyInteger COLOR = PropertyInteger.create("color", 0, 15);

	private static final LinkedList<Entity> entitiesTeleported = new LinkedList<Entity>();
	private static int goCrazyX = Integer.MIN_VALUE;
	private static int goCrazyY = Integer.MIN_VALUE;
	private static int goCrazyZ = Integer.MIN_VALUE;

	public BlockColourfulPortal(Material material) {
		this(material, net.minecraft.block.SoundType.GLASS);
	}

	public BlockColourfulPortal(Material material, net.minecraft.block.SoundType sound) {
		super(material, false);
		this.setDefaultState(this.blockState.getBaseState().withProperty(COLOR, Integer.valueOf(0)));
		this.setTickRandomly(true);
		this.setSoundType(sound);
	}

	// ---------- small int-coordinate helpers (keep ported algorithms readable) ----------

	static Block blockAt(World world, int x, int y, int z) {
		return world.getBlockState(new BlockPos(x, y, z)).getBlock();
	}

	static int metaAt(World world, int x, int y, int z) {
		IBlockState s = world.getBlockState(new BlockPos(x, y, z));
		return s.getBlock().getMetaFromState(s);
	}

	static boolean isAir(World world, int x, int y, int z) {
		return world.isAirBlock(new BlockPos(x, y, z));
	}

	static void setAir(World world, int x, int y, int z) {
		world.setBlockToAir(new BlockPos(x, y, z));
	}

	// ---------- block state ----------

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { COLOR });
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(COLOR, Integer.valueOf(meta & 15));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(COLOR).intValue();
	}

	// ---------- appearance ----------

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess iba, BlockPos pos) {
		float xDiff = 0.5F;
		float zDiff = 0.5F;
		float yDiff = 0.5F;
		if (!ColourfulPortalsMod.isPortalOrFrameBlock(iba, pos.west()) || !ColourfulPortalsMod.isPortalOrFrameBlock(iba, pos.east())) {
			xDiff = 0.125F;
		} else if (!ColourfulPortalsMod.isPortalOrFrameBlock(iba, pos.north()) || !ColourfulPortalsMod.isPortalOrFrameBlock(iba, pos.south())) {
			zDiff = 0.125F;
		} else if (!ColourfulPortalsMod.isPortalOrFrameBlock(iba, pos.down()) || !ColourfulPortalsMod.isPortalOrFrameBlock(iba, pos.up())) {
			yDiff = 0.125F;
		}
		return new AxisAlignedBB(0.5F - xDiff, 0.5F - yDiff, 0.5F - zDiff, 0.5F + xDiff, 0.5F + yDiff, 0.5F + zDiff);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess iba, BlockPos pos, EnumFacing side) {
		AxisAlignedBB box = getBoundingBox(state, iba, pos);
		if (((side == EnumFacing.DOWN) && (box.minY > 0.0D)) || ((side == EnumFacing.UP) && (box.maxY < 1.0D))
				|| ((side == EnumFacing.NORTH) && (box.minZ > 0.0D)) || ((side == EnumFacing.SOUTH) && (box.maxZ < 1.0D))
				|| ((side == EnumFacing.WEST) && (box.minX > 0.0D)) || ((side == EnumFacing.EAST) && (box.maxX < 1.0D))) {
			return true;
		}
		BlockPos offset = pos.offset(side);
		return !ColourfulPortalsMod.isPortalOrFrameBlock(iba, offset);
	}

	@Override
	public int quantityDropped(java.util.Random rand) {
		return 0;
	}

	@Override
	public Item getItemDropped(IBlockState state, java.util.Random rand, int fortune) {
		return null;
	}

	// ---------- breaking / neighbour changes ----------

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		ColourfulPortalsMod.deletePortal(new ColourfulPortalLocation(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension(),
				ColourfulPortalsMod.getShiftedCPMetadata(state.getBlock(), state.getBlock().getMetaFromState(state))));
		super.breakBlock(world, pos, state);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos posIn, Block other, BlockPos fromPos) {
		int x = posIn.getX();
		int y = posIn.getY();
		int z = posIn.getZ();
		boolean xDir = true;
		boolean yDir = true;
		boolean zDir = true;
		int i = 0;
		int maxSize = ColourfulPortalsMod.maxPortalSizeCheck * ColourfulPortalsMod.maxPortalSizeCheck + 1;
		for (i = 0; (i < maxSize) && ColourfulPortalsMod.isCPBlock(blockAt(world, x + i, y, z)); i++) {
		}
		if (!ColourfulPortalsMod.isFrameBlock(blockAt(world, x + i, y, z))) {
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && ColourfulPortalsMod.isCPBlock(blockAt(world, x - i, y, z)); i++) {
		}
		if (!ColourfulPortalsMod.isFrameBlock(blockAt(world, x - i, y, z))) {
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && ColourfulPortalsMod.isCPBlock(blockAt(world, x, y + i, z)); i++) {
		}
		if (!ColourfulPortalsMod.isFrameBlock(blockAt(world, x, y + i, z))) {
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && ColourfulPortalsMod.isCPBlock(blockAt(world, x, y - i, z)); i++) {
		}
		if (!ColourfulPortalsMod.isFrameBlock(blockAt(world, x, y - i, z))) {
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && ColourfulPortalsMod.isCPBlock(blockAt(world, x, y, z + i)); i++) {
		}
		if (!ColourfulPortalsMod.isFrameBlock(blockAt(world, x, y, z + i))) {
			xDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && ColourfulPortalsMod.isCPBlock(blockAt(world, x, y, z - i)); i++) {
		}
		if (!ColourfulPortalsMod.isFrameBlock(blockAt(world, x, y, z - i))) {
			xDir = false;
			yDir = false;
		}
		if (!xDir && !yDir && !zDir) {
			ColourfulPortalsMod.CPLSet visited = new ColourfulPortalsMod.CPLSet();
			Stack<ColourfulPortalLocation> toVisit = new Stack<ColourfulPortalLocation>();
			toVisit.push(new ColourfulPortalLocation(x, y, z, world.provider.getDimension(), ColourfulPortalsMod.getShiftedCPMetadata(world, posIn)));
			visited.add(toVisit.peek());
			while (!toVisit.empty()) {
				ColourfulPortalLocation current = toVisit.pop();
				int[][] dispArray = { { 0, 0, -1 }, { 0, 0, 1 }, { 0, -1, 0 }, { 0, 1, 0 }, { -1, 0, 0 }, { 1, 0, 0 } };
				for (int[] disps : dispArray) {
					int nx = current.xPos + disps[0];
					int ny = current.yPos + disps[1];
					int nz = current.zPos + disps[2];
					if (ColourfulPortalsMod.isFramedCPBlock(blockAt(world, nx, ny, nz))) {
						ColourfulPortalLocation temp = new ColourfulPortalLocation(nx, ny, nz, world.provider.getDimension(), ColourfulPortalsMod.getShiftedCPMetadata(world, new BlockPos(nx, ny, nz)));
						if (!visited.contains(temp)) {
							toVisit.push(temp);
							visited.add(temp);
						}
					}
				}
			}
			for (ColourfulPortalLocation toDelete : visited) {
				setAir(world, toDelete.xPos, toDelete.yPos, toDelete.zPos);
			}
		}
	}

	// ---------- particles ----------

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, java.util.Random rand) {
		int blockX = pos.getX();
		int blockY = pos.getY();
		int blockZ = pos.getZ();
		int max = 2;
		boolean crazy = false;
		if ((goCrazyX == blockX) && (goCrazyY == blockY) && (goCrazyZ == blockZ)) {
			max = 50;
			crazy = true;
			goCrazyX = goCrazyY = goCrazyZ = Integer.MIN_VALUE;
		}
		for (int i = 0; i < max; i++) {
			float y = blockY + rand.nextFloat();
			float yVel = (rand.nextFloat() - 0.5F) * 0.5F;
			int dispX = rand.nextInt(2) * 2 - 1;
			int dispZ = rand.nextInt(2) * 2 - 1;
			float x = blockX + 0.5F + 0.25F * dispX;
			float xVel = rand.nextFloat() * 2.0F * dispX;
			float z = blockZ + 0.5F + 0.25F * dispZ;
			float zVel = rand.nextFloat() * 2.0F * dispZ;
			net.minecraft.client.Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleColourfulPortal(world, x, y, z, xVel, yVel, zVel, crazy));
		}
	}

	// ---------- teleport entry point ----------

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (world.isRemote) {
			return;
		}

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		// Colourful ender pearls dropped into a portal create a destination.
		if (entity instanceof EntityItem) {
			ItemStack item = ((EntityItem) entity).getItem();
			if (Item.getIdFromItem(item.getItem()) == Item.getIdFromItem(ColourfulPortalsMod.enderPearlColoured)) {
				tryToCreateDestination(world, pos, true);
				entity.setDead();
				return;
			} else if (Item.getIdFromItem(item.getItem()) == Item.getIdFromItem(ColourfulPortalsMod.enderPearlColouredReflective)) {
				tryToCreateDestination(world, pos, false);
				entity.setDead();
				return;
			}
		}

		// Work with the bottom of any riding stack.
		Entity bottom = entity.getLowestRidingEntity();
		if (bottom != entity) {
			// Dismount so the teleport behaves predictably across dimensions.
			entity.dismountRidingEntity();
		}
		Entity toTeleport = bottom;

		if (!entitySatisfiesTeleportConditions(world, toTeleport)) {
			toTeleport.getEntityData().setInteger("ColourfulPortalDelay", 10);
			if (!(toTeleport instanceof EntityPlayer)) {
				toTeleport.getEntityData().setBoolean("InColourfulPortal", true);
			}
			if (!entitiesTeleported.contains(toTeleport)) {
				entitiesTeleported.add(toTeleport);
			}
			return;
		}

		teleportColourfully(world, pos, toTeleport);

		if (!entitiesTeleported.contains(toTeleport)) {
			entitiesTeleported.add(toTeleport);
		}
	}

	// ---------- portal creation ----------

	public static boolean tryToCreatePortal(World world, BlockPos pos) {
		return tryToCreatePortal(world, pos, true);
	}

	public static boolean tryToCreatePortal(World world, BlockPos startPos, boolean addLocationToList) {
		if (world.isRemote) {
			return false;
		}
		int x = startPos.getX();
		int y = startPos.getY();
		int z = startPos.getZ();
		int maxSize = ColourfulPortalsMod.maxPortalSizeCheck * ColourfulPortalsMod.maxPortalSizeCheck - 1;

		if ((blockAt(world, x, y + 1, z) != Blocks.AIR) && (blockAt(world, x, y + 1, z) != ColourfulPortalsMod.colourfulWater)) {
			return false;
		}
		if (!ColourfulPortalsMod.isFrameBlock(blockAt(world, x, y - 1, z))) {
			return false;
		}
		Block frameBlock = blockAt(world, x, y - 1, z);
		int frameMeta = metaAt(world, x, y - 1, z);

		boolean[] dirs = { true, true, true };
		int i = 0;
		for (i = 0; (i < maxSize + 1) && ((blockAt(world, x + i, y, z) == Blocks.AIR) || (blockAt(world, x + i, y, z) == ColourfulPortalsMod.colourfulWater)); i++) {
		}
		if ((blockAt(world, x + i, y, z) != frameBlock) || (metaAt(world, x + i, y, z) != frameMeta)) {
			dirs[2] = false;
			dirs[1] = false;
		}
		for (i = 0; (i < maxSize + 1) && ((blockAt(world, x - i, y, z) == Blocks.AIR) || (blockAt(world, x - i, y, z) == ColourfulPortalsMod.colourfulWater)); i++) {
		}
		if ((blockAt(world, x - i, y, z) != frameBlock) || (metaAt(world, x - i, y, z) != frameMeta)) {
			dirs[2] = false;
			dirs[1] = false;
		}
		for (i = 0; (i < maxSize + 1) && ((blockAt(world, x, y + i, z) == Blocks.AIR) || (blockAt(world, x, y + i, z) == ColourfulPortalsMod.colourfulWater)); i++) {
		}
		if ((blockAt(world, x, y + i, z) != frameBlock) || (metaAt(world, x, y + i, z) != frameMeta)) {
			dirs[2] = false;
			dirs[0] = false;
		}
		for (i = 0; (i < maxSize + 1) && ((blockAt(world, x, y - i, z) == Blocks.AIR) || (blockAt(world, x, y - i, z) == ColourfulPortalsMod.colourfulWater)); i++) {
		}
		if ((blockAt(world, x, y - i, z) != frameBlock) || (metaAt(world, x, y - i, z) != frameMeta)) {
			dirs[2] = false;
			dirs[0] = false;
		}
		for (i = 0; (i < maxSize + 1) && ((blockAt(world, x, y, z + i) == Blocks.AIR) || (blockAt(world, x, y, z + i) == ColourfulPortalsMod.colourfulWater)); i++) {
		}
		if ((blockAt(world, x, y, z + i) != frameBlock) || (metaAt(world, x, y, z + i) != frameMeta)) {
			dirs[0] = false;
			dirs[1] = false;
		}
		for (i = 0; (i < maxSize + 1) && ((blockAt(world, x, y, z - i) == Blocks.AIR) || (blockAt(world, x, y, z - i) == ColourfulPortalsMod.colourfulWater)); i++) {
		}
		if ((blockAt(world, x, y, z - i) != frameBlock) || (metaAt(world, x, y, z - i) != frameMeta)) {
			dirs[0] = false;
			dirs[1] = false;
		}

		for (int d = 0; d < 3; d++) {
			if (!dirs[d]) {
				continue;
			}
			boolean xLook = (d == 0);
			boolean yLook = (d == 1);
			boolean zLook = (d == 2);

			ColourfulPortalsMod.CPLSet visited = new ColourfulPortalsMod.CPLSet();
			Stack<ColourfulPortalLocation> toVisit = new Stack<ColourfulPortalLocation>();
			toVisit.push(new ColourfulPortalLocation(x, y, z, world.provider.getDimension(), ColourfulPortalsMod.getShiftedCPMetadata(world, startPos)));
			visited.add(toVisit.peek());

			int maxSizeTotal = (ColourfulPortalsMod.maxPortalSizeCheck * ColourfulPortalsMod.maxPortalSizeCheck - 1)
					* (ColourfulPortalsMod.maxPortalSizeCheck * ColourfulPortalsMod.maxPortalSizeCheck - 1);
			for (int j = 0; (j < maxSizeTotal) && (!toVisit.empty()) && dirs[d]; j++) {
				ColourfulPortalLocation current = toVisit.pop();
				if (dirs[0] || dirs[2]) {
					if (badFrameNeighbour(world, current.xPos, current.yPos + 1, current.zPos, frameBlock, frameMeta, x, y, z, current.xPos, current.yPos + 1, current.zPos)) {
						if (xLook) {
							dirs[0] = false;
						} else if (zLook) {
							dirs[2] = false;
						}
					}
					if (badFrameNeighbour(world, current.xPos, current.yPos - 1, current.zPos, frameBlock, frameMeta, x, y, z, current.xPos, current.yPos - 1, current.zPos)) {
						if (xLook) {
							dirs[0] = false;
						} else if (zLook) {
							dirs[2] = false;
						}
					}
				}
				if (dirs[0] || dirs[1]) {
					if (badFrameNeighbour(world, current.xPos, current.yPos, current.zPos + 1, frameBlock, frameMeta, x, y, z, current.xPos, current.yPos, current.zPos + 1)) {
						if (xLook) {
							dirs[0] = false;
						} else if (yLook) {
							dirs[1] = false;
						}
					}
					if (badFrameNeighbour(world, current.xPos, current.yPos, current.zPos - 1, frameBlock, frameMeta, x, y, z, current.xPos, current.yPos, current.zPos - 1)) {
						if (xLook) {
							dirs[0] = false;
						} else if (yLook) {
							dirs[1] = false;
						}
					}
				}
				if (dirs[1] || dirs[2]) {
					if (badFrameNeighbour(world, current.xPos + 1, current.yPos, current.zPos, frameBlock, frameMeta, x, y, z, current.xPos + 1, current.yPos, current.zPos)) {
						if (yLook) {
							dirs[1] = false;
						} else if (zLook) {
							dirs[2] = false;
						}
					}
					if (badFrameNeighbour(world, current.xPos - 1, current.yPos, current.zPos, frameBlock, frameMeta, x, y, z, current.xPos - 1, current.yPos, current.zPos)) {
						if (yLook) {
							dirs[1] = false;
						} else if (zLook) {
							dirs[2] = false;
						}
					}
				}
				if (dirs[d] && (Math.abs(x - current.xPos) < ColourfulPortalsMod.maxPortalSizeCheck) && (y <= 256) && (y > 0)
						&& (Math.abs(z - current.zPos) < ColourfulPortalsMod.maxPortalSizeCheck)) {
					if (zLook || xLook) {
						pushIfFillable(world, current.xPos, current.yPos + 1, current.zPos, frameBlock, frameMeta, visited, toVisit);
						pushIfFillable(world, current.xPos, current.yPos - 1, current.zPos, frameBlock, frameMeta, visited, toVisit);
					}
					if (zLook || yLook) {
						pushIfFillable(world, current.xPos + 1, current.yPos, current.zPos, frameBlock, frameMeta, visited, toVisit);
						pushIfFillable(world, current.xPos - 1, current.yPos, current.zPos, frameBlock, frameMeta, visited, toVisit);
					}
					if (yLook || xLook) {
						pushIfFillable(world, current.xPos, current.yPos, current.zPos + 1, frameBlock, frameMeta, visited, toVisit);
						pushIfFillable(world, current.xPos, current.yPos, current.zPos - 1, frameBlock, frameMeta, visited, toVisit);
					}
				}
			}
			if (dirs[d]) {
				for (ColourfulPortalLocation cpl : visited) {
					if ((dirs[0] && (cpl.xPos == x)) || (dirs[1] && (cpl.yPos == y)) || (dirs[2] && (cpl.zPos == z))) {
						ColourfulPortalsMod.setFramedCPBlock(world, new BlockPos(cpl.xPos, cpl.yPos, cpl.zPos), frameBlock, frameMeta, 2);
					}
				}
				int shiftedMeta = ColourfulPortalsMod.getShiftedCPMetadataByFrameBlock(frameBlock, frameMeta);
				boolean creationSuccess = true;
				if (addLocationToList) {
					creationSuccess = ColourfulPortalsMod.addPortalToList(new ColourfulPortalLocation(x, y, z, world.provider.getDimension(), shiftedMeta));
				}
				return creationSuccess;
			}
		}
		return false;
	}

	private static boolean badFrameNeighbour(World world, int bx, int by, int bz, Block frameBlock, int frameMeta, int x, int y, int z, int cx, int cy, int cz) {
		Block nextBlock = blockAt(world, bx, by, bz);
		int nextMeta = metaAt(world, bx, by, bz);
		return ((nextBlock != frameBlock) && (nextMeta != frameMeta) && (nextBlock != Blocks.AIR) && (nextBlock != ColourfulPortalsMod.colourfulWater))
				|| (Math.abs(cx - x) > ColourfulPortalsMod.maxPortalSizeCheck) || (Math.abs(cy - y) > ColourfulPortalsMod.maxPortalSizeCheck)
				|| (Math.abs(cz - z) > ColourfulPortalsMod.maxPortalSizeCheck);
	}

	private static void pushIfFillable(World world, int nx, int ny, int nz, Block frameBlock, int frameMeta, ColourfulPortalsMod.CPLSet visited, Stack<ColourfulPortalLocation> toVisit) {
		if ((blockAt(world, nx, ny, nz) == Blocks.AIR) || (blockAt(world, nx, ny, nz) == ColourfulPortalsMod.colourfulWater)) {
			ColourfulPortalLocation temp = new ColourfulPortalLocation(nx, ny, nz, world.provider.getDimension(), ColourfulPortalsMod.getShiftedCPMetadataByFrameBlock(frameBlock, frameMeta));
			if (!visited.contains(temp)) {
				toVisit.push(temp);
				visited.add(temp);
			}
		}
	}

	public static void tryToCreateDestination(World world, BlockPos pos, boolean sameDim) {
		if (!ColourfulPortalsMod.tooManyPortals(world.getBlockState(pos).getBlock(), world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)))) {
			ColourfulPortalLocation destination = createDestination(world, sameDim, world.provider.getDimension(), ColourfulPortalsMod.getShiftedCPMetadata(world, pos));
			if (destination == null) {
				return;
			}
			ColourfulPortalsMod.addPortalToList(destination);
		}
		float soundPitch = sameDim ? 1.5F : 1.8F;
		world.playSound(null, pos, ColourfulPortalsMod.soundTeleport, SoundCategory.BLOCKS, 1.0F, soundPitch);
		goCrazyX = pos.getX();
		goCrazyY = pos.getY();
		goCrazyZ = pos.getZ();
	}

	private static ColourfulPortalLocation createDestination(World world, boolean isSameDim, int oldDim, int meta) {
		int unshiftedMeta = ColourfulPortalsMod.unshiftCPMetadata(meta);
		Block portalBlock = ColourfulPortalsMod.getCPBlockByShiftedMetadata(meta);
		Block frameBlock = ColourfulPortalsMod.getFrameBlockByShiftedMetadata(meta);
		if (portalBlock == null || frameBlock == null) {
			return null;
		}

		MinecraftServer server = world.getMinecraftServer();
		if (server == null) {
			return null;
		}

		byte var2 = 16;
		double var3 = -1.0D;

		int maxDistance = ColourfulPortalsMod.maxPortalGenerationDistance;
		if (maxDistance < 0) {
			maxDistance = -maxDistance;
		}
		if (maxDistance > 29999872) {
			maxDistance = 29999872;
		}
		java.util.Random rand = new java.util.Random();
		int var5 = rand.nextInt(maxDistance * 2) - maxDistance;
		int var6 = 0;
		int var7 = rand.nextInt(maxDistance * 2) - maxDistance;

		int dimension;
		WorldServer worldServer;
		if (isSameDim) {
			worldServer = server.getWorld(oldDim);
			dimension = oldDim;
		} else {
			WorldServer[] wServers = server.worlds;
			int indexStart = rand.nextInt(wServers.length);
			int index = indexStart;
			worldServer = null;
			do {
				if (ColourfulPortalsMod.isDimensionValidForDestination(wServers[index].provider.getDimension())) {
					worldServer = wServers[index];
				}
				index++;
				if (index >= wServers.length) {
					index -= wServers.length;
				}
			} while ((index != indexStart) && (worldServer == null));
			if (worldServer == null) {
				return null;
			}
			dimension = worldServer.provider.getDimension();
		}

		int var8 = var5;
		int var9 = var6;
		int var10 = var7;
		int var11 = 0;
		int var12 = rand.nextInt(4);
		for (int var13 = var5 - var2; var13 <= var5 + var2; var13++) {
			double var14 = var13 + 0.5D - var5;
			for (int var16 = var7 - var2; var16 <= var7 + var2; var16++) {
				double var17 = var16 + 0.5D - var7;
				label609: for (int var19 = worldServer.getActualHeight() - 1; var19 >= 0; var19--) {
					if (isAirAt(worldServer, var13, var19, var16)) {
						while ((var19 > 0) && isAirAt(worldServer, var13, var19 - 1, var16)) {
							var19--;
						}
						for (int var20 = var12; var20 < var12 + 4; var20++) {
							int var21 = var20 % 2;
							int var22 = 1 - var21;
							if (var20 % 4 >= 2) {
								var21 = -var21;
								var22 = -var22;
							}
							for (int var23 = 0; var23 < 3; var23++) {
								for (int var24 = 0; var24 < 4; var24++) {
									for (int var25 = -1; var25 < 4; var25++) {
										int var26 = var13 + (var24 - 1) * var21 + var23 * var22;
										int var27 = var19 + var25;
										int var28 = var16 + (var24 - 1) * var22 - var23 * var21;
										if (((var25 < 0) && !isSolidAt(worldServer, var26, var27, var28)) || ((var25 >= 0) && !isAirAt(worldServer, var26, var27, var28))) {
											break label609;
										}
									}
								}
							}
							double var32 = var19 + 0.5D - var6;
							double var31 = var14 * var14 + var32 * var32 + var17 * var17;
							if ((var3 < 0.0D) || (var31 < var3)) {
								var3 = var31;
								var8 = var13;
								var9 = var19;
								var10 = var16;
								var11 = var20 % 4;
							}
						}
					}
				}
			}
		}
		if (var3 < 0.0D) {
			for (int var13 = var5 - var2; var13 <= var5 + var2; var13++) {
				double var14 = var13 + 0.5D - var5;
				for (int var16 = var7 - var2; var16 <= var7 + var2; var16++) {
					double var17 = var16 + 0.5D - var7;
					label957: for (int var19 = worldServer.getActualHeight() - 1; var19 >= 0; var19--) {
						if (isAirAt(worldServer, var13, var19, var16)) {
							while ((var19 > 0) && isAirAt(worldServer, var13, var19 - 1, var16)) {
								var19--;
							}
							for (int var20 = var12; var20 < var12 + 2; var20++) {
								int var21 = var20 % 2;
								int var22 = 1 - var21;
								for (int var23 = 0; var23 < 4; var23++) {
									for (int var24 = -1; var24 < 4; var24++) {
										int var25 = var13 + (var23 - 1) * var21;
										int var26 = var19 + var24;
										int var27 = var16 + (var23 - 1) * var22;
										if (((var24 < 0) && !isSolidAt(worldServer, var25, var26, var27)) || ((var24 >= 0) && !isAirAt(worldServer, var25, var26, var27))) {
											break label957;
										}
									}
								}
								double var32 = var19 + 0.5D - var6;
								double var31 = var14 * var14 + var32 * var32 + var17 * var17;
								if ((var3 < 0.0D) || (var31 < var3)) {
									var3 = var31;
									var8 = var13;
									var9 = var19;
									var10 = var16;
									var11 = var20 % 2;
								}
							}
						}
					}
				}
			}
		}
		int var29 = var8;
		int var15 = var9;
		int var16b = var10;
		int var30 = var11 % 2;
		int var18 = 1 - var30;
		if (var11 % 4 >= 2) {
			var30 = -var30;
			var18 = -var18;
		}
		if (var3 < 0.0D) {
			if (var9 < 70) {
				var9 = 70;
			}
			if (var9 > worldServer.getActualHeight() - 10) {
				var9 = worldServer.getActualHeight() - 10;
			}
			var15 = var9;
			for (int var19 = -1; var19 <= 1; var19++) {
				for (int var20 = 1; var20 < 3; var20++) {
					for (int var21 = -1; var21 < 3; var21++) {
						int var22 = var29 + (var20 - 1) * var30 + var19 * var18;
						int var23 = var15 + var21;
						int var24 = var16b + (var20 - 1) * var18 - var19 * var30;
						boolean var33 = var21 < 0;
						setBlockAt(worldServer, var22, var23, var24, var33 ? frameBlock.getDefaultState() : Blocks.AIR.getDefaultState());
					}
				}
			}
		}
		for (int var19 = 0; var19 < 4; var19++) {
			for (int var20 = 0; var20 < 4; var20++) {
				for (int var21 = -1; var21 < 4; var21++) {
					int var22 = var29 + (var20 - 1) * var30;
					int var23 = var15 + var21;
					int var24 = var16b + (var20 - 1) * var18;
					boolean var33 = (var20 == 0) || (var20 == 3) || (var21 == -1) || (var21 == 3);
					IBlockState placeState = var33 ? frameBlock.getStateFromMeta(unshiftedMeta) : portalBlock.getStateFromMeta(unshiftedMeta);
					worldServer.setBlockState(new BlockPos(var22, var23, var24), placeState, 2);
				}
			}
			for (int var20 = 0; var20 < 4; var20++) {
				for (int var21 = -1; var21 < 4; var21++) {
					int var22 = var29 + (var20 - 1) * var30;
					int var23 = var15 + var21;
					int var24 = var16b + (var20 - 1) * var18;
					BlockPos p = new BlockPos(var22, var23, var24);
					worldServer.notifyNeighborsOfStateChange(p, worldServer.getBlockState(p).getBlock(), true);
				}
			}
		}
		return new ColourfulPortalLocation(var29, var15, var16b, dimension, meta);
	}

	private static boolean isAirAt(World w, int x, int y, int z) {
		return w.isAirBlock(new BlockPos(x, y, z));
	}

	private static boolean isSolidAt(World w, int x, int y, int z) {
		return w.getBlockState(new BlockPos(x, y, z)).getMaterial().isSolid();
	}

	private static void setBlockAt(World w, int x, int y, int z, IBlockState state) {
		w.setBlockState(new BlockPos(x, y, z), state);
	}

	public static void playColourfulTeleportSound(World world, double x, double y, double z) {
		world.playSound(null, x, y, z, ColourfulPortalsMod.soundTeleport, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}

	private boolean entitySatisfiesTeleportConditions(World world, Entity entity) {
		if (world.isRemote) {
			return false;
		}
		if ((entity instanceof EntityPlayer) && (entity.getEntityData().getInteger("ColourfulPortalPlayerDelay") < 0)) {
			return false;
		}
		if ((entity instanceof EntityPlayer) && (entity.getEntityData().getInteger("ColourfulPortalPlayerDelay") >= 10) && entity.isSneaking()) {
			return true;
		}
		return !entity.getEntityData().getBoolean("InColourfulPortal");
	}

	private Entity teleportColourfully(World world, BlockPos startPos, Entity entity) {
		ColourfulPortalLocation destination = ColourfulPortalsMod.getColourfulDestination(world, startPos);
		// If this is the only portal of its colour, the destination is this very portal.
		// Don't "teleport in place" (it just replays the sound/effect every tick).
		ColourfulPortalLocation start = ColourfulPortalsMod.findCPLocation(world, startPos);
		if (start != null && start.equals(destination)) {
			return entity;
		}
		MinecraftServer server = world.getMinecraftServer();
		if (server == null || server.getWorld(destination.dimension) == null) {
			return entity;
		}
		WorldServer newWorldServer = server.getWorld(destination.dimension);
		BlockPos destPos = new BlockPos(destination.xPos, destination.yPos, destination.zPos);
		double x = destination.xPos + 0.5D;
		double y = destination.yPos + 0.1D + (ColourfulPortalsMod.isStandaloneCPBlock(newWorldServer.getBlockState(destPos).getBlock()) ? 1.0D : 0.0D);
		double z = destination.zPos + 0.5D;

		entity.getEntityData().setInteger("ColourfulPortalDelay", 10);
		entity.getEntityData().setBoolean("InColourfulPortal", true);

		if (entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			player.getEntityData().setInteger("ColourfulPortalPlayerDelay", 0);
			teleportPlayerColourfully(newWorldServer, x, y, z, player, destination);
		} else {
			entity = teleportEntityColourfully(newWorldServer, x, y, z, entity, destination);
		}

		playColourfulTeleportSound(world, startPos.getX() + 0.5D, startPos.getY() + 0.5D, startPos.getZ() + 0.5D);
		playColourfulTeleportSound(newWorldServer, x, y, z);
		if (entity != null && !entitiesTeleported.contains(entity)) {
			entitiesTeleported.add(entity);
		}
		return entity;
	}

	private static Entity teleportEntityColourfully(WorldServer world, double x, double y, double z, Entity entity, ColourfulPortalLocation destination) {
		int dimension = destination.dimension;
		int currentDimension = entity.world.provider.getDimension();
		if (dimension != currentDimension) {
			entitiesTeleported.remove(entity);
			WorldServer oldWS = (WorldServer) entity.world;
			Entity newEntity = entity.changeDimension(dimension, new ColourfulTeleporter(oldWS, x, y, z));
			return newEntity != null ? newEntity : entity;
		}
		entity.setLocationAndAngles(x, y, z, entity.rotationYaw, 0.0F);
		return entity;
	}

	private static void teleportPlayerColourfully(WorldServer world, double x, double y, double z, EntityPlayerMP player, ColourfulPortalLocation destination) {
		int dimension = destination.dimension;
		int currentDimension = player.world.provider.getDimension();
		MinecraftServer server = player.mcServer;
		if (currentDimension != dimension) {
			server.getPlayerList().transferPlayerToDimension(player, dimension, new ColourfulTeleporter(server.getWorld(dimension), x, y, z));
			player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
		} else {
			player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
			world.updateEntityWithOptionalForce(player, false);
		}
	}

	public static void serverTick() {
		ArrayList<Entity> toRemove = new ArrayList<Entity>();
		for (Entity entity : entitiesTeleported) {
			if (entity.isDead) {
				toRemove.add(entity);
			} else {
				boolean inCP = true;
				BlockPos at = new BlockPos((int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ));
				BlockPos below = at.down();
				if (!ColourfulPortalsMod.isCPBlock(entity.world.getBlockState(at).getBlock())
						&& !ColourfulPortalsMod.isCPBlock(entity.world.getBlockState(below).getBlock())
						&& (entity.getEntityData().getInteger("ColourfulPortalDelay") > 0)) {
					entity.getEntityData().setInteger("ColourfulPortalDelay", entity.getEntityData().getInteger("ColourfulPortalDelay") - 1);
				}
				if (entity.getEntityData().getInteger("ColourfulPortalDelay") <= 0) {
					inCP = false;
				}
				if (entity instanceof EntityPlayer) {
					int delay = entity.getEntityData().getInteger("ColourfulPortalPlayerDelay");
					if (delay < 10) {
						entity.getEntityData().setInteger("ColourfulPortalPlayerDelay", delay + 1);
					}
				}
				if (!inCP) {
					entity.getEntityData().setBoolean("InColourfulPortal", false);
					if (entity instanceof EntityPlayer) {
						entity.getEntityData().setInteger("ColourfulPortalPlayerDelay", 0);
					}
					toRemove.add(entity);
				}
			}
		}
		for (Entity entity : toRemove) {
			entitiesTeleported.remove(entity);
		}
	}
}
