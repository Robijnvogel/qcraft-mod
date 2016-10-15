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

public class QuantumUtil {

    private static Block getBlock(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getBlock();
    }

    public static boolean getRedstoneSignal(World world, BlockPos pos, EnumFacing side) {
        Block block = getBlock(world, pos);
        if (block != null) {
            if (block == Blocks.redstone_wire) {
                int metadata = world.getBlockMetadata(pos);
                return (side != EnumFacing.UP && metadata > 0);
            } else if (block.canProvidePower()) {
                EnumFacing testSide = side.getOpposite();
                int power = block.getWeakPower(world, pos, world.getBlockState(pos), testSide);
                return (power > 0);
            } else if (world.isBlockNormalCubeDefault(pos, false)) {
                for (EnumFacing i : EnumFacing.values()) {
                    if (i != side) {
                        BlockPos test = pos.offset(i);
                        Block neighbour = getBlock(world, test);
                        if (neighbour != null && neighbour.canProvidePower()) {
                            int power = neighbour.getStrongPower(world, test, world.getBlockState(pos), i);
                            if (power > 0) {
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
