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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockQuantumOre extends Block {

    private static IIcon s_icon;
    private final boolean m_glowing;

    public BlockQuantumOre(boolean glowing) {
        super(Material.rock);
        setHardness(3.0f);
        setResistance(5.0f);
        setUnlocalizedName("qcraft:ore");

        m_glowing = glowing;
        if (m_glowing) {
            setCreativeTab(QCraft.getCreativeTab());
            setLightLevel(0.625f);
            setTickRandomly(true);
        }
    }

    @Override
    public int tickRate(World par1World) {
        return 30;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        this.glow(world, pos);
        super.onBlockClicked(world, pos, player);
    }

    @Override
    public void onEntityCollidedWithBlock(World par1World, BlockPos pos, Entity par5Entity) {
        this.glow(par1World, pos);
        super.onEntityCollidedWithBlock(par1World, pos, par5Entity);
    }

    @Override
    public boolean onBlockActivated(World par1World, BlockPos pos, IBlockState state, EntityPlayer par5EntityPlayer, EnumFacing side, float par7, float par8, float par9) {
        this.glow(par1World, pos);
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
    public int quantityDroppedWithBonus(int par1, Random par2Random) {
        return this.quantityDropped(par2Random) + par2Random.nextInt(par1 + 1);
    }

    @Override
    public int quantityDropped(Random par1Random) {
        return 1 + par1Random.nextInt(2);
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float par6, int par7) {
        super.dropBlockAsItemWithChance(world, pos, state, par6, par7);

        if (this.getItemDropped(state, world.rand, par7) != Item.getItemFromBlock(this)) {
            int j1 = 1 + world.rand.nextInt(5);
            this.dropXpOnBlockBreak(world, pos, j1);
        }
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random par5Random) {
        if (m_glowing) {
            this.sparkle(world, pos);
        }
    }

    private void sparkle(World world, BlockPos pos) {
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

    private void glow(World world, BlockPos pos) {
        this.sparkle(world, pos);
        if (this == QCraft.Blocks.quantumOre) {
            world.setBlockState(pos, QCraft.Blocks.quantumOreGlowing.getDefaultState());
        }
    }

    @Override
    protected ItemStack createStackedBlock(IBlockState state) {
        return new ItemStack(QCraft.Blocks.quantumOre);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        s_icon = iconRegister.registerIcon("qcraft:ore");
    }

    @Override
    public IIcon getIcon(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return s_icon;
    }

    @Override
    public IIcon getIcon(EnumFacing side, int damage) {
        return s_icon;
    }
}
