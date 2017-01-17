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
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockQuantumOre extends BlockRedstoneOre {
    
    private final boolean isOn;

    public BlockQuantumOre(boolean glowing) {
        super(glowing);
        setLightLevel(0.625f);
        setHardness(3.0f);
        setResistance(5.0f);
        setUnlocalizedName("ore");
        setStepSound(soundTypePiston);
        setUnlocalizedName("oreRedstone");
        setCreativeTab(QCraft.getCreativeTab());
        
        isOn = glowing;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        this.activate(world, pos);
        super.onBlockClicked(world, pos, player);
    }

    @Override
    public void onEntityCollidedWithBlock(World par1World, BlockPos pos, Entity par5Entity) {
        this.activate(par1World, pos);
        super.onEntityCollidedWithBlock(par1World, pos, par5Entity);
    }

    @Override
    public boolean onBlockActivated(World par1World, BlockPos pos, IBlockState state, EntityPlayer par5EntityPlayer, EnumFacing side, float par7, float par8, float par9) {
        this.activate(par1World, pos);
        return super.onBlockActivated(par1World, pos, state, par5EntityPlayer, side, par7, par8, par9);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random r) {
        if (this == QCraft.Blocks.quantumOreGlowing) {
            world.setBlockState(pos, QCraft.Blocks.quantumOre.getDefaultState());
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random r, int fortune) {
        return QCraft.Items.quantumDust;
    }

    @Override
    public int quantityDropped(Random par1Random) {
        return 1 + par1Random.nextInt(2);
    }
    
    @Override
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random par5Random) {
        if (this.isOn) {
            this.spawnParticles(world, pos);
        }
    }

    private void spawnParticles(World world, BlockPos pos) {
        if (!world.isRemote) {
            return;
        }

        Random random = world.rand;
        double base = 0.0625D;

        for (int i = 0; i < 6; ++i) {
            double x = (double) random.nextFloat();
            double y = (double) random.nextFloat();
            double z = (double) random.nextFloat();

            for (EnumFacing side : EnumFacing.values()) {
                if (!world.getBlockState(pos.offset(side)).getBlock().isOpaqueCube()) {
                    if (null != side) {
                        switch (side) {
                            case DOWN:
                                y = base + 1;
                                break;
                            case UP:
                                y = -base;
                                break;
                            case NORTH:
                                z = base + 1;
                                break;
                            case SOUTH:
                                z = -base;
                                break;
                            case WEST:
                                x = base + 1;
                                break;
                            case EAST:
                                x = -base;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            x += (double) pos.getX();
            y += (double) pos.getY();
            z += (double) pos.getZ();

            if (x < (double) pos.getX() || x > (double) (pos.getX() + 1) || y < 0.0D || y > (double) (pos.getY() + 1) || z < (double) pos.getZ() || z > (double) (pos.getZ() + 1)) {
                QCraft.spawnQuantumDustFX(world, x, y, z);
            }
        }
    }

    private void activate(World world, BlockPos pos) {
        this.spawnParticles(world, pos);
        if (this == QCraft.Blocks.quantumOre) {
            world.setBlockState(pos, QCraft.Blocks.quantumOreGlowing.getDefaultState());
        }
    }

    @Override
    protected ItemStack createStackedBlock(IBlockState state) {
        return new ItemStack(QCraft.Blocks.quantumOre);
    }
}
