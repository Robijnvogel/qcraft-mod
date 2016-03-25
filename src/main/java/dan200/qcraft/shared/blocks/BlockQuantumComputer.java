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

import dan200.qcraft.shared.items.ItemQuantumComputer;
import dan200.QCraft;
import dan200.qcraft.shared.TileEntityQuantumComputer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BlockQuantumComputer extends BlockDirectional
        implements ITileEntityProvider
{

    public BlockQuantumComputer()
    {
        super( Material.iron );
        setCreativeTab( QCraft.getCreativeTab() );
        setHardness( 5.0f );
        setResistance( 10.0f );
        setStepSound( Block.soundTypeMetal );
        setRegistryName( "qcraft:computer" );
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public Item getItemDropped( IBlockState blockState, Random random, int j )
    {
        return Item.getItemFromBlock( this );
    }

    @Override
    public int damageDropped( IBlockState blockState )
    {
        return 0;
    }

    @Override
    public void dropBlockAsItemWithChance( World world, BlockPos blockPos, IBlockState blockState, float chance, int fortune )
    {
        // RemoveBlockByPlayer handles this instead
    }

    @Override
    public ArrayList<ItemStack> getDrops( IBlockAccess world, BlockPos blockPos, IBlockState blockState, int fortune )
    {
        ArrayList<ItemStack> blocks = new ArrayList<ItemStack>();
        TileEntity entity = world.getTileEntity( blockPos );
        if( entity != null && entity instanceof TileEntityQuantumComputer )
        {
            // Get the computer back
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            ItemStack stack = ItemQuantumComputer.create( computer.getEntanglementFrequency(), 1 );
            ItemQuantumComputer.setStoredData( stack, computer.getStoredData() );
            blocks.add( stack );
        }
        return blocks;
    }

    protected boolean shouldDropItemsInCreative( World world, BlockPos blockPos )
    {
        return false;
    }

    @Override
    public boolean removedByPlayer( World world, BlockPos blockPos, EntityPlayer player, boolean willHarvest )
    {
        if( world.isRemote )
        {
            return false;
        }

        if( !player.capabilities.isCreativeMode || shouldDropItemsInCreative( world, blockPos ) )
        {
            // Regular and silk touch block (identical)
            int metadata = getMetaFromState(world.getBlockState(blockPos));
            ArrayList<ItemStack> items = getDrops( world, blockPos, world.getBlockState(blockPos), 0 );
            Iterator<ItemStack> it = items.iterator();
            while( it.hasNext() )
            {
                ItemStack item = it.next();
                dropBlockAsItem( world, blockPos, world.getBlockState(blockPos), 0 );
            }
        }

        return super.removedByPlayer( world, blockPos, player, willHarvest );
    }

    @Override
    public ItemStack getPickBlock( MovingObjectPosition target, World world, BlockPos blockPos )
    {
        int metadata = getMetaFromState(world.getBlockState(blockPos));
        ArrayList<ItemStack> items = getDrops( world, blockPos, world.getBlockState(blockPos), 0 );
        if( items.size() > 0 )
        {
            return items.get( 0 );
        }
        return null;
    }

    @Override
    public boolean onBlockActivated( World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ )
    {
        if( player.isSneaking() )
        {
            return false;
        }

        if( !world.isRemote )
        {
            // Show GUI
            TileEntity entity = world.getTileEntity( blockPos );
            if( entity != null && entity instanceof TileEntityQuantumComputer )
            {
                TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
                QCraft.openQuantumComputerGUI( player, computer );
            }
        }
        return true;
    }

    @Override
    public void breakBlock( World world, BlockPos blockPos, IBlockState blockState )
    {
        TileEntity entity = world.getTileEntity( blockPos );
        if( entity != null && entity instanceof TileEntityQuantumComputer )
        {
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            computer.onDestroy();
        }
        super.breakBlock( world, blockPos, blockState );
    }

    @Override
    public void onBlockPlacedBy( World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase player, ItemStack stack )
    {
        int direction = ( ( MathHelper.floor_double( (double) ( player.rotationYaw * 4.0F / 360.0F ) + 0.5D ) & 0x3 ) + 2 ) % 4;
        int metadata = ( direction & 0x3 );
        world.setBlockState(blockPos, blockState, 3); //.setBlockMetadataWithNotify( blockPos, metadata, 3 );
    }

    @Override
    public void onNeighborBlockChange( World world, BlockPos blockPos, IBlockState blockState, Block id )
    {
        super.onNeighborBlockChange( world, blockPos, blockState, id );

        TileEntity entity = world.getTileEntity( blockPos );
        if( entity != null && entity instanceof TileEntityQuantumComputer )
        {
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            computer.setRedstonePowered( EnumFacing.getFront(world.isBlockIndirectlyGettingPowered( blockPos ) ) );
        }
    }

    @Override
    public boolean canConnectRedstone( IBlockAccess world, BlockPos blockPos, EnumFacing side )
    {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity( World world, int metadata )
    {
        return new TileEntityQuantumComputer();
    }

    @Override
    public TileEntity createTileEntity( World world, IBlockState blockState )
    {
        return createNewTileEntity( world, getMetaFromState(blockState));
    }
}
