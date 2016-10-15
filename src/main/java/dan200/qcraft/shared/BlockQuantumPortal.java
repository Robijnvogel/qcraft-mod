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
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockQuantumPortal extends Block {

    private static IIcon s_icon;

    public BlockQuantumPortal() {
        super(Material.portal);
        setTickRandomly(true);
        setHardness(-1.0f);
        setStepSound(Block.soundTypeGlass);
        setLightLevel(0.75f);
        setUnlocalizedName("qcraft:portal");
    }

    @Override
    public void updateTick(World par1World, BlockPos pos, IBlockState state, Random par5Random) {
        super.updateTick(par1World, pos, state, par5Random);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World par1World, BlockPos pos, IBlockState state) {
        return null;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) { //@TODO?
        float f;
        float f1;

        if (world.getBlockState(pos.east()).getBlock() != this && world.getBlockState(pos.west()).getBlock() != this) {
            f = 0.125F;
            f1 = 0.5F;
            this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f1, 0.5F + f, 1.0F, 0.5F + f1);
        } else {
            f = 0.5F;
            f1 = 0.125F;
            this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f1, 0.5F + f, 1.0F, 0.5F + f1);
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block id) {
        byte b0 = 0;
        byte b1 = 1;

        if (world.getBlockState(pos.east()).getBlock() == this || world.getBlockState(pos.west()).getBlock() == this) {
            b0 = 1;
            b1 = 0;
        }

        BlockPos temp = pos.down().up(); //:P
        while (world.getBlockState(temp.down()).getBlock() == this) {
            temp = temp.down();
        }

        if (world.getBlock(temp.down()) != Blocks.glass) {
            world.setBlockToAir(pos);
        } else {
            int h = 1;
            while (h < 4 && world.getBlockState(temp.up(h)).getBlock() == this) {
                ++h;
            }

            if (h == 3 && world.getBlockState(temp.up(h)).getBlock() == Blocks.glass) {
                boolean flag = world.getBlockState(pos.east()).getBlock() == this || world.getBlockState(pos.west()).getBlock() == this;
                boolean flag1 = world.getBlockState(pos.north()).getBlock() == this || world.getBlockState(pos.south()).getBlock() == this;

                if (flag && flag1) {
                    world.setBlockToAir(pos);
                    //about pos (line under here)
                } else if ((world.getBlock(x + b0, y, z + b1) != Blocks.glass || world.getBlock(x - b0, y, z - b1) != this) && (world.getBlock(x - b0, y, z - b1) != Blocks.glass || world.getBlock(x + b0, y, z + b1) != this)) {
                    world.setBlockToAir(pos);
                }
            } else {
                world.setBlockToAir(x, y, z);
            }
        }
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        if (par1IBlockAccess.getBlock(par2, par3, par4) == this) {
            return false;
        } else {
            boolean flag = par1IBlockAccess.getBlock(par2 - 1, par3, par4) == this && par1IBlockAccess.getBlock(par2 - 2, par3, par4) != this;
            boolean flag1 = par1IBlockAccess.getBlock(par2 + 1, par3, par4) == this && par1IBlockAccess.getBlock(par2 + 2, par3, par4) != this;
            boolean flag2 = par1IBlockAccess.getBlock(par2, par3, par4 - 1) == this && par1IBlockAccess.getBlock(par2, par3, par4 - 2) != this;
            boolean flag3 = par1IBlockAccess.getBlock(par2, par3, par4 + 1) == this && par1IBlockAccess.getBlock(par2, par3, par4 + 2) != this;
            boolean flag4 = flag || flag1;
            boolean flag5 = flag2 || flag3;
            return flag4 && par5 == 4 ? true : (flag4 && par5 == 5 ? true : (flag5 && par5 == 2 ? true : flag5 && par5 == 3));
        }
    }

    @Override
    public int quantityDropped(Random par1Random) {
        return 0;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random) {
        for (int l = 0; l < 4; ++l) {
            double d0 = (double) ((float) par2 + par5Random.nextFloat());
            double d1 = (double) ((float) par3 + par5Random.nextFloat());
            double d2 = (double) ((float) par4 + par5Random.nextFloat());
            int i1 = par5Random.nextInt(2) * 2 - 1;

            if (par1World.getBlock(par2 - 1, par3, par4) != this && par1World.getBlock(par2 + 1, par3, par4) != this) {
                d0 = (double) par2 + 0.5D + 0.25D * (double) i1;
            } else {
                d2 = (double) par4 + 0.5D + 0.25D * (double) i1;
            }

            QCraft.spawnQuantumDustFX(par1World, d0, d1, d2);
        }
    }

    @Override
    public Item getItem(World par1World, int par2, int par3, int par4) {
        return null;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        s_icon = iconRegister.registerIcon("qcraft:portal");
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int i, int j, int k, int side) {
        return s_icon;
    }

    @Override
    public IIcon getIcon(int side, int damage) {
        return s_icon;
    }
}
