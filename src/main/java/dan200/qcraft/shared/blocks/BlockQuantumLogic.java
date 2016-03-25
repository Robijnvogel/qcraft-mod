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


package dan200.qcraft.shared.blocks;

import dan200.QCraft;
import dan200.qcraft.shared.IQuantumObservable;
import dan200.qcraft.shared.QuantumUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BlockQuantumLogic extends BlockDirectional
{
    public int blockRenderID;

    public class SubType
    {
        public static final int ObserverOff = 0;
        public static final int ObserverOn = 1;
        public static final int Count = 2;
    }

    public int getSubType( int metadata )
    {
        return ( ( metadata >> 2 ) & 0x3 );
    }

    protected BlockQuantumLogic()
    {
        super( Material.circuits );
        setBlockBounds( 0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F );
        setHardness( 0.0F );
        setStepSound( Block.soundTypeWood );
        setRegistryName( "qcraft:automatic_observer" );
        setCreativeTab( QCraft.getCreativeTab() );
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return blockRenderID;
    }

    @Override
    public boolean canPlaceBlockAt( World world, BlockPos blockPos )
    {
        if( World.doesBlockHaveSolidTopSurface( world, new BlockPos(blockPos.getX(), blockPos.getY()-1, blockPos.getZ()) ) )
        {
            return super.canPlaceBlockAt( world, blockPos );
        }
        return false;
    }

    @Override
    public boolean canBlockStay( World world, BlockPos blockPos )
    {
        if( World.doesBlockHaveSolidTopSurface( world, new BlockPos(blockPos.getX(), blockPos.getY()-1, blockPos.getZ()) ) )
        {
            return super.canBlockStay( world, blockPos );
        }
        return false;
    }

    @Override
    public int isProvidingStrongPower( IBlockAccess world, BlockPos blockPos, EnumFacing side )
    {
        return 0;
    }

    @Override
    public int isProvidingWeakPower( IBlockAccess world, BlockPos blockPos, EnumFacing side )
    {
        return 0;
    }

    @Override
    public boolean canConnectRedstone( IBlockAccess world, BlockPos blockPos, EnumFacing side )
    {
        IBlockState blockState = world.getBlockState(blockPos);
        int metadata = getMetaFromState( blockState );
        EnumFacing direction = getStateFromMeta( metadata ).getValue(FACING).getOpposite();
        return ( side == direction );
    }

    @Override
    public boolean canProvidePower()
    {
        return true;
    }

    @Override
    public void onNeighborBlockChange( World world, BlockPos blockPos, IBlockState blockState, Block block )
    {
        if( !this.canBlockStay( world, blockPos ) )
        {
            if( !world.isRemote )
            {
                // Destroy
                this.dropBlockAsItem( world, blockPos, blockState, 0 );
                world.setBlockToAir( blockPos );
            }
        }
        else
        {
            // Redetermine subtype
            updateOutput( world, blockPos );
        }
    }

    private void updateOutput( World world, BlockPos blockPos )
    {
        if( world.isRemote )
        {
            return;
        }

        // Redetermine subtype
        IBlockState blockState = world.getBlockState(blockPos);
        int metadata = getMetaFromState( blockState );
        EnumFacing direction = getStateFromMeta( metadata ).getValue(FACING);
        int subType = getSubType( metadata );
        int newSubType = evaluateInput( world, blockPos ) ? SubType.ObserverOn : SubType.ObserverOff;
        if( newSubType != subType )
        {
            // Set new subtype
            setDirectionAndSubType( world, blockPos, direction, newSubType );
            subType = newSubType;

            // Notify
            world.markBlockForUpdate( blockPos );
            world.notifyNeighborsOfStateChange(blockPos, this );
        }

        // Observe
        EnumFacing facing = direction.getOpposite();
        observe( world, blockPos, facing, subType == SubType.ObserverOn );
    }

    private void setDirectionAndSubType( World world, BlockPos blockPos, EnumFacing direction, int subType )
    {
        int metadata = ( direction.getIndex() & 0x3 ) + ( ( subType & 0x3 ) << 2 );
        world.setBlockState( blockPos, getStateFromMeta(metadata), 3 );
    }

    @Override
    public void onBlockPlacedBy( World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase player, ItemStack stack )
    {
        EnumFacing direction = EnumFacing.getFront((( MathHelper.floor_double( (double) ( player.rotationYaw * 4.0F / 360.0F ) + 0.5D ) & 3 ) + 2 ) % 4);
        int subType = stack.getItemDamage();
        setDirectionAndSubType( world, blockPos, direction, subType );
    }

    @Override
    public void onBlockAdded( World world, BlockPos blockPos, IBlockState blockState )
    {
        updateOutput( world, blockPos );
    }

    @Override
    public void onBlockDestroyedByPlayer( World par1World, BlockPos blockPos, IBlockState blockState )
    {
        super.onBlockDestroyedByPlayer( par1World, blockPos, blockState );
    }

    @Override
    public void randomDisplayTick( World world, BlockPos blockPos, IBlockState blockState, Random r )
    {
        if( !world.isRemote )
        {
            return;
        }
    }

    private boolean evaluateInput( World world, BlockPos blockPos )
    {
        int metadata = getMetaFromState( world.getBlockState( blockPos ) );
        EnumFacing direction = getStateFromMeta( metadata ).getValue(FACING).getOpposite();
        EnumFacing backDir = direction.getOpposite();
        return getRedstoneSignal( world, blockPos, backDir );
    }

    private boolean getRedstoneSignal( World world, BlockPos blockPos, EnumFacing dir )
    {        
        int x = blockPos.getX() + dir.getFrontOffsetX();
        int y = blockPos.getY() + dir.getFrontOffsetY();
        int z = blockPos.getZ() + dir.getFrontOffsetZ();
        EnumFacing side = dir.getOpposite();
        return QuantumUtil.getRedstoneSignal( world, new BlockPos(x, y, z), side );
    }

    private void observe( World world, BlockPos blockPos, EnumFacing dir, boolean observe )
    {
        int x = blockPos.getX() + dir.getFrontOffsetX();
        int y = blockPos.getY() + dir.getFrontOffsetY();
        int z = blockPos.getZ() + dir.getFrontOffsetZ();
        Block block = world.getBlockState(blockPos).getBlock();
        if( block != null && block instanceof IQuantumObservable )
        {
            EnumFacing side = dir.getOpposite();
            IQuantumObservable observable = (IQuantumObservable) block;
            if( observable.isObserved( world, blockPos, side ) != observe )
            {
                if( observe )
                {
                    observable.observe( world, blockPos, side );
                }
                else
                {
                    observable.reset( world, blockPos, side );
                }
            }
        }
    }
}
