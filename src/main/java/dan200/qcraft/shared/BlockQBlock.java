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

import dan200.QCraft;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockQBlock extends BlockSand
        implements ITileEntityProvider, IQuantumObservable {

    public int blockRenderID;
    private static IIcon s_transparentIcon;
    private static IIcon s_swirlIcon;
    private static IIcon s_fuzzIcon;
    private static ItemStack[] s_impostorBlocks;

    public static enum Appearance {
        Block,
        Fuzz,
        Swirl
    }

    public static ItemStack[] getImpostorBlockList() {
        if (s_impostorBlocks == null) {
            s_impostorBlocks = new ItemStack[]{
                null,
                new ItemStack(Blocks.stone, 1, 0),
                new ItemStack(Blocks.grass, 1, 0),
                new ItemStack(Blocks.dirt, 1, 0),
                new ItemStack(Blocks.bedrock, 1, 0),
                new ItemStack(Blocks.sand, 1, 0),
                new ItemStack(Blocks.gravel, 1, 0),
                new ItemStack(Blocks.gold_ore, 1, 0),
                new ItemStack(Blocks.iron_ore, 1, 0),
                new ItemStack(Blocks.coal_ore, 1, 0),
                new ItemStack(Blocks.log, 1, 0),
                new ItemStack(Blocks.lapis_ore, 1, 0),
                new ItemStack(Blocks.sandstone, 1, 0),
                new ItemStack(Blocks.diamond_ore, 1, 0),
                new ItemStack(Blocks.redstone_ore, 1, 0),
                new ItemStack(Blocks.emerald_ore, 1, 0),
                new ItemStack(Blocks.ice, 1, 0),
                new ItemStack(Blocks.clay, 1, 0),
                new ItemStack(Blocks.pumpkin, 1, 0),
                new ItemStack(Blocks.melon_block, 1, 0),
                new ItemStack(Blocks.mycelium, 1, 0),
                new ItemStack(Blocks.obsidian, 1, 0), // 21
                new ItemStack(Blocks.cobblestone, 1, 0),
                new ItemStack(Blocks.planks, 1, 0),
                new ItemStack(Blocks.bookshelf, 1, 0),
                new ItemStack(Blocks.mossy_cobblestone, 1, 0),
                new ItemStack(Blocks.netherrack, 1, 0),
                new ItemStack(Blocks.soul_sand, 1, 0),
                new ItemStack(Blocks.glowstone, 1, 0),
                new ItemStack(Blocks.end_stone, 1, 0),
                new ItemStack(Blocks.iron_block, 1, 0),
                new ItemStack(Blocks.gold_block, 1, 0), // 31
                new ItemStack(Blocks.diamond_block, 1, 0),
                new ItemStack(Blocks.lapis_block, 1, 0),
                new ItemStack(Blocks.wool, 1, 0),
                new ItemStack(Blocks.glass, 1, 0),
                new ItemStack(Blocks.wool, 1, 1),
                new ItemStack(Blocks.wool, 1, 2),
                new ItemStack(Blocks.wool, 1, 3),
                new ItemStack(Blocks.wool, 1, 4),
                new ItemStack(Blocks.wool, 1, 5),
                new ItemStack(Blocks.wool, 1, 6),
                new ItemStack(Blocks.wool, 1, 7),
                new ItemStack(Blocks.wool, 1, 8),
                new ItemStack(Blocks.wool, 1, 9),
                new ItemStack(Blocks.wool, 1, 10),
                new ItemStack(Blocks.wool, 1, 11),
                new ItemStack(Blocks.wool, 1, 12),
                new ItemStack(Blocks.wool, 1, 13),
                new ItemStack(Blocks.wool, 1, 14),
                new ItemStack(Blocks.wool, 1, 15),
                new ItemStack(Blocks.log, 1, 1),
                new ItemStack(Blocks.log, 1, 2),
                new ItemStack(Blocks.log, 1, 3),
                new ItemStack(Blocks.planks, 1, 1),
                new ItemStack(Blocks.planks, 1, 2),
                new ItemStack(Blocks.planks, 1, 3),
                new ItemStack(Blocks.sandstone, 1, 1),
                new ItemStack(Blocks.sandstone, 1, 2),
                new ItemStack(Blocks.stonebrick, 1, 0),
                new ItemStack(Blocks.stonebrick, 1, 1),
                new ItemStack(Blocks.stonebrick, 1, 2),
                new ItemStack(Blocks.stonebrick, 1, 3),
                new ItemStack(Blocks.nether_brick, 1, 0),
                new ItemStack(Blocks.brick_block, 1, 0),
                new ItemStack(Blocks.redstone_block, 1, 0),
                new ItemStack(Blocks.quartz_ore, 1, 0),
                new ItemStack(Blocks.quartz_block, 1, 0),
                new ItemStack(Blocks.quartz_block, 1, 1),
                new ItemStack(Blocks.quartz_block, 1, 2),
                // New in 1.6.4!
                new ItemStack(Blocks.stained_hardened_clay, 1, 0),
                new ItemStack(Blocks.stained_hardened_clay, 1, 1),
                new ItemStack(Blocks.stained_hardened_clay, 1, 2),
                new ItemStack(Blocks.stained_hardened_clay, 1, 3),
                new ItemStack(Blocks.stained_hardened_clay, 1, 4),
                new ItemStack(Blocks.stained_hardened_clay, 1, 5),
                new ItemStack(Blocks.stained_hardened_clay, 1, 6),
                new ItemStack(Blocks.stained_hardened_clay, 1, 7),
                new ItemStack(Blocks.stained_hardened_clay, 1, 8),
                new ItemStack(Blocks.stained_hardened_clay, 1, 9),
                new ItemStack(Blocks.stained_hardened_clay, 1, 10),
                new ItemStack(Blocks.stained_hardened_clay, 1, 11),
                new ItemStack(Blocks.stained_hardened_clay, 1, 12),
                new ItemStack(Blocks.stained_hardened_clay, 1, 13),
                new ItemStack(Blocks.stained_hardened_clay, 1, 14),
                new ItemStack(Blocks.stained_hardened_clay, 1, 15),
                new ItemStack(Blocks.hay_block, 1, 0),
                new ItemStack(Blocks.hardened_clay, 1, 0),
                new ItemStack(Blocks.coal_block, 1, 0),
                // New in 1.7.2!
                new ItemStack(Blocks.log2, 1, 0),
                new ItemStack(Blocks.log2, 1, 1),
                new ItemStack(Blocks.dirt, 1, 2), // Podzol
                new ItemStack(Blocks.planks, 1, 4),
                new ItemStack(Blocks.planks, 1, 5),
                new ItemStack(Blocks.sand, 1, 1), // Red sand
                new ItemStack(Blocks.packed_ice, 1, 0),
                new ItemStack(Blocks.stained_glass, 1, 0),
                new ItemStack(Blocks.stained_glass, 1, 1),
                new ItemStack(Blocks.stained_glass, 1, 2),
                new ItemStack(Blocks.stained_glass, 1, 3),
                new ItemStack(Blocks.stained_glass, 1, 4),
                new ItemStack(Blocks.stained_glass, 1, 5),
                new ItemStack(Blocks.stained_glass, 1, 6),
                new ItemStack(Blocks.stained_glass, 1, 7),
                new ItemStack(Blocks.stained_glass, 1, 8),
                new ItemStack(Blocks.stained_glass, 1, 9),
                new ItemStack(Blocks.stained_glass, 1, 10),
                new ItemStack(Blocks.stained_glass, 1, 11),
                new ItemStack(Blocks.stained_glass, 1, 12),
                new ItemStack(Blocks.stained_glass, 1, 13),
                new ItemStack(Blocks.stained_glass, 1, 14),
                new ItemStack(Blocks.stained_glass, 1, 15),};
        }
        return s_impostorBlocks;
    }

    public static class SubType {

        public static final int STANDARD = 0;
        public static final int FIFTYFIFTY = 1;
        public static final int COUNT = 2;
    }

    public BlockQBlock() {
        setCreativeTab(QCraft.getCreativeTab());
        setHardness(5.0f);
        setResistance(10.0f);
        setStepSound(Block.soundTypeMetal);
        setUnlocalizedName("qcraft:qblock");
    }

    @Override
    public boolean getUseNeighborBrightness() {
        return true;
    }

    public int getSubType(IBlockAccess world, BlockPos pos) {
        return world.getTileEntity(pos).getBlockMetadata();
    }

    // IQuantumObservable implementation
    @Override
    public boolean isObserved(World world, BlockPos pos, EnumFacing side) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock qBlock = (TileEntityQBlock) entity;
            if (qBlock.isForceObserved(side)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void observe(World world, BlockPos pos, EnumFacing side) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock qBlock = (TileEntityQBlock) entity;
            qBlock.setForceObserved(side, true);
        }
    }

    @Override
    public void reset(World world, BlockPos pos, EnumFacing side) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock qBlock = (TileEntityQBlock) entity;
            qBlock.setForceObserved(side, false);
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess iblockaccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public int getRenderType() {
        return blockRenderID;
    }

    @Override
    public boolean isNormalCube(IBlockAccess world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        return block != null && !(block instanceof BlockCompressedPowered) && block != Blocks.ice && block != Blocks.packed_ice && block != Blocks.glass && block != Blocks.stained_glass;
    }

    @Override
    public int colorMultiplier(IBlockAccess world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        if (block == Blocks.grass) {
            return block.colorMultiplier(world, pos);
        }
        return 0xffffff;
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB bigBox, List list, Entity entity) {
        // Determine if solid
        boolean solid = false;
        int type = getImpostorType(world, pos);
        if (type > 0) {
            // Solid blocks are solid to everyone
            solid = true;
        } else if (entity instanceof EntityPlayer) {
            // Air blocks are solid to people with goggles on
            EntityPlayer player = (EntityPlayer) entity;
            if (QCraft.isPlayerWearingQuantumGoggles(player)) {
                solid = true;
            }
        }

        // Add AABB if so
        if (solid) {
            AxisAlignedBB aabb = AxisAlignedBB.fromBounds(
                    (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(),
                    (double) pos.getX() + 1.0, (double) pos.getY() + 1.0, (double) pos.getZ() + 1.0
            );
            if (aabb != null && aabb.intersectsWith(bigBox)) {
                list.add(aabb);
            }
        }
    }

    @Override
    public boolean isReplaceable(World world, BlockPos pos) {
        /*
		Appearance appearance = getAppearance( world, x, y, z );
		int type = getImpostorType( world, x, y, z );
		if( appearance == Appearance.Block && type == 0 )
		{
			return true;
		}
         */
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        Appearance appearance = getAppearance(world, pos);
        int type = getImpostorType(world, pos);
        if (appearance != Appearance.Block || type > 0) {
            super.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        } else {
            super.setBlockBounds(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
        setBlockBoundsBasedOnState(world, pos);
        return super.getCollisionBoundingBox(world, pos, state);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
        setBlockBoundsBasedOnState(world, pos);
        return super.getSelectedBoundingBox(world, pos);
    }

    @Override
    public float getBlockHardness(World world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.getBlockHardness(world, pos);
        }
        return 0.0f;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.getExplosionResistance(world, pos, exploder, explosion);
        }
        return 0.0f;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        Block block = getImpostorBlock(world, pos);
        return block != null;
    }

    @Override
    public boolean isAir(IBlockAccess world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        return block == null;
    }

    @Override
    public boolean canSustainLeaves(IBlockAccess world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.canSustainLeaves(world, pos);
        }
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockAccess world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        return block == null;
    }

    @Override
    public boolean isWood(IBlockAccess world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.isWood(world, pos);
        }
        return true;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.getFlammability(world, pos, face);
        }
        return 0;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.isFlammable(world, pos, face);
        }
        return false;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.getFireSpreadSpeed(world, pos, face);
        }
        return 0;
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.isFireSource(world, pos, side);
        }
        return false;
    }

    @Override
    public int getLightOpacity(IBlockAccess world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.getLightOpacity(world, pos);
        }
        return 0;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beaconPos) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.isBeaconBase(world, pos, beaconPos);
        }
        return false;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.getDrops(world, pos, block.getDefaultState(), fortune);
        }
        return new ArrayList<ItemStack>();
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock qblock = (TileEntityQBlock) entity;
            ItemStack item = ItemQBlock.create(qblock.getSubType(), qblock.getTypes(), qblock.getEntanglementFrequency(), 1);
            if (!world.isRemote && !world.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
            {
                if (world.rand.nextFloat() <= chance) {
                    spawnAsEntity(world, pos, item);
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean harvestable) {
        if (world.isRemote) {
            return false;
        }

        if (!player.capabilities.isCreativeMode) {
            if (EnchantmentHelper.getSilkTouchModifier(player)) {
                // Silk harvest (get qblock back)
                dropBlockAsItem(world, pos, world.getBlockState(pos), 0); //0 -> fortune doesn't matter and should always be null on a silk touch tool anyway
                //moved to "dropBlockAsItemWithChance" method
            } else {
                // Regular harvest (get impostor)
                Block block = getImpostorBlock(world, pos);
                if (block != null) {
                    int metadata = getImpostorDamage(world, pos);
                    if (harvestable) {
                        int fortune = EnchantmentHelper.getFortuneModifier(player);
                        List<ItemStack> items = getDrops(world, pos, world.getBlockState(pos), fortune);
                        Iterator<ItemStack> it = items.iterator();
                        while (it.hasNext()) {
                            ItemStack item = it.next();
                            if (!world.isRemote && !world.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
                            {
                                if (world.rand.nextFloat() <= 1) {
                                    spawnAsEntity(world, pos, item);
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.removedByPlayer(world, pos, player, harvestable);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock qblock = (TileEntityQBlock) entity;
            return ItemQBlock.create(qblock.getSubType(), qblock.getTypes(), qblock.getEntanglementFrequency(), 1);
        }
        return null;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        int subType = stack.getItemDamage();
        int metadata = subType;
        world.setBlockMetadataWithNotify(pos, metadata, 3);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random r) {
        Block block = getImpostorBlock(world, pos);
        if (block != null && block instanceof BlockSand) {
            super.updateTick(world, pos, state, r);
        }
    }

    @Override
    protected void onStartFalling(EntityFallingBlock entityFallingSand)
    {
        // Setup NBT for block to place
        World world = entityFallingSand.worldObj;        
        int x = (int) (entityFallingSand.posX - 0.5f);
        int y = (int) (entityFallingSand.posY - 0.5f);
        int z = (int) (entityFallingSand.posZ - 0.5f);
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            entity.writeToNBT(nbttagcompound);
            entityFallingSand.tileEntityData = nbttagcompound;
        }

        // Prevent the falling qBlock from dropping items
        entityFallingSand.shouldDropItem = false;
    }

    @Override
    public void onEndFalling(World world, BlockPos pos) // onStopFalling
    {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock qBlock = (TileEntityQBlock) entity;
            qBlock.hasJustFallen = true;
        }
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
        Block block = getImpostorBlock(world, pos);
        return block != null && block instanceof BlockCompressedPowered;
    }

    @Override
    public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        Block block = getImpostorBlock(world, pos);
        if (block != null && block instanceof BlockCompressedPowered) {
            return 15;
        }
        return 0;
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        Block block = getImpostorBlock(world, pos);
        if (block != null) {
            return block.getLightValue();
        }
        return 0;
    }

    public int getColorForType(EnumFacing side, int type) {
        if (type == 2) // grass
        {
            return (side == EnumFacing.UP) ? Blocks.grass.getRenderColor(Block.getBlockById(1).getDefaultState()) : 0xffffff;
        }
        return 0xffffff;
    }

    public IIcon getIconForType(EnumFacing side, int type, Appearance appearance) {
        if (appearance == Appearance.Swirl) {
            return s_swirlIcon;
        } else if (appearance == Appearance.Fuzz) {
            return s_fuzzIcon;
        } else //if( appearance == Appearance.Block )
        {
            ItemStack[] blockList = getImpostorBlockList();
            if (type >= 0 && type < blockList.length) {
                ItemStack item = blockList[type];
                if (item != null) {
                    Block block = ((ItemBlock) item.getItem()).block;
                    int damage = item.getItemDamage();
                    return block.getIcon(side, damage);
                }
            }
            return s_transparentIcon;
        }
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int type = getImpostorType(world, x, y, z);
        Appearance appearance = getAppearance(world, x, y, z);
        return getIconForType(side, type, appearance);
    }

    public static boolean s_forceGrass = false;

    @Override
    public IIcon getIcon(int side, int damage) {
        if (s_forceGrass) {
            return Blocks.grass.getIcon(side, damage);
        } else {
            return s_swirlIcon;
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        s_transparentIcon = iconRegister.registerIcon("qcraft:transparent");
        s_swirlIcon = iconRegister.registerIcon("qcraft:qblock_swirl");
        s_fuzzIcon = iconRegister.registerIcon("qcraft:qblock_fuzz");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityQBlock();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createNewTileEntity(world, 0); //because why bother passing a metadata value that is not even used?
    }

    private Appearance getAppearance(IBlockAccess world, BlockPos pos) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQBlock) {
            TileEntityQBlock quantum = (TileEntityQBlock) entity;
            return quantum.getAppearance();
        }
        return Appearance.Fuzz;
    }

    private int getImpostorType(IBlockAccess world, BlockPos pos) {
        int type = 0;
        if (pos.getY() >= 0) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity != null && entity instanceof TileEntityQBlock) {
                TileEntityQBlock quantum = (TileEntityQBlock) entity;
                type = quantum.getObservedType();
            }
        }
        return type;
    }

    public Block getImpostorBlock(IBlockAccess world, BlockPos pos) {
        // Return block
        int type = getImpostorType(world, pos);
        ItemStack[] blockList = getImpostorBlockList();
        if (type < blockList.length) {
            ItemStack item = blockList[type];
            if (item != null) {
                return Block.getBlockFromItem(item.getItem());
            }
        }
        return null;
    }

    private int getImpostorDamage(IBlockAccess world, BlockPos pos) {
        // Return damage
        int type = getImpostorType(world, pos);
        ItemStack[] blockList = getImpostorBlockList();
        if (type < blockList.length) {
            ItemStack item = blockList[type];
            if (item != null) {
                return item.getItemDamage();
            }
        }
        return 0;
    }
}
