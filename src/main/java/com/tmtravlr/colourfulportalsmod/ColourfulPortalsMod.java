package com.tmtravlr.colourfulportalsmod;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod(modid = ColourfulPortalsMod.MODID, name = "Colourful Portals Mod", version = "1.0.1", acceptedMinecraftVersions = "[1.12,1.12.2]")
public class ColourfulPortalsMod {

	public static final String MODID = "colourfulportalsmod";

	@Mod.Instance(MODID)
	public static ColourfulPortalsMod instance;

	@SidedProxy(clientSide = "com.tmtravlr.colourfulportalsmod.ClientProxy", serverSide = "com.tmtravlr.colourfulportalsmod.CommonProxy")
	public static CommonProxy proxy;

	// Fluid + items + blocks
	public static Fluid colourfulFluid;
	public static Item bucketColourfulWaterEmpty;
	public static Item bucketColourfulWater;
	public static Item bucketColourfulWaterUnmixed;
	public static Item bucketColourfulWaterPartMixed;
	public static Item bucketColourfulWaterFirst;
	public static Item enderPearlColoured;
	public static Item enderPearlColouredReflective;
	public static Block colourfulWater;

	public static SoundEvent soundTeleport;

	public static HashMap<Integer, BlockColourfulPortal> cpBlocks = new HashMap<Integer, BlockColourfulPortal>();
	public static HashMap<Integer, BlockStandaloneCP> scpBlocks = new HashMap<Integer, BlockStandaloneCP>();
	public static HashMap<Integer, Block> frameBlocks = new HashMap<Integer, Block>();
	public static HashMap<Integer, String> frameNames = new HashMap<Integer, String>();

	// Config values
	public static int maxPortalGenerationDistance = 3000;
	public static int maxPortalsPerType = -1;
	public static int maxPortalSizeCheck = 16;
	public static int xpLevelMixingCost = 2;
	public static int xpLevelRemixingCost = 1;
	public static boolean useDestinationBlackList = true;
	public static boolean useDestinationWhiteList = false;
	public static boolean xpBottleCrafting = false;
	public static boolean addColourfulWaterToDungeonChests = true;
	public static int[] destinationBlackList = { 1 };
	public static int[] destinationWhiteList = { 0, -1 };
	public static boolean useDimensionBlackList = false;
	public static boolean useDimensionWhiteList = false;
	public static int[] dimensionBlackList = new int[0];
	public static int[] dimensionWhiteList = { 0, 1, -1 };
	public static String[] frameBlockNames = { "wool", "stained_hardened_clay", "stained_glass" };

	public static final CreativeTabs cpTab = new CreativeTabs("colourfulPortals") {
		@Override
		@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
		public ItemStack getTabIconItem() {
			return new ItemStack(ColourfulPortalsMod.bucketColourfulWater != null ? ColourfulPortalsMod.bucketColourfulWater : net.minecraft.init.Items.WATER_BUCKET);
		}
	};

	private static LinkedList<ColourfulPortalLocation> colourfulPortals = new LinkedList<ColourfulPortalLocation>();
	private File saveLocation;
	public String currentFolder = "";

	private static boolean configLoaded = false;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		instance = this;
		loadConfig();
		proxy.registerEventHandlers();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(TileEntityStandaloneCP.class, new ResourceLocation(MODID, "standalone_cp"));
		DispenserBehaviorCP.register();
		proxy.registerRenderers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	/** Loads the config. Safe to call multiple times; only loads once. */
	public static void loadConfig() {
		if (configLoaded) {
			return;
		}
		configLoaded = true;

		File configFile = new File(Loader.instance().getConfigDir(), "colourfulportals.cfg");
		Configuration config = new Configuration(configFile);
		config.load();

		maxPortalGenerationDistance = config.get("other", "Random Portal Generation Max Distance", 3000).getInt();
		maxPortalsPerType = config.get("other", "Maximum Number of Portals per Type (Colour and Material)", -1).getInt();
		maxPortalSizeCheck = config.get("other", "Maximum Portal Size (Make Bigger for Larger Portals)", 16).getInt();
		// Fixed costs (not read from config) so an existing colourfulportals.cfg can't override them:
		// Bucket of Dyes = 2 levels, Partially Enchanted Bucket of Dyes = 1 level.
		xpLevelMixingCost = 2;
		xpLevelRemixingCost = 1;
		xpBottleCrafting = config.get("other", "Allow crafting of colourful water with XP bottles (for automation)", false).getBoolean();
		addColourfulWaterToDungeonChests = config.getBoolean("Add Buckets of Colourful Water to Dungeon Chests?", "other", true,
				"If set to true, full and empty buckets of colourful water will occasionally spawn in chests.");
		if (xpLevelRemixingCost > xpLevelMixingCost) {
			xpLevelRemixingCost = xpLevelMixingCost;
		}

		config.addCustomCategoryComment("random_destination_blacklist", "If set to true, random destination portals with random dimensions\nwill not generate in any of the dimensions in this list.");
		config.addCustomCategoryComment("random_destination_whitelist", "If set to true, random destination portals with random dimensions\nwill only generate in these dimensions.");
		config.addCustomCategoryComment("dimension_blacklist", "If set to true, portals cannot be created in these dimensions at all.");
		config.addCustomCategoryComment("dimension_whitelist", "If set to true, portals can ONLY be created in the given dimensions.");

		int[] defaultBlackList = { 1 };
		int[] defaultWhiteList = { 0, -1 };
		int[] fullDefaultList = { 0, 1, -1 };
		int[] emptyList = new int[0];

		useDestinationBlackList = config.get("random_destination_blacklist", "Use this Blacklist?", true).getBoolean(true);
		useDestinationWhiteList = config.get("random_destination_whitelist", "Use this Whitelist?", false).getBoolean(false);
		destinationBlackList = config.get("random_destination_blacklist", "List of Blacklisted Dimensions for Random Generation", defaultBlackList).getIntList();
		destinationWhiteList = config.get("random_destination_whitelist", "List of Whitelisted Dimensions for Random Generation", defaultWhiteList).getIntList();

		useDimensionBlackList = config.get("dimension_blacklist", "Use this Blacklist?", false).getBoolean(false);
		useDimensionWhiteList = config.get("dimension_whitelist", "Use this Whitelist?", false).getBoolean(false);
		dimensionBlackList = config.get("dimension_blacklist", "List of Blacklisted Dimensions for all Portals", emptyList).getIntList();
		dimensionWhiteList = config.get("dimension_whitelist", "List of Whitelisted Dimensions for all Portals", fullDefaultList).getIntList();

		config.addCustomCategoryComment("portal_frame_types", "Blocks that can be used to make portals out of.\nThey should have 16 metadata types that represent colours in the same way as wool.");
		String[] defaultPortalTypes = { "wool", "stained_hardened_clay", "stained_glass" };
		frameBlockNames = config.get("portal_frame_types", "Portal Frame Blocks", defaultPortalTypes).getStringList();

		if (config.hasChanged()) {
			config.save();
		}
	}

	/** Registers the custom fluid. Safe to call multiple times. */
	public static void ensureFluid() {
		if (colourfulFluid == null) {
			colourfulFluid = new ColourfulFluid();
			FluidRegistry.registerFluid(colourfulFluid);
		}
	}

	// ===================== Helper methods (ported) =====================

	public static boolean isStandaloneCPBlock(Block block) {
		return scpBlocks.containsValue(block);
	}

	public static boolean isFramedCPBlock(Block block) {
		return cpBlocks.containsValue(block);
	}

	public static boolean isCPBlock(Block block) {
		return isStandaloneCPBlock(block) || isFramedCPBlock(block);
	}

	public static boolean isFrameBlock(Block block) {
		return frameBlocks.containsValue(block);
	}

	public static boolean isPortalOrFrameBlock(IBlockAccess iba, BlockPos pos) {
		Block block = iba.getBlockState(pos).getBlock();
		return isFramedCPBlock(block) || isFrameBlock(block);
	}

	public static int getShiftedCPMetadata(IBlockAccess iba, BlockPos pos) {
		IBlockState state = iba.getBlockState(pos);
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		return getShiftedCPMetadata(block, meta);
	}

	public static int getIndexFromShiftedMetadata(int meta) {
		return (int) Math.floor(meta / 16.0);
	}

	public static int getShiftedCPMetadata(Block block, int meta) {
		for (int i = 0; i < frameBlocks.size(); i++) {
			if (cpBlocks.get(i) == block || scpBlocks.get(i) == block) {
				return meta + 16 * i;
			}
		}
		return -1;
	}

	public static int getShiftedCPMetadataByFrameBlock(Block block, int meta) {
		for (int i = 0; i < frameBlocks.size(); i++) {
			if (frameBlocks.get(i) == block) {
				return meta + 16 * i;
			}
		}
		return -1;
	}

	public static Block getCPBlockByShiftedMetadata(int meta) {
		return cpBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getStandaloneCPBlockByShiftedMetadata(int meta) {
		return scpBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getFrameBlockByShiftedMetadata(int meta) {
		return frameBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getCPBlockByFrameBlock(Block frameBlock) {
		for (int i = 0; i < frameBlocks.size(); i++) {
			if (frameBlocks.get(i) == frameBlock) {
				return cpBlocks.get(i);
			}
		}
		return null;
	}

	public static int unshiftCPMetadata(int meta) {
		return meta % 16;
	}

	public static void setFramedCPBlock(World world, BlockPos pos, Block frameBlock, int meta, int flag) {
		int index = getIndexFromShiftedMetadata(getShiftedCPMetadataByFrameBlock(frameBlock, meta));
		Block block = cpBlocks.get(index);
		world.setBlockState(pos, block.getStateFromMeta(meta), flag);
	}

	public static boolean isDimensionValidForDestination(int dimension) {
		if (!isDimensionValidAtAll(dimension)) {
			return false;
		}
		if (useDestinationWhiteList) {
			if (destinationWhiteList.length == 0) {
				return false;
			}
			boolean inWhiteList = false;
			for (int element : destinationWhiteList) {
				if (dimension == element) {
					inWhiteList = true;
				}
			}
			if (!inWhiteList) {
				return false;
			}
		}
		if (useDestinationBlackList) {
			for (int element : destinationBlackList) {
				if (dimension == element) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isDimensionValidAtAll(int dimension) {
		if (useDimensionWhiteList) {
			if (dimensionWhiteList.length == 0) {
				return false;
			}
			boolean inWhiteList = false;
			for (int element : dimensionWhiteList) {
				if (dimension == element) {
					inWhiteList = true;
				}
			}
			if (!inWhiteList) {
				return false;
			}
		}
		if (useDimensionBlackList) {
			for (int element : dimensionBlackList) {
				if (dimension == element) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean canCreatePortal(World world, BlockPos pos, Block block, int meta) {
		if (tooManyPortals(block, meta)) {
			return false;
		}
		if (!isDimensionValidAtAll(world.provider.getDimension())) {
			return false;
		}
		return true;
	}

	public static boolean tooManyPortals(Block block, int meta) {
		if (maxPortalsPerType < 0) {
			return false;
		}
		if (maxPortalsPerType == 0) {
			return true;
		}
		int portalsWithType = 0;
		for (int i = 0; i < colourfulPortals.size(); i++) {
			if (colourfulPortals.get(i).portalMetadata == getShiftedCPMetadata(block, meta)) {
				portalsWithType++;
			}
		}
		return portalsWithType >= maxPortalsPerType;
	}

	// ===================== Portal list persistence =====================

	public void loadPortalsList() {
		FileInputStream fInput = null;
		ObjectInputStream oInput = null;
		try {
			this.saveLocation = proxy.getSaveLocation();
			if (this.saveLocation == null) {
				return;
			}
			if (this.saveLocation.exists()) {
				fInput = new FileInputStream(this.saveLocation);
				oInput = new ObjectInputStream(fInput);
				colourfulPortals = (LinkedList<ColourfulPortalLocation>) oInput.readObject();
				oInput.close();
				fInput.close();
				checkForPortalChanges();
			} else {
				this.saveLocation.createNewFile();
				colourfulPortals = new LinkedList<ColourfulPortalLocation>();
			}
		} catch (Exception e) {
			if (!(e instanceof EOFException)) {
				e.printStackTrace();
			}
			try {
				if (oInput != null) {
					oInput.close();
				}
				if (fInput != null) {
					fInput.close();
				}
			} catch (IOException ioe) {
			}
		}
	}

	private void checkForPortalChanges() {
		MinecraftServer server = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server == null) {
			return;
		}
		ArrayList<ColourfulPortalLocation> toDelete = new ArrayList<ColourfulPortalLocation>();
		for (ColourfulPortalLocation portal : colourfulPortals) {
			WorldServer currentWS = server.getWorld(portal.dimension);
			if (currentWS == null) {
				continue;
			}
			BlockPos pos = new BlockPos(portal.xPos, portal.yPos, portal.zPos);
			Block at = currentWS.getBlockState(pos).getBlock();
			if (getCPBlockByShiftedMetadata(portal.portalMetadata) != at && getStandaloneCPBlockByShiftedMetadata(portal.portalMetadata) != at) {
				if (!BlockColourfulPortal.tryToCreatePortal(currentWS, pos, false)) {
					toDelete.add(portal);
				}
			}
		}
		for (ColourfulPortalLocation deleted : toDelete) {
			colourfulPortals.remove(deleted);
		}
		savePortals();
	}

	public static ColourfulPortalLocation getColourfulDestination(World world, BlockPos pos) {
		MinecraftServer server = world.getMinecraftServer();
		if (colourfulPortals.size() > 0) {
			ColourfulPortalLocation start = findCPLocation(world, pos);
			int originalPos = colourfulPortals.indexOf(start);
			if (originalPos == -1) {
				return new ColourfulPortalLocation(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension(), getShiftedCPMetadata(world, pos));
			}
			int size = colourfulPortals.size();
			for (int i = 0; i < size; i++) {
				int index = i + originalPos + 1;
				if (index >= size) {
					index -= size;
				}
				ColourfulPortalLocation current = colourfulPortals.get(index);
				if (current.portalMetadata == start.portalMetadata) {
					if (server != null && server.getWorld(current.dimension) != null) {
						return current;
					}
				}
			}
			return start;
		}
		return new ColourfulPortalLocation(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension(), getShiftedCPMetadata(world, pos));
	}

	public static ColourfulPortalLocation findCPLocation(World world, BlockPos startPos) {
		int x = startPos.getX();
		int y = startPos.getY();
		int z = startPos.getZ();
		if (!isCPBlock(BlockColourfulPortal.blockAt(world, x, y, z))) {
			return null;
		}
		if (isStandaloneCPBlock(BlockColourfulPortal.blockAt(world, x, y, z))) {
			return new ColourfulPortalLocation(x, y, z, world.provider.getDimension(), getShiftedCPMetadata(world, startPos));
		}
		boolean xDir = true;
		boolean yDir = true;
		boolean zDir = true;
		int i = 0;
		int maxSize = maxPortalSizeCheck * maxPortalSizeCheck + 1;
		for (i = 0; (i < maxSize) && isCPBlock(BlockColourfulPortal.blockAt(world, x + i, y, z)); i++) {
		}
		if (!isFrameBlock(BlockColourfulPortal.blockAt(world, x + i, y, z))) {
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && isCPBlock(BlockColourfulPortal.blockAt(world, x - i, y, z)); i++) {
		}
		if (!isFrameBlock(BlockColourfulPortal.blockAt(world, x - i, y, z))) {
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && isCPBlock(BlockColourfulPortal.blockAt(world, x, y + i, z)); i++) {
		}
		if (!isFrameBlock(BlockColourfulPortal.blockAt(world, x, y + i, z))) {
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && isCPBlock(BlockColourfulPortal.blockAt(world, x, y - i, z)); i++) {
		}
		if (!isFrameBlock(BlockColourfulPortal.blockAt(world, x, y - i, z))) {
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && isCPBlock(BlockColourfulPortal.blockAt(world, x, y, z + i)); i++) {
		}
		if (!isFrameBlock(BlockColourfulPortal.blockAt(world, x, y, z + i))) {
			xDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && isCPBlock(BlockColourfulPortal.blockAt(world, x, y, z - i)); i++) {
		}
		if (!isFrameBlock(BlockColourfulPortal.blockAt(world, x, y, z - i))) {
			xDir = false;
			yDir = false;
		}
		if (!xDir && !yDir && !zDir) {
			return null;
		}
		CPLSet visited = new CPLSet();
		java.util.Stack<ColourfulPortalLocation> toVisit = new java.util.Stack<ColourfulPortalLocation>();
		toVisit.push(new ColourfulPortalLocation(x, y, z, world.provider.getDimension(), getShiftedCPMetadata(world, startPos)));
		visited.add(toVisit.peek());
		while (!toVisit.empty()) {
			ColourfulPortalLocation current = toVisit.pop();
			if (colourfulPortals.contains(current)) {
				return current;
			}
			int[][] dispArray = { { 0, 1, 0 }, { 0, -1, 0 }, { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 } };
			boolean[] allow = { zDir || xDir, zDir || xDir, zDir || yDir, zDir || yDir, yDir || xDir, yDir || xDir };
			for (int d = 0; d < 6; d++) {
				if (!allow[d]) {
					continue;
				}
				int nx = current.xPos + dispArray[d][0];
				int ny = current.yPos + dispArray[d][1];
				int nz = current.zPos + dispArray[d][2];
				if (isCPBlock(BlockColourfulPortal.blockAt(world, nx, ny, nz))) {
					ColourfulPortalLocation temp = new ColourfulPortalLocation(nx, ny, nz, world.provider.getDimension(), getShiftedCPMetadata(world, new BlockPos(nx, ny, nz)));
					if (!visited.contains(temp)) {
						toVisit.push(temp);
						visited.add(temp);
					}
				}
			}
		}
		return null;
	}

	public static void deletePortal(ColourfulPortalLocation locToDelete) {
		if (colourfulPortals.remove(locToDelete)) {
			savePortals();
		}
	}

	public static boolean addPortalToList(ColourfulPortalLocation newLocation) {
		if (!colourfulPortals.contains(newLocation)) {
			colourfulPortals.add(newLocation);
			savePortals();
			return true;
		}
		return false;
	}

	private static void savePortals() {
		if (instance == null || instance.saveLocation == null) {
			return;
		}
		try {
			FileOutputStream fOut = new FileOutputStream(instance.saveLocation);
			ObjectOutputStream oOut = new ObjectOutputStream(fOut);
			oOut.writeObject(colourfulPortals);
			oOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class CPLSet extends TreeSet<ColourfulPortalLocation> {
		public CPLSet() {
			super(CPLcomparator);
		}
	}

	public static Comparator<ColourfulPortalLocation> CPLcomparator = new Comparator<ColourfulPortalLocation>() {
		@Override
		public int compare(ColourfulPortalLocation first, ColourfulPortalLocation second) {
			if (first.portalMetadata != second.portalMetadata) {
				return second.portalMetadata - first.portalMetadata;
			}
			if (first.dimension != second.dimension) {
				return second.dimension - first.dimension;
			}
			if (first.xPos != second.xPos) {
				return second.xPos - first.xPos;
			}
			if (first.yPos != second.yPos) {
				return second.yPos - first.yPos;
			}
			if (first.zPos != second.zPos) {
				return second.zPos - first.zPos;
			}
			return 0;
		}
	};

	public static class ColourfulPortalLocation implements Serializable {
		private static final long serialVersionUID = 1L;
		int xPos;
		int yPos;
		int zPos;
		int dimension;
		int portalMetadata;

		public ColourfulPortalLocation(int x, int y, int z, int dim, int meta) {
			this.xPos = x;
			this.yPos = y;
			this.zPos = z;
			this.dimension = dim;
			this.portalMetadata = meta;
		}

		@Override
		public boolean equals(Object o) {
			if ((o == null) || !(o instanceof ColourfulPortalLocation)) {
				return false;
			}
			ColourfulPortalLocation other = (ColourfulPortalLocation) o;
			return (this.xPos == other.xPos) && (this.yPos == other.yPos) && (this.zPos == other.zPos) && (this.dimension == other.dimension)
					&& (this.portalMetadata == other.portalMetadata);
		}

		@Override
		public int hashCode() {
			return ((((xPos * 31) + yPos) * 31 + zPos) * 31 + dimension) * 31 + portalMetadata;
		}

		@Override
		public String toString() {
			return "CPL[meta=" + this.portalMetadata + ", x=" + this.xPos + ", y=" + this.yPos + ", z=" + this.zPos + ", dim=" + this.dimension + "]";
		}
	}
}
