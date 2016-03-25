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
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BlockQuantumPortal extends Block
{

    public BlockQuantumPortal()
    {
        super( Material.portal );
        setTickRandomly( true );
        setHardness( -1.0f );
        setStepSound( Block.soundTypeGlass );
        setLightLevel( 0.75f );
        setRegistryName( "qcraft:portal" );
    }

    @Override
    public void updateTick( World par1World, BlockPos blockPos, IBlockState blockState, Random par5Random )
    {
        super.updateTick(par1World, blockPos, blockState, par5Random);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox( World par1World, BlockPos blockPos, IBlockState blockState )
    {
        return null;
    }

    @Override
    public void setBlockBoundsBasedOnState( IBlockAccess world, BlockPos blockPos )
    {
        float f;
        float f1;
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();        

        if( world.getBlockState( new BlockPos(x - 1, y, z) ).getBlock() != this && world.getBlockState( new BlockPos(x + 1, y, z) ).getBlock() != this )
        {
            f = 0.125F;
            f1 = 0.5F;
            this.setBlockBounds( 0.5F - f, 0.0F, 0.5F - f1, 0.5F + f, 1.0F, 0.5F + f1 );
        }
        else
        {
            f = 0.5F;
            f1 = 0.125F;
            this.setBlockBounds( 0.5F - f, 0.0F, 0.5F - f1, 0.5F + f, 1.0F, 0.5F + f1 );
        }
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isBlockNormalCube()
    {
        return false;
    }

    @Override
    public void onNeighborBlockChange( World world, BlockPos blockPos, IBlockState blockState, Block id )
    {
        byte b0 = 0;
        byte b1 = 1;
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        if( world.getBlockState( new BlockPos(x - 1, y, z) ).getBlock() == this || world.getBlockState( new BlockPos(x + 1, y, z) ).getBlock() == this )
        {
            b0 = 1;
            b1 = 0;
        }

        int yOrigin = y;
        while( world.getBlockState( new BlockPos(x, yOrigin - 1, z )).getBlock() == this )
        {
            --yOrigin;
        }

        if( world.getBlockState( new BlockPos(x, yOrigin - 1, z )).getBlock() != Blocks.glass )
        {
            world.setBlockToAir( blockPos );
        }
        else
        {
            int h = 1;
            while( h < 4 && world.getBlockState( new BlockPos(x, yOrigin + h, z) ).getBlock() == this )
            {
                ++h;
            }

            if( h == 3 && world.getBlockState( new BlockPos( x, yOrigin + h, z ) ).getBlock() == Blocks.glass )
            {
                boolean flag = world.getBlockState( new BlockPos( x - 1, y, z ) ).getBlock() == this || world.getBlockState( new BlockPos( x + 1, y, z ) ).getBlock() == this;
                boolean flag1 = world.getBlockState( new BlockPos( x, y, z - 1 ) ).getBlock() == this || world.getBlockState( new BlockPos( x, y, z + 1 ) ).getBlock() == this;

                if( flag && flag1 )
                {
                    world.setBlockToAir( blockPos );
                }
                else
                {
                    if( ( world.getBlockState( new BlockPos( x + b0, y, z + b1 ) ).getBlock() != Blocks.glass || world.getBlockState( new BlockPos( x - b0, y, z - b1 ) ).getBlock() != this ) && ( world.getBlockState( new BlockPos( x - b0, y, z - b1 ) ).getBlock() != Blocks.glass || world.getBlockState( new BlockPos( x + b0, y, z + b1 ) ).getBlock() != this ) )
                    {
                        world.setBlockToAir( blockPos );
                    }
                }
            }
            else
            {
                world.setBlockToAir( blockPos );
            }
        }
    }

    @Override
    public boolean shouldSideBeRendered( IBlockAccess par1IBlockAccess, BlockPos blockPos, EnumFacing side )
    {
        if( par1IBlockAccess.getBlockState( blockPos ).getBlock() == this )
        {
            return false;
        }
        else
        {
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();
        
            boolean flag = par1IBlockAccess.getBlockState( new BlockPos( x - 1, y, z ) ).getBlock() == this && par1IBlockAccess.getBlockState( new BlockPos( x - 2, y, z ) ).getBlock() != this;
            boolean flag1 = par1IBlockAccess.getBlockState( new BlockPos( x + 1, y, z ) ).getBlock() == this && par1IBlockAccess.getBlockState( new BlockPos( x + 2, y, z ) ).getBlock() != this;
            boolean flag2 = par1IBlockAccess.getBlockState( new BlockPos( x, y, z - 1 ) ).getBlock() == this && par1IBlockAccess.getBlockState( new BlockPos( x, y, z - 2 ) ).getBlock() != this;
            boolean flag3 = par1IBlockAccess.getBlockState( new BlockPos( x, y, z + 1 ) ).getBlock() == this && par1IBlockAccess.getBlockState( new BlockPos( x, y, z + 2 ) ).getBlock() != this;
            boolean flag4 = flag || flag1;
            boolean flag5 = flag2 || flag3;
            return flag4 && side == EnumFacing.WEST ? true : ( flag4 && side == EnumFacing.EAST ? true : ( flag5 && side == EnumFacing.NORTH ? true : flag5 && side == EnumFacing.SOUTH ) );
        }
    }

    @Override
    public int quantityDropped( Random par1Random )
    {
        return 0;
    }

    @Override
    public void onEntityCollidedWithBlock( World world, BlockPos blockPos, Entity entity )
    {
    }

    @Override
    public void randomDisplayTick( World par1World, BlockPos blockPos, IBlockState blockState, Random par5Random )
    {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        for( int l = 0; l < 4; ++l )
        {
            double d0 = (double) ( (float) x + par5Random.nextFloat() );
            double d1 = (double) ( (float) y + par5Random.nextFloat() );
            double d2 = (double) ( (float) z + par5Random.nextFloat() );
            int i1 = par5Random.nextInt( 2 ) * 2 - 1;

            if( par1World.getBlockState( new BlockPos( x - 1, y, z ) ).getBlock() != this && par1World.getBlockState( new BlockPos( x + 1, y, z ) ).getBlock() != this )
            {
                d0 = (double) x + 0.5D + 0.25D * (double) i1;
            }
            else
            {
                d2 = (double) z + 0.5D + 0.25D * (double) i1;
            }

            QCraft.spawnQuantumDustFX( par1World, blockPos);
        }
    }

    @Override
    public Item getItem( World par1World, BlockPos blockPos )
    {
        return null;
    }
}

