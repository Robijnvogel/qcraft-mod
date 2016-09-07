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
import net.minecraft.block.BlockRedstoneOre;
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

public class BlockQuantumOre extends BlockRedstoneOre
{
    private boolean m_glowing;

    public BlockQuantumOre( boolean glowing )
    {
        super( glowing );
        setHardness( 3.0f );
        setResistance( 5.0f );
        setRegistryName( "qcraft:ore" );
        setCreativeTab( QCraft.getCreativeTab() );
    }

    @Override
    public void updateTick( World world, BlockPos blockPos, IBlockState blockState, Random r )
    {
        if( this == QBlocks.quantumOreGlowing )
        {
            world.setBlockState(blockPos, QBlocks.quantumOre.getDefaultState());
        }
    }

    @Override
    public Item getItemDropped( IBlockState blockState, Random r, int j )
    {
        return QItems.quantumDust;
    }
    
    @Override
    public int quantityDropped( Random random )
    {
        return 1 + random.nextInt( 2 );
    }

    private void spawnParticles(World worldIn, BlockPos pos)
    {
        if( !worldIn.isRemote )
        {
            return;
        }
        
        Random random = worldIn.rand;
        double d0 = 0.0625D;

        for (int i = 0; i < 6; ++i)
        {
            double d1 = (double)((float)pos.getX() + random.nextFloat());
            double d2 = (double)((float)pos.getY() + random.nextFloat());
            double d3 = (double)((float)pos.getZ() + random.nextFloat());

            if (i == 0 && !worldIn.getBlockState(pos.up()).getBlock().isOpaqueCube())
            {
                d2 = (double)pos.getY() + d0 + 1.0D;
            }

            if (i == 1 && !worldIn.getBlockState(pos.down()).getBlock().isOpaqueCube())
            {
                d2 = (double)pos.getY() - d0;
            }

            if (i == 2 && !worldIn.getBlockState(pos.south()).getBlock().isOpaqueCube())
            {
                d3 = (double)pos.getZ() + d0 + 1.0D;
            }

            if (i == 3 && !worldIn.getBlockState(pos.north()).getBlock().isOpaqueCube())
            {
                d3 = (double)pos.getZ() - d0;
            }

            if (i == 4 && !worldIn.getBlockState(pos.east()).getBlock().isOpaqueCube())
            {
                d1 = (double)pos.getX() + d0 + 1.0D;
            }

            if (i == 5 && !worldIn.getBlockState(pos.west()).getBlock().isOpaqueCube())
            {
                d1 = (double)pos.getX() - d0;
            }

            if (d1 < (double)pos.getX() || d1 > (double)(pos.getX() + 1) || d2 < 0.0D || d2 > (double)(pos.getY() + 1) || d3 < (double)pos.getZ() || d3 > (double)(pos.getZ() + 1))
            {
                QCraft.spawnQuantumDustFX( worldIn, new BlockPos(d1, d2, d3) );
            }
        }
    }

    @Override
    protected ItemStack createStackedBlock( IBlockState blockState )
    {
        return new ItemStack( QBlocks.quantumOre );
    }    
}
