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
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockQuantumLogic extends BlockDirectional {

    public int blockRenderID;
    private IIcon[] m_icons;

    public class SubType {

        public static final int OBSERVEROFF = 0;
        public static final int OBSERVERON = 1;
        public static final int COUNT = 2;
    }

    public int getSubType(int metadata) {
        return ((metadata >> 2) & 0x3);
    }

    protected BlockQuantumLogic() {
        super(Material.circuits);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
        setHardness(0.0F);
        setStepSound(Block.soundTypeWood);
        setUnlocalizedName("qcraft:automatic_observer");
        setCreativeTab(QCraft.getCreativeTab());
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return blockRenderID;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        if (World.doesBlockHaveSolidTopSurface(world, pos.offset(EnumFacing.DOWN))) {
            return super.canPlaceBlockAt(world, pos);
        }
        return false;
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos) {
        if (World.doesBlockHaveSolidTopSurface(world, pos.offset(EnumFacing.DOWN))) {
            return super.canBlockStay(world, pos);
        }
        return false;
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int i, int j, int k, int side) {
        int metadata = world.getBlockMetadata(i, j, k);
        int damage = getSubType(metadata);
        return getIcon(side, damage);
    }

    @Override
    public IIcon getIcon(int side, int damage) {
        int subType = damage;
        if (side == 1 && damage >= 0 && damage < m_icons.length) {
            return m_icons[damage];
        }
        return Blocks.double_stone_slab.getBlockTextureFromSide(side);
    }

    @Override
    public int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return 0;
    }

    @Override
    public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return 0;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
        Block block = world.getBlockState(pos).getBlock();
        if (block != null && block instanceof BlockDirectional) {
            BlockDirectional blockDir = (BlockDirectional) block;
            EnumFacing direction = blockDir.FACING; //@TODO cast to blockfacing if it's quantum logic then get FACING?
            return (side == direction);
        }
        return false;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (!this.canBlockStay(world, pos)) {
            if (!world.isRemote) {
                // Destroy
                this.dropBlockAsItem(world, pos, state, 0);
                world.setBlockToAir(pos);
            }
        } else {
            // Redetermine subtype
            updateOutput(world, pos);
        }
    }

    private void updateOutput(World world, BlockPos pos) {
        if (world.isRemote) {
            return;
        }

        // Redetermine subtype
        int metadata = world.getBlockMetadata(pos);
        EnumFacing direction = getDirection(metadata);
        int subType = getSubType(metadata);
        int newSubType = evaluateInput(world, pos) ? SubType.OBSERVERON : SubType.OBSERVEROFF;
        if (newSubType != subType) {
            // Set new subtype
            setDirectionAndSubType(world, pos, direction, newSubType);
            subType = newSubType;

            // Notify
            world.markBlockForUpdate(pos);
            world.notifyNeighborsOfStateChange(pos, this);
        }

        // Observe
        EnumFacing facing = direction.getOpposite();
        observe(world, pos, facing, subType == SubType.OBSERVERON);
    }

    private void setDirectionAndSubType(World world, BlockPos pos, EnumFacing direction, int subType) {
        int metadata = (direction & 0x3) + ((subType & 0x3) << 2);
        world.setBlockMetadataWithNotify(pos, metadata, 3);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        EnumFacing direction = ((MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3) + 2) % 4;
        int subType = stack.getItemDamage();
        setDirectionAndSubType(world, pos, direction, subType);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        updateOutput(world, pos);
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random r) {
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        m_icons = new IIcon[SubType.COUNT];
        m_icons[SubType.OBSERVEROFF] = iconRegister.registerIcon("qcraft:automatic_observer");
        m_icons[SubType.OBSERVERON] = iconRegister.registerIcon("qcraft:automatic_observer_on");
    }

    private boolean evaluateInput(World world, BlockPos pos) {
        int metadata = world.getBlockMetadata(pos);
        EnumFacing direction = Direction.directionToFacing[getDirection(metadata)].getOpposite();
        EnumFacing backDir = direction.getOpposite();
        return getRedstoneSignal(world, pos, backDir);
    }

    private boolean getRedstoneSignal(World world, BlockPos pos, EnumFacing dir) {
        BlockPos pos2 = pos.offset(dir);
        EnumFacing side = dir.getOpposite();
        return QuantumUtil.getRedstoneSignal(world, pos2, side);
    }

    private void observe(World world, BlockPos pos, EnumFacing dir, boolean observe) {
        BlockPos pos2 = pos.offset(dir);
        Block block = world.getBlockState(pos2).getBlock();
        if (block != null && block instanceof IQuantumObservable) {
            EnumFacing side = dir.getOpposite();
            IQuantumObservable observable = (IQuantumObservable) block;
            if (observable.isObserved(world, pos2, side) != observe) {
                if (observe) {
                    observable.observe(world, pos2, side);
                } else {
                    observable.reset(world, pos2, side);
                }
            }
        }
    }
}
