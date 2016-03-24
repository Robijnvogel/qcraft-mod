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

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class QuantumUtil
{
    private static Block getBlock( IBlockAccess world, BlockPos blockPos )
    {
        return world.getBlockState(blockPos).getBlock();
    }

    public static boolean getRedstoneSignal( World world, BlockPos blockPos, EnumFacing side )
    {
        Block block = getBlock( world, blockPos );
        if( block != null )
        {
            if( block == Blocks.redstone_wire )
            {
                int metadata = block.getDamageValue(world, blockPos);
                return ( side != EnumFacing.UP && metadata > 0 ) ? true : false;
            }
            else if( block.canProvidePower() )
            {
                EnumFacing testSide = side.getOpposite();
                int power = block.getWeakPower(world, blockPos, world.getBlockState(blockPos), testSide);
                return ( power > 0 ) ? true : false;
            }
            else if( block.isNormalCube() )
            {
                for( EnumFacing facing : EnumFacing.values())
                {
                    if( facing != side )
                    {
                        BlockPos tempBlockPos = blockPos.offset(facing);
                        Block neighbour = getBlock( world, tempBlockPos );
                        if( neighbour != null && neighbour.canProvidePower() )
                        {
                            int power = neighbour.getStrongPower(world, tempBlockPos, neighbour.getDefaultState(), facing);
                            if( power > 0 )
                            {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }
        return false;
    }
}
