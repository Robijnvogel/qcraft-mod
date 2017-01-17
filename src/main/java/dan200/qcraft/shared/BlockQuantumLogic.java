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
import static net.minecraft.block.BlockDirectional.FACING;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockQuantumLogic extends BlockRedstoneDiode {

    protected BlockQuantumLogic(boolean powered) {
        super(powered);
        setUnlocalizedName("qcraft:automatic_observer"); //@TODO, do this centralized
        setCreativeTab(QCraft.getCreativeTab());
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public String getLocalizedName() {
        return StatCollector.translateToLocal("tile.qcraft:automatic_observer.name");
    }

    @Override
    protected int getDelay(IBlockState state) {
        return 0;
    }

    @Override
    protected IBlockState getPoweredState(IBlockState unpoweredState) {
        EnumFacing enumfacing = (EnumFacing) unpoweredState.getValue(FACING);
        return QCraft.Blocks.powered_observer.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    protected IBlockState getUnpoweredState(IBlockState poweredState) {
        EnumFacing enumfacing = (EnumFacing) poweredState.getValue(FACING);
        return QCraft.Blocks.unpowered_observer.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return QCraft.Items.observer;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Item getItem(World worldIn, BlockPos pos) {
        return QCraft.Items.observer;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRepeaterPowered) {
            EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
            double d0 = (double) ((float) pos.getX() + 0.5F) + (double) (rand.nextFloat() - 0.5F) * 0.2D;
            double d1 = (double) ((float) pos.getY() + 0.4F) + (double) (rand.nextFloat() - 0.5F) * 0.2D;
            double d2 = (double) ((float) pos.getZ() + 0.5F) + (double) (rand.nextFloat() - 0.5F) * 0.2D;
            float f = -5.0F;

            f = f / 16.0F;
            double d3 = (double) (f * (float) enumfacing.getFrontOffsetX());
            double d4 = (double) (f * (float) enumfacing.getFrontOffsetZ());
            worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        this.notifyNeighbors(worldIn, pos, state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | ((EnumFacing) state.getValue(FACING)).getHorizontalIndex();
        return i;
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, new IProperty[]{FACING});
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
        EnumFacing direction = world.getBlockState(pos).getValue(FACING).getOpposite();
        return (side == direction);
    }

    @Override
    public boolean canProvidePower() {
        return false;
    }
    
    @Override
    protected void updateState(World world, BlockPos pos, IBlockState state) {
        super.updateState(world, pos, state);
        
        if (world.isRemote) {
            return;
        }
        
        int metadata = getMetaFromState(state);
        EnumFacing facing = state.getValue(FACING);
        Block block = state.getBlock();
        boolean powered = false;
        if (block instanceof BlockQuantumLogic) {
            BlockQuantumLogic blockQL = (BlockQuantumLogic) block;
            powered = blockQL.isRepeaterPowered;
        }       
        boolean newPowered = this.isRepeaterPowered;
        if (newPowered != powered) {
            // Set new subtype
            world.setBlockState(pos, newPowered ? this.getPoweredState(state) : this.getUnpoweredState(state), 2);

            // Notify
            world.markBlockForUpdate(pos);
            world.notifyNeighborsOfStateChange(pos, this);
        }

        // Observe
        observe(world, pos, facing, newPowered == true);
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
