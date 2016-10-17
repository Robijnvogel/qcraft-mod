/*
Copyright 2014 Google Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package dan200.qcraft.shared;

import com.google.common.base.CaseFormat;
import dan200.QCraft;
import java.io.IOException;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileEntityQuantumComputer extends TileEntity {

    public static final EntanglementRegistry<TileEntityQuantumComputer> ComputerRegistry = new EntanglementRegistry<TileEntityQuantumComputer>();
    public static final EntanglementRegistry<TileEntityQuantumComputer> ClientComputerRegistry = new EntanglementRegistry<TileEntityQuantumComputer>();
    private static boolean tooManyPossiblePortals = false;

    public static EntanglementRegistry<TileEntityQuantumComputer> getEntanglementRegistry(World world) {
        if (!world.isRemote) {
            return ComputerRegistry;
        } else {
            return ClientComputerRegistry;
        }
    }

    public static class AreaShape {

        public int m_xMin;
        public int m_xMax;
        public int m_yMin;
        public int m_yMax;
        public int m_zMin;
        public int m_zMax;

        public boolean equals(AreaShape o) {
            return o.m_xMin == m_xMin
                    && o.m_xMax == m_xMax
                    && o.m_yMin == m_yMin
                    && o.m_yMax == m_yMax
                    && o.m_zMin == m_zMin
                    && o.m_zMax == m_zMax;
        }
    }

    public static class AreaData {

        public AreaShape m_shape;
        public Block[] m_blocks;
        public int[] m_metaData;

        public NBTTagCompound encode() {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("xmin", m_shape.m_xMin);
            nbttagcompound.setInteger("xmax", m_shape.m_xMax);
            nbttagcompound.setInteger("ymin", m_shape.m_yMin);
            nbttagcompound.setInteger("ymax", m_shape.m_yMax);
            nbttagcompound.setInteger("zmin", m_shape.m_zMin);
            nbttagcompound.setInteger("zmax", m_shape.m_zMax);

            NBTTagList blockNames = new NBTTagList();
            for (int i = 0; i < m_blocks.length; ++i) {
                String name = null;
                Block block = m_blocks[i];
                if (block != null) {
                    name = Block.blockRegistry.getNameForObject(block).getResourcePath(); //??
                }
                if (name != null && name.length() > 0) {
                    blockNames.appendTag(new NBTTagString(name));
                } else {
                    blockNames.appendTag(new NBTTagString("null"));
                }
            }
            nbttagcompound.setTag("blockNames", blockNames);

            nbttagcompound.setIntArray("metaData", m_metaData);
            return nbttagcompound;
        }

        public static AreaData decode(NBTTagCompound nbttagcompound) {
            AreaData storedData = new AreaData();
            storedData.m_shape = new AreaShape();
            storedData.m_shape.m_xMin = nbttagcompound.getInteger("xmin");
            storedData.m_shape.m_xMax = nbttagcompound.getInteger("xmax");
            storedData.m_shape.m_yMin = nbttagcompound.getInteger("ymin");
            storedData.m_shape.m_yMax = nbttagcompound.getInteger("ymax");
            storedData.m_shape.m_zMin = nbttagcompound.getInteger("zmin");
            storedData.m_shape.m_zMax = nbttagcompound.getInteger("zmax");

            int size
                    = (storedData.m_shape.m_xMax - storedData.m_shape.m_xMin + 1)
                    * (storedData.m_shape.m_yMax - storedData.m_shape.m_yMin + 1)
                    * (storedData.m_shape.m_zMax - storedData.m_shape.m_zMin + 1);
            storedData.m_blocks = new Block[size];
            if (nbttagcompound.hasKey("blockData")) {
                int[] blockIDs = nbttagcompound.getIntArray("blockData");
                for (int i = 0; i < size; ++i) {
                    storedData.m_blocks[i] = Block.getBlockById(blockIDs[i]);
                }
            } else {
                NBTTagList blockNames = nbttagcompound.getTagList("blockNames", Constants.NBT.TAG_STRING);
                for (int i = 0; i < size; ++i) {
                    String name = blockNames.getStringTagAt(i);
                    if (name.length() > 0 && !name.equals("null")) {
                        storedData.m_blocks[i] = Block.getBlockFromName(name);
                    }
                }
            }
            storedData.m_metaData = nbttagcompound.getIntArray("metaData");
            return storedData;
        }
    }

    // Shared state
    private boolean m_powered;
    private int m_entanglementFrequency;
    private int m_timeSinceEnergize;

    // Area Teleportation state
    private AreaData m_storedData;

    // Server Teleportation state
    private String m_portalID;
    private boolean m_portalNameConflict;
    private String m_remoteServerAddress;
    private String m_remoteServerName;
    private String m_remotePortalID;

    public TileEntityQuantumComputer() {
        m_powered = false;
        m_entanglementFrequency = -1;
        m_timeSinceEnergize = 0;

        m_storedData = null;

        m_portalID = null;
        m_portalNameConflict = false;
        m_remoteServerAddress = null;
        m_remoteServerName = null;
        m_remotePortalID = null;
    }

    private EntanglementRegistry<TileEntityQuantumComputer> getEntanglementRegistry() {
        return getEntanglementRegistry(worldObj);
    }

    private PortalRegistry getPortalRegistry() {
        return PortalRegistry.getPortalRegistry(worldObj);
    }

    @Override
    public void validate() {
        super.validate();
        register();
    }

    @Override
    public void invalidate() {
        unregister();
        super.invalidate();
    }

    public void onDestroy() {
        PortalLocation location = getPortal();
        if (location != null && isPortalDeployed(location)) {
            undeployPortal(location);
        }
        unregisterPortal();
    }

    // Entanglement
    private void register() {
        if (m_entanglementFrequency >= 0) {
            getEntanglementRegistry().register(m_entanglementFrequency, this, this.worldObj);
        }
    }

    private void unregister() {
        if (m_entanglementFrequency >= 0) {
            getEntanglementRegistry().unregister(m_entanglementFrequency, this, this.worldObj);
        }
    }

    public void setEntanglementFrequency(int frequency) {
        if (m_entanglementFrequency != frequency) {
            unregister();
            m_entanglementFrequency = frequency;
            register();
        }
    }

    public int getEntanglementFrequency() {
        return m_entanglementFrequency;
    }

    private TileEntityQuantumComputer findEntangledTwin() {
        if (m_entanglementFrequency >= 0) {
            List<TileEntityQuantumComputer> twins = ComputerRegistry.getEntangledObjects(m_entanglementFrequency);
            if (twins != null) {
                Iterator<TileEntityQuantumComputer> it = twins.iterator();
                while (it.hasNext()) {
                    TileEntityQuantumComputer computer = it.next();
                    if (computer != this) {
                        return computer;
                    }
                }
            }
        }
        return null;
    }

    // Area Teleportation
    public void setStoredData(AreaData data) {
        m_storedData = data;
    }

    public AreaData getStoredData() {
        return m_storedData;
    }

    private boolean isPillarBase(BlockPos pos, EnumFacing side) {
        if (pos.getY() < 0 || pos.getY() >= 256) {
            return false;
        }

        TileEntity entity = worldObj.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock quantum = (TileEntityQBlock) entity;
            EnumMap<EnumFacing, Integer> types = quantum.getTypes();
            for (EnumFacing itSide : EnumFacing.values()) {
                if (itSide == side) {
                    if (types.get(itSide) != 31) // GOLD
                    {
                        return false;
                    }
                } else if (types.get(itSide) != 21) // OBSIDIAN
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isPillar(BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() >= 256) {
            return false;
        }

        Block block = worldObj.getBlockState(pos).getBlock();
        return block == Blocks.obsidian;
    }

    private boolean isGlass(BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() >= 256) {
            return false;
        }

        Block block = worldObj.getBlockState(pos).getBlock();
        return block.getMaterial() == Material.glass && !(block instanceof BlockPane);
    }

    private AreaShape calculateShape() {
        AreaShape shape = new AreaShape();
        shape.m_xMin = -99;
        shape.m_xMax = -99;
        shape.m_yMin = 0;
        shape.m_yMax = 0;
        shape.m_zMin = -99;
        shape.m_zMax = -99;
        for (int i = 0; i < QCraft.maxQTPSize; ++i) {
            if (shape.m_xMin == -99 && isPillarBase(pos.west(i + 1), EnumFacing.EAST)) {
                shape.m_xMin = -i;
            }
            if (shape.m_xMax == -99 && isPillarBase(pos.east(i + 1), EnumFacing.WEST)) {
                shape.m_xMax = i;
            }
            if (shape.m_zMin == -99 && isPillarBase(pos.north(i + 1), EnumFacing.SOUTH)) {
                shape.m_zMin = -i;
            }
            if (shape.m_zMax == -99 && isPillarBase(pos.south(i + 1), EnumFacing.NORTH)) {
                shape.m_zMax = i;
            }
        }

        if (shape.m_xMin != -99
                && shape.m_xMax != -99
                && shape.m_zMin != -99
                && shape.m_zMax != -99) {
            // Find Y Min
            for (int i = 1; i < QCraft.maxQTPSize; ++i) {
                if (isPillar(pos.east(shape.m_xMin - 1).down(i))
                        && isPillar(pos.east(shape.m_xMax + 1).down(i))
                        && isPillar(pos.south(shape.m_zMin - 1).down(i))
                        && isPillar(pos.south(shape.m_zMax + 1).down(i))) {
                    shape.m_yMin = -i;
                } else {
                    break;
                }
            }

            // Find Y Max
            for (int i = 1; i < QCraft.maxQTPSize; ++i) {
                if (isPillar(pos.east(shape.m_xMin - 1).up(i))
                        && isPillar(pos.east(shape.m_xMax + 1).up(i))
                        && isPillar(pos.south(shape.m_zMin - 1).up(i))
                        && isPillar(pos.south(shape.m_zMax + 1).up(i))) {
                    shape.m_yMax = i;
                } else {
                    break;
                }
            }

            // Check glass caps
            if (isGlass(pos.east(shape.m_xMin - 1).up(shape.m_yMax + 1))
                    && isGlass(pos.east(shape.m_xMax + 1).up(shape.m_yMax + 1))
                    && isGlass(pos.south(shape.m_zMin - 1).up(shape.m_yMax + 1))
                    && isGlass(pos.south(shape.m_zMax + 1).up(shape.m_yMax + 1))) {
                return shape;
            }
        }
        return null;
    }

    private AreaData storeArea() {
        AreaShape shape = calculateShape();
        if (shape == null) {
            return null;
        }

        AreaData storedData = new AreaData();
        int minX = shape.m_xMin;
        int maxX = shape.m_xMax;
        int minY = shape.m_yMin;
        int maxY = shape.m_yMax;
        int minZ = shape.m_zMin;
        int maxZ = shape.m_zMax;

        int size = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        int index = 0;

        storedData.m_shape = shape;
        storedData.m_blocks = new Block[size];
        storedData.m_metaData = new int[size];
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos worldPos = pos.east(x).up(y).south(z);
                    if (!(x == 0 && x == y && y == z)) { //because why make it readable, right?
                        TileEntity tileentity = worldObj.getTileEntity(worldPos);
                        if (tileentity != null) {
                            return null;
                        }

                        Block block = worldObj.getBlockState(worldPos).getBlock(); //4. just like this
                        int meta = block.getMetaFromState(worldObj.getBlockState(worldPos)); //@TODO 1. getBlockState?
                        storedData.m_blocks[index] = block; //3. making this is redundant then?
                        storedData.m_metaData[index] = meta; //2. make this a block state as well?
                    }
                    index++;
                }
            }
        }

        return storedData;
    }

    private void notifyBlockOfNeighborChange(BlockPos pos) {
        worldObj.notifyBlockOfStateChange(pos, worldObj.getBlockState(pos).getBlock());
    }

    private void notifyEdgeBlocks(AreaShape shape) {
        // Notify the newly transported blocks on the edges of the area that their neighbours have changed
        int minX = shape.m_xMin;
        int maxX = shape.m_xMax;
        int minY = shape.m_yMin;
        int maxY = shape.m_yMax;
        int minZ = shape.m_zMin;
        int maxZ = shape.m_zMax;
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                notifyBlockOfNeighborChange(pos.east(x).up(y).south(minZ));
                notifyBlockOfNeighborChange(pos.east(x).up(y).south(maxZ));
                notifyBlockOfNeighborChange(pos.east(x).up(y).south(minZ - 1));
                notifyBlockOfNeighborChange(pos.east(x).up(y).south(maxZ + 1));
            }
        }
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                notifyBlockOfNeighborChange(pos.east(x).up(minY).south(z));
                notifyBlockOfNeighborChange(pos.east(x).up(maxY).south(z));
                notifyBlockOfNeighborChange(pos.east(x).up(minY - 1).south(z));
                notifyBlockOfNeighborChange(pos.east(x).up(maxY + 1).south(z));
            }
        }
        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                notifyBlockOfNeighborChange(pos.east(minX).up(y).south(z));
                notifyBlockOfNeighborChange(pos.east(maxX).up(y).south(z));
                notifyBlockOfNeighborChange(pos.east(minX - 1).up(y).south(z));
                notifyBlockOfNeighborChange(pos.east(maxX + 1).up(y).south(z));
            }
        }
    }

    private Set<EntityItem> getEntityItemsInArea(AreaShape shape) {
        AxisAlignedBB aabb = AxisAlignedBB.fromBounds(
                (double) (pos.getX() + shape.m_xMin),
                (double) (pos.getY() + shape.m_yMin),
                (double) (pos.getZ() + shape.m_zMin),
                (double) (pos.getX() + shape.m_xMax + 1),
                (double) (pos.getY() + shape.m_yMax + 1),
                (double) (pos.getZ() + shape.m_zMax + 1)
        );

        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);
        Set<EntityItem> set = new HashSet<EntityItem>();
        for (int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity) list.get(i);
            if (entity instanceof EntityItem && !entity.isDead) {
                set.add((EntityItem) entity);
            }
        }
        return set;
    }

    private void killNewItems(Set<EntityItem> before, Set<EntityItem> after) {
        Iterator<EntityItem> it = after.iterator();
        while (it.hasNext()) {
            EntityItem item = it.next();
            if (!item.isDead && !before.contains(item)) {
                item.setDead();
            }
        }
    }

    private void clearArea(AreaShape shape) {
        // Cache the loose entities
        Set<EntityItem> before = getEntityItemsInArea(shape);

        // Set the area around us to air, notifying the adjacent blocks
        int minX = shape.m_xMin;
        int maxX = shape.m_xMax;
        int minY = shape.m_yMin;
        int maxY = shape.m_yMax;
        int minZ = shape.m_zMin;
        int maxZ = shape.m_zMax;
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos worldPos = pos.east(x).up(y).south(z);
                    if (!(x == 0 && x == y && x == z)) {
                        worldObj.setBlockToAir(worldPos);
                    }
                }
            }
        }

        // Kill the new entities
        Set<EntityItem> after = getEntityItemsInArea(shape);
        killNewItems(before, after);

        // Notify edge blocks
        notifyEdgeBlocks(shape);
    }

    private void unpackArea(AreaData storedData) {
        // Cache the loose entities
        Set<EntityItem> before = getEntityItemsInArea(storedData.m_shape);

        // Set the area around us to the stored data
        int index = 0;
        int minX = storedData.m_shape.m_xMin;
        int maxX = storedData.m_shape.m_xMax;
        int minY = storedData.m_shape.m_yMin;
        int maxY = storedData.m_shape.m_yMax;
        int minZ = storedData.m_shape.m_zMin;
        int maxZ = storedData.m_shape.m_zMax;
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos worldPos = pos.east(x).up(y).south(z);
                    if (!(x == 0 && x == y && x == z)) {
                        Block block = storedData.m_blocks[index];
                        if (block != null) {
                            IBlockState state = block.getStateFromMeta(storedData.m_metaData[index]);
                            worldObj.setBlockState(worldPos, state, 2);
                        } else {
                            worldObj.setBlockToAir(worldPos);
                        }
                    }
                    index++;
                }
            }
        }

        // Kill the new entities
        Set<EntityItem> after = getEntityItemsInArea(storedData.m_shape);
        killNewItems(before, after);

        // Notify edge blocks
        notifyEdgeBlocks(storedData.m_shape);
    }

    private boolean checkCooling() {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos tempPos = pos.offset(side);
            Block block = worldObj.getBlockState(tempPos).getBlock();
            if (block != null && (block.getMaterial() == Material.ice || block.getMaterial() == Material.packedIce)) {
                return true;
            }
        }
        return false;
    }

    public static enum TeleportError {
        Ok,
        NoTwin,
        FrameIncomplete,
        DestinationFrameIncomplete,
        FrameMismatch,
        FrameObstructed,
        InsufficientCooling,
        AreaNotTransportable,
        DestinationNotTransportable,
        FrameDeployed,
        NameConflict,
        MultiplePossiblePortalsFound;

        public static String decode(TeleportError error) {
            if (error != Ok) {
                return "gui.qcraft:computer.error_" + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, error.toString());
            }
            return null;
        }
    }

    private TeleportError canTeleport() {
        // Check entangled:
        TileEntityQuantumComputer twin = null;
        if (m_entanglementFrequency >= 0) {
            // Find entangled twin:
            twin = findEntangledTwin();

            // Check the twin exists:
            if (twin == null) {
                return TeleportError.NoTwin;
            }
        }

        // Check the shape is big enough:
        AreaShape localShape = calculateShape();
        if (localShape == null) {
            return TeleportError.FrameIncomplete;
        }

        if (twin != null) {
            // Check the twin shape matches
            AreaShape twinShape = twin.calculateShape();
            if (twinShape == null) {
                return TeleportError.DestinationFrameIncomplete;
            }
            if (!localShape.equals(twinShape)) {
                return TeleportError.FrameMismatch;
            }
        } else // Check the stored shape matches
        {
            if (m_storedData != null) {
                if (!localShape.equals(m_storedData.m_shape)) {
                    return TeleportError.FrameMismatch;
                }
            }
        }

        // Check cooling
        if (!checkCooling()) {
            return TeleportError.InsufficientCooling;
        }

        // Store the two areas:
        AreaData localData = storeArea();
        if (localData == null) {
            return TeleportError.AreaNotTransportable;
        }

        if (twin != null) {
            AreaData twinData = twin.storeArea();
            if (twinData == null) {
                return TeleportError.DestinationNotTransportable;
            }
        }

        return TeleportError.Ok;
    }

    private TeleportError tryTeleport() {
        TeleportError error = canTeleport();
        if (error == TeleportError.Ok) {
            if (m_entanglementFrequency >= 0) {
                // Store the two areas:
                TileEntityQuantumComputer twin = findEntangledTwin();
                if (twin != null) {
                    AreaData localData = storeArea();
                    AreaData twinData = twin.storeArea();
                    if (localData != null && twinData != null) {
                        // Unpack the two areas:
                        unpackArea(twinData);
                        twin.unpackArea(localData);
                    }
                }
            } else {
                // Store the local area:
                AreaData localData = storeArea();

                // Unpack the stored area:
                if (m_storedData != null) {
                    unpackArea(m_storedData);
                } else {
                    clearArea(localData.m_shape);
                }

                m_storedData = localData;
            }

            // Effects
            worldObj.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getY() + 0.5, "mob.endermen.portal", 1.0F, 1.0F);
        }
        return error;
    }

    // Server Teleportation
    private void registerPortal() {
        PortalLocation location = findPortal();
        if (location != null) {
            if (m_portalID == null) {
                m_portalID = getPortalRegistry().getUnusedID();
                worldObj.markBlockForUpdate(pos);
            }
            m_portalNameConflict = !getPortalRegistry().register(m_portalID, location);
            EntanglementSavedData.get(this.worldObj).markDirty(); //Notify that this needs to be saved on world save
        }
        tooManyPossiblePortals = false;
    }

    private void unregisterPortal() {
        if (m_portalID != null) {
            if (!m_portalNameConflict) {
                getPortalRegistry().unregister(m_portalID);
                EntanglementSavedData.get(this.worldObj).markDirty(); //Notify that this needs to be saved on world save
            }
            m_portalNameConflict = false;
        }
    }

    public void setPortalID(String id) {
        unregisterPortal();
        m_portalID = id;
        registerPortal();
    }

    public String getPortalID() {
        return m_portalID;
    }

    public void setRemoteServerAddress(String address) {
        m_remoteServerAddress = address;
        m_remoteServerName = getPortalRegistry().getServerName(address);
    }

    public String getRemoteServerAddress() {
        return m_remoteServerAddress;
    }

    public String getRemoteServerName() {
        return m_remoteServerName;
    }

    public void cycleRemoteServerAddress(String previousAddress) {
        m_remoteServerAddress = getPortalRegistry().getServerAddressAfter(previousAddress);
        m_remoteServerName = getPortalRegistry().getServerName(m_remoteServerAddress);
    }

    public void setRemotePortalID(String id) {
        m_remotePortalID = id;
    }

    public String getRemotePortalID() {
        return m_remotePortalID;
    }

    public boolean isTeleporter() {
        if (m_entanglementFrequency >= 0) {
            return false;
        }
        if (!QCraft.canAnybodyCreatePortals()) {
            return false;
        }
        PortalLocation location = getPortal();
        return location != null;
    }

    public boolean isTeleporterEnergized() {
        return canDeactivatePortal() == TeleportError.Ok;
    }

    private boolean isPortalCorner(BlockPos pos, EnumFacing dir) {
        if (pos.getY() < 0 || pos.getY() >= 256) {
            return false;
        }

        TileEntity entity = worldObj.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock quantum = (TileEntityQBlock) entity;
            EnumMap<EnumFacing, Integer> types = quantum.getTypes();
            for (EnumFacing side : EnumFacing.values()) {
                if (side == dir || side == dir.getOpposite()) {
                    if (types.get(side) != 31) // GOLD
                    {
                        return false;
                    }
                } else if (types.get(side) != 21) // OBSIDIAN
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static class PortalCornerPair {

        public BlockPos c1Pos;
        public BlockPos c2Pos;
        public EnumFacing lookDirection;

        private PortalCornerPair(BlockPos pos1, BlockPos pos2, EnumFacing facing) {
            c1Pos = pos1;
            c2Pos = pos2;
            lookDirection = facing;
        }
    }

    public static class PortalLocation {

        public int m_dimensionID;
        public int m_x1;
        public int m_x2;
        public int m_y1;
        public int m_y2;
        public int m_z1;
        public int m_z2;

        private PortalLocation(int x1, int y1, int z1, int x2, int y2, int z2, int id) {
            m_dimensionID = id;
            m_x1 = x1;
            m_x2 = x2;
            m_y1 = y1;
            m_y2 = y2;
            m_z1 = z1;
            m_z2 = z2;
        }

        public NBTTagCompound encode() {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("dimensionID", m_dimensionID);
            nbttagcompound.setInteger("x1", m_x1);
            nbttagcompound.setInteger("y1", m_y1);
            nbttagcompound.setInteger("z1", m_z1);
            nbttagcompound.setInteger("x2", m_x2);
            nbttagcompound.setInteger("y2", m_y2);
            nbttagcompound.setInteger("z2", m_z2);
            return nbttagcompound;
        }

        public static PortalLocation decode(NBTTagCompound nbttagcompound) {
            int dimID;
            if (nbttagcompound.hasKey("dimensionID")) {
                dimID = nbttagcompound.getInteger("dimensionID");
            } else {
                dimID = 0;
            }
            int x1 = nbttagcompound.getInteger("x1");
            int y1 = nbttagcompound.getInteger("y1");
            int z1 = nbttagcompound.getInteger("z1");
            int x2 = nbttagcompound.getInteger("x2");
            int y2 = nbttagcompound.getInteger("y2");
            int z2 = nbttagcompound.getInteger("z2");
            return new PortalLocation(x1, y1, z1, x2, y2, z2, dimID);
        }
    }

    private boolean portalExistsAt(PortalLocation location) {
        EnumFacing lookDir; //direction the portal is looking
        int c1;
        int c2;
        if (location.m_x1 == location.m_x2) {
            lookDir = EnumFacing.NORTH;
            c1 = location.m_z1;
            c2 = location.m_z2;
        } else {
            lookDir = EnumFacing.WEST;
            c1 = location.m_x1;
            c2 = location.m_x2;
        }

        // Check walls from bottom to top
        for (int y = Math.min(location.m_y1, location.m_y2) + 1; y < Math.max(location.m_y1, location.m_y2); ++y) {
            if (!isGlass(new BlockPos(location.m_x1, y, location.m_z1))) {
                return false;
            }
            if (!isGlass(new BlockPos(location.m_x2, y, location.m_z2))) {
                return false;
            }
        }

        // Check ceiling and floor
        for (int xz = Math.min(c1, c2) + 1; xz < Math.max(c1, c2); ++xz) {
            if (!isGlass(new BlockPos((location.m_x1 == location.m_x2 ? location.m_x1 : xz), location.m_y1, (location.m_z1 == location.m_z2 ? location.m_z1 : xz)))) {
                return false;
            }
            if (!isGlass(new BlockPos((location.m_x1 == location.m_x2 ? location.m_x1 : xz), location.m_y2, (location.m_z1 == location.m_z2 ? location.m_z1 : xz)))) {
                return false;
            }
        }
        // Check corners
        if (!isPortalCorner(new BlockPos(location.m_x1, location.m_y1, location.m_z1), lookDir)) {
            return false;
        }
        if (!isPortalCorner(new BlockPos(location.m_x1, location.m_y2, location.m_z1), lookDir)) {
            return false;
        }
        if (!isPortalCorner(new BlockPos(location.m_x2, location.m_y1, location.m_z2), lookDir)) {
            return false;
        }
        return isPortalCorner(new BlockPos(location.m_x2, location.m_y2, location.m_z2), lookDir);
    }

    private PortalLocation getPortal() {
        if (m_portalID != null) {
            if (!m_portalNameConflict) {
                PortalLocation portal = getPortalRegistry().getPortal(m_portalID);
                if (portal != null) {
                    return portal;
                }
            } else {
                PortalLocation portal = findPortal();
                if (portal != null) {
                    return portal;
                }
            }
        }
        return null;
    }

    private ArrayList<PortalLocation> findPortalsAt(BlockPos pos) {
        ArrayList<PortalLocation> returnValue = new ArrayList();
        ArrayList<PortalCornerPair> possibleCornerPairs = new ArrayList<>();
        if (isGlass(pos)) { //must find a pair of corner blocks that are on the same line and have the same facing.
            int x1 = 0;
            int y1 = 0;
            int z1 = 0;
            for (EnumFacing dir : EnumFacing.values()) {
                BlockPos tempPos = pos.up(0);
                for (int i = 0; i < QCraft.maxPortalSize + 1; i++) {
                    tempPos = tempPos.offset(dir);
                    if (!isGlass(tempPos)) {
                        break;
                    }
                }
                if (dir.getAxisDirection() == AxisDirection.NEGATIVE) { //every first corner on every axis
                    x1 = tempPos.getX();
                    y1 = tempPos.getY();
                    z1 = tempPos.getZ();
                } else // every second corner on every axis
                {
                    if (dir.getAxis() == Axis.Y) { //if direction of search was up/down
                        for (int i = 0; i < 2; i++) {
                            EnumFacing facing = EnumFacing.getFront((i + 1) * 2);
                            if (isPortalCorner(tempPos, facing)
                                    && isPortalCorner(new BlockPos(x1, y1, z1), EnumFacing.getFront((i + 1) * 2))
                                    && Math.abs(tempPos.getY() - y1) < QCraft.maxPortalSize + 2) { //+2: +1 for the extra corner block and +1 for the "<" boolean operator
                                possibleCornerPairs.add(new PortalCornerPair(tempPos, new BlockPos(x1, y1, z1), facing));
                                break;
                            }
                        }
                    } else { //if search direction was sideways
                        EnumFacing perpenDir = dir.rotateAround(Axis.Y);
                        if (isPortalCorner(tempPos, perpenDir)
                                && isPortalCorner(new BlockPos(x1, y1, z1), perpenDir)
                                && Math.abs(tempPos.getX() - x1) < QCraft.maxPortalSize + 2
                                && Math.abs(tempPos.getZ() - z1) < QCraft.maxPortalSize + 2) {
                            possibleCornerPairs.add(new PortalCornerPair(tempPos, new BlockPos(x1, y1, z1), perpenDir));
                        }
                    }
                }
            }
        } else { //if it's a corner
            for (EnumFacing side : EnumFacing.values()) {
                if (isPortalCorner(pos, side)) {
                    continue; //skipping the two directions in which this portalCorner can not be part of a portal.
                }
                BlockPos tempPos = pos.up(0);
                for (int i = 0; i < QCraft.maxPortalSize + 1; i++) { // maximum portal size
                    tempPos = tempPos.offset(side);
                    if (!isGlass(tempPos)) {
                        break;
                    }
                }
                if (side.getAxis() == Axis.Y) { //if direction of search was up/down
                    for (int i = 0; i < 2; i++) { //northsouth OR eastwest
                        if (isPortalCorner(pos, EnumFacing.getFront((i + 1) * 2))
                                && isPortalCorner(tempPos, EnumFacing.getFront((i + 1) * 2))) {
                            possibleCornerPairs.add(new PortalCornerPair(pos, tempPos, EnumFacing.getFront((i + 1) * 2)));
                            break;
                        }
                    }
                } else { //if search direction was sideways
                    EnumFacing perpenDir = side.rotateAround(Axis.Y);
                    if (isPortalCorner(tempPos, perpenDir)) { // we already know that THIS portalcorner is in this direction, since we are skipping the other directions
                        possibleCornerPairs.add(new PortalCornerPair(pos, tempPos, perpenDir));
                    }
                }
            }
        }
        for (PortalCornerPair cornerPair : possibleCornerPairs) {
            ArrayList<PortalLocation> temp = findRestOfPortal(cornerPair);
            if (temp != null) {
                for (PortalLocation location : temp) {
                    returnValue.add(location);
                }
            }
        }
        return returnValue; //contains 0 up to 6 portal locations
    }

    private ArrayList<PortalLocation> findRestOfPortal(PortalCornerPair cornerPair) {
        ArrayList<PortalLocation> returnValue = new ArrayList();
        BlockPos pos1 = cornerPair.c1Pos.up(0);
        BlockPos pos2 = cornerPair.c2Pos.up(0); //will not get changed after this though...
        int x1 = pos1.getX(); //x = east/west
        int y1 = pos1.getY(); //y = up/down
        int z1 = pos1.getZ(); //z = north/south
        int x2 = pos2.getX();
        int y2 = pos2.getY();
        int z2 = pos2.getZ();
        EnumFacing lookDir = cornerPair.lookDirection; //direction the portal should be looking
        EnumFacing searchDir = lookDir.rotateAround(Axis.Y); //converts {2, 3, 4, 5} to {4, 4, 2, 2}
        if (Math.abs(y1 - y2) < 4 && (Math.abs(x1 - x2) < 3) && (Math.abs(z1 - z2) < 3)) { //if the portal would be too small if this pair of corners would make a portal
            return null;
        } else {
            for (EnumFacing dir : EnumFacing.values()) {
                BlockPos tempPos1 = new BlockPos(x1, y1, z1);
                BlockPos tempPos2 = new BlockPos(x2, y2, z2);
                for (int i = 0; i < QCraft.maxPortalSize + 1; i++) { //check for maximal portal size
                    tempPos1 = tempPos1.offset(dir);
                    tempPos2 = tempPos2.offset(dir);
                    if (!isGlass(tempPos1) || !isGlass(tempPos2)) { //once connected glass stops
                        break;
                    }
                }
                if (isGlass(tempPos1) || isGlass(tempPos2) //if glass hasn't stopped at both on the same distance
                        || Math.abs(y1 - tempPos1.getY()) < (dir.getAxis() == Axis.Y ? 4 : 3)) { //if the portal wouldn't be high (3) or wide (2) enough
                    continue;
                }
                if (isPortalCorner(tempPos1, lookDir) && isPortalCorner(tempPos2, lookDir)) {
                    //have to establish the last glass connection
                    int c1;
                    int c2;
                    if (x1 != x2) {
                        c1 = x1;
                        c2 = x2;
                    } else if (y1 != y2) {
                        c1 = y1;
                        c2 = y2;
                    } else {
                        c1 = z1;
                        c2 = z2;
                    }
                    //check for completeness of last horizontal or vertical glass portal-border.
                    for (int i = Math.min(c1, c2) + 1; i < Math.max(c1, c2); i++) {
                        if (!isGlass(new BlockPos((x1 == x2) ? x1 : i, (y1 == y2) ? y1 : i, (z1 == z2) ? z1 : i))) {
                            break;
                        }
                        if (i == Math.max(c1, c2) - 1) {
                            returnValue.add(new PortalLocation(x1, y1, z1,
                                    (x1 == x2) ? x1 : i, (y1 == y2) ? y1 : i, (z1 == z2) ? z1 : i,
                                    worldObj.provider.getDimensionId()));
                        }
                    }
                }
            }
            return returnValue; //contains 0 up to 2 portal locations
        }
    }

    private PortalLocation findPortal() {
        ArrayList<PortalLocation> portalLocations = new ArrayList();
        tooManyPossiblePortals = false;
        for (EnumFacing dir : EnumFacing.values()) {
            // See if this adjoining block is part of a portal:
            BlockPos tempPos = pos.offset(dir);
            if (!isGlass(tempPos) && !isPortalCorner(tempPos, EnumFacing.EAST) && !isPortalCorner(tempPos, EnumFacing.SOUTH)) {
                continue;
            }

            ArrayList<PortalLocation> tempLocations = findPortalsAt(tempPos);
            if ((tempLocations.size() == 2 && !(isPortalCorner(tempPos, EnumFacing.EAST) || isPortalCorner(tempPos, EnumFacing.SOUTH))) //block at tempPos is a glass block -> too many portals
                    || tempLocations.size() > 2) {
                tooManyPossiblePortals = true;
                return null;
            }
            portalLocations.addAll(tempLocations);
            if (portalLocations.size() > 2) {
                tooManyPossiblePortals = true;
                return null;
            }
        }

        if (portalLocations.size() < 1) {
            return null;
        } else if (portalLocations.size() == 2) {
            PortalLocation portal1 = portalLocations.get(0);
            PortalLocation portal2 = portalLocations.get(1);

            if (Math.min(portal1.m_x1, portal1.m_x2) == Math.min(portal2.m_x1, portal2.m_x2)
                    && Math.min(portal1.m_y1, portal1.m_y2) == Math.min(portal2.m_y1, portal2.m_y2)
                    && Math.min(portal1.m_z1, portal1.m_z2) == Math.min(portal2.m_z1, portal2.m_z2)) {
                return portalLocations.get(0);
            } else {
                tooManyPossiblePortals = true;
                return null;
            }
        } else if (portalLocations.size() > 2) {
            tooManyPossiblePortals = true;
            return null;
        } else {
            return portalLocations.get(0);
        }
    }

    private boolean isPortalClear(PortalLocation portal) {
        for (int y = Math.min(portal.m_y1, portal.m_y2) + 1; y < Math.max(portal.m_y1, portal.m_y2); ++y) {
            for (int x = Math.min(portal.m_x1, portal.m_x2) + 1; x < Math.max(portal.m_x1, portal.m_x2); ++x) {
                if (!worldObj.isAirBlock(new BlockPos(x, y, portal.m_z1))) {
                    return false;
                }
            }
            for (int z = Math.min(portal.m_z1, portal.m_z2) + 1; z < Math.max(portal.m_z1, portal.m_z2); ++z) {
                if (!worldObj.isAirBlock(new BlockPos(portal.m_x1, y, z))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void deployPortal(PortalLocation portal) {
        for (int y = Math.min(portal.m_y1, portal.m_y2) + 1; y < Math.max(portal.m_y1, portal.m_y2); ++y) {
            for (int x = Math.min(portal.m_x1, portal.m_x2) + 1; x < Math.max(portal.m_x1, portal.m_x2); ++x) {
                worldObj.setBlockState(new BlockPos(x, y, portal.m_z1), QCraft.Blocks.quantumPortal.getDefaultState(), 2);
            }
            for (int z = Math.min(portal.m_z1, portal.m_z2) + 1; z < Math.max(portal.m_z1, portal.m_z2); ++z) {
                worldObj.setBlockState(new BlockPos(portal.m_x1, y, z), QCraft.Blocks.quantumPortal.getDefaultState(), 2);
            }
        }
    }

    private void undeployPortal(PortalLocation portal) {
        for (int y = Math.min(portal.m_y1, portal.m_y2) + 1; y < Math.max(portal.m_y1, portal.m_y2); ++y) {
            for (int x = Math.min(portal.m_x1, portal.m_x2) + 1; x < Math.max(portal.m_x1, portal.m_x2); ++x) {
                worldObj.setBlockToAir(new BlockPos(x, y, portal.m_z1));
            }
            for (int z = Math.min(portal.m_z1, portal.m_z2) + 1; z < Math.max(portal.m_z1, portal.m_z2); ++z) {
                worldObj.setBlockToAir(new BlockPos(portal.m_x1, y, z));
            }
        }
    }

    private boolean isPortalDeployed(PortalLocation portal) {
        for (int y = Math.min(portal.m_y1, portal.m_y2) + 1; y < Math.max(portal.m_y1, portal.m_y2); ++y) {
            for (int x = Math.min(portal.m_x1, portal.m_x2) + 1; x < Math.max(portal.m_x1, portal.m_x2); ++x) {
                if (worldObj.getBlockState(new BlockPos(x, y, portal.m_z1)).getBlock() != QCraft.Blocks.quantumPortal) {
                    return false;
                }
            }
            for (int z = Math.min(portal.m_z1, portal.m_z2) + 1; z < Math.max(portal.m_z1, portal.m_z2); ++z) {
                if (worldObj.getBlockState(new BlockPos(portal.m_x1, y, z)).getBlock() != QCraft.Blocks.quantumPortal) {
                    return false;
                }
            }
        }
        return true;
    }

    private TeleportError canActivatePortal() {
        if (m_entanglementFrequency >= 0) {
            return TeleportError.FrameIncomplete;
        }
        if (!QCraft.canAnybodyCreatePortals()) {
            return TeleportError.FrameIncomplete;
        }

        tooManyPossiblePortals = false;
        PortalLocation location = getPortal();
        if (tooManyPossiblePortals) {
            tooManyPossiblePortals = false;
            return TeleportError.MultiplePossiblePortalsFound;
        }

        if (location == null) {
            return TeleportError.FrameIncomplete;
        }
        if (isPortalDeployed(location)) {
            return TeleportError.FrameDeployed;
        }
        if (m_portalNameConflict) {
            return TeleportError.NameConflict;
        }
        if (!isPortalClear(location)) {
            return TeleportError.FrameObstructed;
        }
        if (!checkCooling()) {
            return TeleportError.InsufficientCooling;
        }
        return TeleportError.Ok;
    }

    private TeleportError tryActivatePortal() {
        TeleportError error = canActivatePortal();
        if (error == TeleportError.Ok) {
            // Deploy
            PortalLocation location = getPortal();
            if (location != null) {
                deployPortal(location);
            }

            // Effects
            worldObj.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, "mob.endermen.portal", 1.0F, 1.0F);
        }
        return error;
    }

    public TeleportError canDeactivatePortal() {
        if (m_entanglementFrequency >= 0) {
            return TeleportError.FrameIncomplete;
        }
        PortalLocation location = getPortal();
        if (location == null) {
            return TeleportError.FrameIncomplete;
        }
        if (!isPortalDeployed(location)) {
            return TeleportError.FrameIncomplete;
        }
        return TeleportError.Ok;
    }

    private TeleportError tryDeactivatePortal() {
        TeleportError error = canDeactivatePortal();
        if (error == TeleportError.Ok) {
            // Deploy
            PortalLocation location = getPortal();
            if (location != null) {
                undeployPortal(location);
            }

            // Effects
            worldObj.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, "mob.endermen.portal", 1.0F, 1.0F);
        }
        return error;
    }

    // Common
    public void setRedstonePowered(boolean powered) {
        if (m_powered != powered) {
            m_powered = powered;
            if (!worldObj.isRemote) {
                if (m_powered && m_timeSinceEnergize >= 4) {
                    tryEnergize();
                }
            }
        }
    }

    public TeleportError canEnergize() {
        TeleportError error = canTeleport();
        if (error == TeleportError.Ok) {
            return error;
        }

        TeleportError serverError = canActivatePortal();
        if (serverError == TeleportError.Ok) {
            return serverError;
        }

        TeleportError deactivateError = canDeactivatePortal();
        if (deactivateError == TeleportError.Ok) {
            return deactivateError;
        }

        if (error.ordinal() >= serverError.ordinal()) {
            return error;
        } else {
            return serverError;
        }
    }

    public TeleportError tryEnergize() {
        TeleportError error = tryTeleport();
        if (error == TeleportError.Ok) {
            m_timeSinceEnergize = 0;
            return error;
        }

        TeleportError serverError = tryActivatePortal();
        if (serverError == TeleportError.Ok) {
            m_timeSinceEnergize = 0;
            return serverError;
        }

        TeleportError deactivateError = tryDeactivatePortal();
        if (deactivateError == TeleportError.Ok) {
            return deactivateError;
        }

        if (error.ordinal() >= serverError.ordinal()) {
            return error;
        } else {
            return serverError;
        }
    }

    @Override
    public void updateEntity() {
        m_timeSinceEnergize++;

        if (!worldObj.isRemote) {
            // Try to register conflicted portal
            if (m_portalNameConflict) {
                registerPortal();
            }

            // Validate existing portal
            PortalLocation location = getPortal();
            if (location != null) {
                if (!portalExistsAt(location)) {
                    if (isPortalDeployed(location)) {
                        undeployPortal(location);
                    }
                    unregisterPortal();
                    location = null;
                } else if (!checkCooling()) {
                    if (isPortalDeployed(location)) {
                        undeployPortal(location);
                    }
                }
            } else {
                unregisterPortal();
                location = null;
            }

            // Find new portal
            if (location == null) {
                registerPortal();
                location = getPortal();
            }

            // Try teleporting entities through portal
            if (location != null && isPortalDeployed(location)) {
                // Search for players
                int x1 = Math.min(location.m_x1, location.m_x2);
                int x2 = Math.max(location.m_x1, location.m_x2);
                int y1 = Math.min(location.m_y1, location.m_y2);
                int y2 = Math.max(location.m_y1, location.m_y2);
                int z1 = Math.min(location.m_z1, location.m_z2);
                int z2 = Math.max(location.m_z1, location.m_z2);
                AxisAlignedBB aabb = AxisAlignedBB.fromBounds(
                        ((double) x1) + (x1 == x2 ? 0.25 : 1),
                        ((double) y1 + 1),
                        ((double) z1) + (z1 == z2 ? 0.25 : 1),
                        ((double) x2) + (x1 == x2 ? 0.75 : 0),
                        ((double) y2),
                        ((double) z2) + (z1 == z2 ? 0.75 : 0)
                );

                List entities = worldObj.getEntitiesWithinAABB(EntityPlayer.class, aabb);
                if (entities != null && entities.size() > 0) {
                    Iterator it = entities.iterator();
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next != null && next instanceof EntityPlayer) {
                            EntityPlayer player = (EntityPlayer) next;
                            if (player.timeUntilPortal <= 0 && player.ticksExisted >= 200
                                    && player.ridingEntity == null && player.riddenByEntity == null) {
                                // Teleport them:
                                teleportPlayer(player);
                            }
                        }
                    }
                }
            }
        }
    }

    private void teleportPlayer(EntityPlayer player) {
        if (m_remoteServerAddress != null) {
            queryTeleportPlayerRemote(player);
        } else {
            teleportPlayerLocal(player, m_remotePortalID);
        }
    }

    private void queryTeleportPlayerRemote(EntityPlayer player) {
        QCraft.requestQueryGoToServer(player, this);
        player.timeUntilPortal = 50;
    }

    public void teleportPlayerRemote(EntityPlayer player, boolean takeItems) {
        teleportPlayerRemote(player, m_remoteServerAddress, m_remotePortalID, takeItems);
    }

    public static void teleportPlayerRemote(EntityPlayer player, String remoteServerAddress, String remotePortalID, boolean takeItems) {
        // Log the trip
        QCraft.log("Sending player " + player.getDisplayName() + " to server \"" + remoteServerAddress + "\"");

        NBTTagCompound luggage = new NBTTagCompound();
        if (takeItems) {
            // Remove and encode the items from the players inventory we want them to take with them
            NBTTagList items = new NBTTagList();
            InventoryPlayer playerInventory = player.inventory;
            for (int i = 0; i < playerInventory.getSizeInventory(); ++i) {
                ItemStack stack = playerInventory.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0) {
                    // Ignore entangled items
                    if (stack.getItem() == Item.getItemFromBlock(QCraft.Blocks.quantumComputer) && ItemQuantumComputer.getEntanglementFrequency(stack) >= 0) {
                        continue;
                    }
                    if (stack.getItem() == Item.getItemFromBlock(QCraft.Blocks.qBlock) && ItemQBlock.getEntanglementFrequency(stack) >= 0) {
                        continue;
                    }

                    // Store items
                    NBTTagCompound itemTag = new NBTTagCompound();
                    if (stack.getItem() == QCraft.Items.missingItem) {
                        itemTag = stack.getTagCompound();
                    } else {
                        GameRegistry.UniqueIdentifier uniqueId = GameRegistry.findUniqueIdentifierFor(stack.getItem());
                        String itemName = uniqueId.modId + ":" + uniqueId.name;
                        itemTag.setString("Name", itemName);
                        stack.writeToNBT(itemTag);
                    }
                    items.appendTag(itemTag);

                    // Remove items
                    playerInventory.setInventorySlotContents(i, null);
                }
            }

            if (items.tagCount() > 0) {
                QCraft.log("Removed " + items.tagCount() + " items from " + player.getDisplayName() + "'s inventory.");
                playerInventory.markDirty();
                luggage.setTag("items", items);
            }
        }

        // Set the destination portal ID
        if (remotePortalID != null) {
            luggage.setString("destinationPortal", remotePortalID);
        }

        try {
            // Cryptographically sign the luggage
            luggage.setString("uuid", UUID.randomUUID().toString());
            byte[] luggageData = QCraft.compressNBTToByteArray(luggage);
            byte[] luggageSignature = EncryptionRegistry.Instance.signData(luggageData);
            NBTTagCompound signedLuggage = new NBTTagCompound();
            signedLuggage.setByteArray("key", EncryptionRegistry.Instance.encodePublicKey(EncryptionRegistry.Instance.getLocalKeyPair().getPublic()));
            signedLuggage.setByteArray("luggage", luggageData);
            signedLuggage.setByteArray("signature", luggageSignature);

            // Send the player to the remote server with the luggage
            byte[] signedLuggageData = QCraft.compressNBTToByteArray(signedLuggage);
            QCraft.requestGoToServer(player, remoteServerAddress, signedLuggageData);
        } catch (IOException e) {
            throw new RuntimeException("Error encoding inventory");
        } finally {
            // Prevent the player from being warped twice
            player.timeUntilPortal = 200;
        }
    }

    public void teleportPlayerLocal(EntityPlayer player) {
        teleportPlayerLocal(player, m_remotePortalID);
    }

    public static void teleportPlayerLocal(EntityPlayer player, String portalID) {
        PortalLocation location = (portalID != null)
                ? PortalRegistry.PortalRegistry.getPortal(portalID)
                : null;

        if (location != null) {
            double xPos = ((double) location.m_x1 + location.m_x2 + 1) / 2;
            double yPos = (double) Math.min(location.m_y1, location.m_y2) + 1;
            double zPos = ((double) location.m_z1 + location.m_z2 + 1) / 2;
            if (location.m_dimensionID == player.dimension) {
                player.timeUntilPortal = 40;
                player.setPositionAndUpdate(xPos, yPos, zPos);
            } else if (player instanceof EntityPlayerMP) {
                player.timeUntilPortal = 40;
                MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(
                        (EntityPlayerMP) player,
                        location.m_dimensionID,
                        new QuantumTeleporter(
                                MinecraftServer.getServer().worldServerForDimension(location.m_dimensionID),
                                xPos, yPos, zPos
                        )
                );
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        // Read properties
        super.readFromNBT(nbttagcompound);
        m_powered = nbttagcompound.getBoolean("p");
        m_timeSinceEnergize = nbttagcompound.getInteger("tse");
        m_entanglementFrequency = nbttagcompound.getInteger("f");
        if (nbttagcompound.hasKey("d")) {
            m_storedData = AreaData.decode(nbttagcompound.getCompoundTag("d"));
        }
        if (nbttagcompound.hasKey("portalID")) {
            m_portalID = nbttagcompound.getString("portalID");
        }
        m_portalNameConflict = nbttagcompound.getBoolean("portalNameConflict");
        if (nbttagcompound.hasKey("remoteIPAddress")) {
            m_remoteServerAddress = nbttagcompound.getString("remoteIPAddress");
        }
        if (nbttagcompound.hasKey("remoteIPName")) {
            m_remoteServerName = nbttagcompound.getString("remoteIPName");
        } else {
            m_remoteServerName = m_remoteServerAddress;
        }
        if (nbttagcompound.hasKey("remotePortalID")) {
            m_remotePortalID = nbttagcompound.getString("remotePortalID");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        // Write properties
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("p", m_powered);
        nbttagcompound.setInteger("tse", m_timeSinceEnergize);
        nbttagcompound.setInteger("f", m_entanglementFrequency);
        if (m_storedData != null) {
            nbttagcompound.setTag("d", m_storedData.encode());
        }
        if (m_portalID != null) {
            nbttagcompound.setString("portalID", m_portalID);
        }
        nbttagcompound.setBoolean("portalNameConflict", m_portalNameConflict);
        if (m_remoteServerAddress != null) {
            nbttagcompound.setString("remoteIPAddress", m_remoteServerAddress);
        }
        if (m_remoteServerName != null) {
            nbttagcompound.setString("remoteIPName", m_remoteServerName);
        }
        if (m_remotePortalID != null) {
            nbttagcompound.setString("remotePortalID", m_remotePortalID);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        // Communicate networked state
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setInteger("f", m_entanglementFrequency);
        if (m_portalID != null) {
            nbttagcompound.setString("portalID", m_portalID);
        }
        nbttagcompound.setBoolean("portalNameConflict", m_portalNameConflict);
        if (m_remoteServerAddress != null) {
            nbttagcompound.setString("remoteIPAddress", m_remoteServerAddress);
        }
        if (m_remoteServerName != null) {
            nbttagcompound.setString("remoteIPName", m_remoteServerName);
        }
        if (m_remotePortalID != null) {
            nbttagcompound.setString("remotePortalID", m_remotePortalID);
        }
        return new S35PacketUpdateTileEntity(this.pos, 0, nbttagcompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        switch (packet.getTileEntityType()) // actionType
        {
            case 0: {
                // Read networked state
                NBTTagCompound nbttagcompound = packet.getNbtCompound(); // data
                setEntanglementFrequency(nbttagcompound.getInteger("f"));
                if (nbttagcompound.hasKey("portalID")) {
                    m_portalID = nbttagcompound.getString("portalID");
                } else {
                    m_portalID = null;
                }
                m_portalNameConflict = nbttagcompound.getBoolean("portalNameConflict");
                if (nbttagcompound.hasKey("remoteIPAddress")) {
                    m_remoteServerAddress = nbttagcompound.getString("remoteIPAddress");
                } else {
                    m_remoteServerAddress = null;
                }
                if (nbttagcompound.hasKey("remoteIPName")) {
                    m_remoteServerName = nbttagcompound.getString("remoteIPName");
                } else {
                    m_remoteServerName = null;
                }
                if (nbttagcompound.hasKey("remotePortalID")) {
                    m_remotePortalID = nbttagcompound.getString("remotePortalID");
                } else {
                    m_remotePortalID = null;
                }
                break;
            }
            default: {
                break;
            }
        }
    }
}
