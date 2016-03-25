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
import dan200.qcraft.shared.items.QItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import java.util.Random;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BlockQuantumOre extends Block
{
    private boolean m_glowing;

    public BlockQuantumOre( boolean glowing )
    {
        super( Material.rock );
        setHardness( 3.0f );
        setResistance( 5.0f );
        setRegistryName( "qcraft:ore" );

        m_glowing = glowing;
        if( m_glowing )
        {
            setCreativeTab( QCraft.getCreativeTab() );
            setLightLevel( 0.625f );
            setTickRandomly( true );
        }
    }

    @Override
    public int tickRate( World par1World )
    {
        return 30;
    }

    @Override
    public void onBlockClicked( World par1World, BlockPos blockPos, EntityPlayer par5EntityPlayer )
    {
        this.glow( par1World, blockPos );
        super.onBlockClicked( par1World, blockPos, par5EntityPlayer );
    }

    @Override
    public void onEntityCollidedWithBlock( World par1World, BlockPos blockPos, Entity par5Entity )
    {
        this.glow( par1World, blockPos );
        super.onEntityCollidedWithBlock( par1World, blockPos, par5Entity );
    }

    @Override
    public boolean onBlockActivated( World par1World, BlockPos blockPos, IBlockState blockState, EntityPlayer par5EntityPlayer, EnumFacing side, float hitX, float hitY, float hitZ )
    {
        this.glow( par1World, blockPos );
        return super.onBlockActivated( par1World, blockPos, blockState, par5EntityPlayer, side, hitX, hitY, hitZ );
    }

    @Override
    public void updateTick( World world, BlockPos blockPos, IBlockState blockState, Random r )
    {
        if( this == QBlocks.quantumOreGlowing )
        {
            world.setBlockState(blockPos, (IBlockState) new BlockState(QBlocks.quantumOre, (IProperty[]) world.getBlockState(blockPos).getProperties().keySet().toArray()));
        }
    }

    @Override
    public Item getItemDropped( IBlockState blockState, Random r, int j )
    {
        return QItems.quantumDust;
    }

    @Override
    public int quantityDroppedWithBonus( int par1, Random par2Random )
    {
        return this.quantityDropped( par2Random ) + par2Random.nextInt( par1 + 1 );
    }

    @Override
    public int quantityDropped( Random par1Random )
    {
        return 1 + par1Random.nextInt( 2 );
    }

    @Override
    public void dropBlockAsItemWithChance( World par1World, BlockPos blockPos, IBlockState blockState, float chance, int fortune )
    {
        super.dropBlockAsItemWithChance( par1World, blockPos, blockState, chance, fortune );

        if( this.getItemDropped( blockState, par1World.rand, fortune ) != Item.getItemFromBlock( this ) )
        {
            int j1 = 1 + par1World.rand.nextInt( 5 );
            this.dropXpOnBlockBreak( par1World, blockPos, j1 );
        }
    }

    @Override
    public void randomDisplayTick( World par1World, BlockPos blockPos, IBlockState blockState, Random par5Random )
    {
        if( m_glowing )
        {
            this.sparkle( par1World, blockPos );
        }
    }

    private void sparkle( World par1World, BlockPos blockPos )
    {
        if( !par1World.isRemote )
        {
            return;
        }

        Random random = par1World.rand;
        double d0 = 0.0625D;
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        for( int l = 0; l < 6; ++l )
        {
            double d1 = (double) ( (float) x + random.nextFloat() );
            double d2 = (double) ( (float) y + random.nextFloat() );
            double d3 = (double) ( (float) z + random.nextFloat() );

            if( l == 0 && !par1World.getBlockState( new BlockPos( x, y + 1, z ) ).getBlock().isOpaqueCube() )
            {
                d2 = (double) ( y + 1 ) + d0;
            }

            if( l == 1 && !par1World.getBlockState( new BlockPos( x, y - 1, z ) ).getBlock().isOpaqueCube() )
            {
                d2 = (double) ( y + 0 ) - d0;
            }

            if( l == 2 && !par1World.getBlockState( new BlockPos( x, y, z + 1 ) ).getBlock().isOpaqueCube() )
            {
                d3 = (double) ( z + 1 ) + d0;
            }

            if( l == 3 && !par1World.getBlockState( new BlockPos( x, y, z - 1 ) ).getBlock().isOpaqueCube() )
            {
                d3 = (double) ( z + 0 ) - d0;
            }

            if( l == 4 && !par1World.getBlockState( new BlockPos( x + 1, y, z ) ).getBlock().isOpaqueCube() )
            {
                d1 = (double) ( x + 1 ) + d0;
            }

            if( l == 5 && !par1World.getBlockState( new BlockPos( x - 1, y, z ) ).getBlock().isOpaqueCube() )
            {
                d1 = (double) ( x + 0 ) - d0;
            }

            if( d1 < (double) x || d1 > (double) ( x + 1 ) || d2 < 0.0D || d2 > (double) ( y + 1 ) || d3 < (double) z || d3 > (double) ( z + 1 ) )
            {
                QCraft.spawnQuantumDustFX( par1World, new BlockPos(d1, d2, d3) );
            }
        }
    }

    private void glow( World world, BlockPos blockPos )
    {
        this.sparkle( world, blockPos );
        if( this == QBlocks.quantumOre )
        {
            world.setBlockState(blockPos, (IBlockState) new BlockState(QBlocks.quantumOreGlowing, (IProperty[]) world.getBlockState(blockPos).getProperties().keySet().toArray()));
        }
    }

    @Override
    protected ItemStack createStackedBlock( IBlockState blockState )
    {
        return new ItemStack( QBlocks.quantumOre );
    }    
}
