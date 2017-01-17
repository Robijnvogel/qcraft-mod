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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockQuantumComputer extends BlockDirectional
        implements ITileEntityProvider {

    public BlockQuantumComputer() {
        super(Material.iron);
        setCreativeTab(QCraft.getCreativeTab());
        setHardness(5.0f);
        setResistance(10.0f);
        setStepSound(Block.soundTypeMetal);
        setUnlocalizedName("qcraft:computer");
    }

    /*
    @Override
    public boolean isOpaqueCube() {
        return false;
    }
     */
    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        // RemoveBlockByPlayer handles this instead ?¿?
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> blocks = new ArrayList();
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQuantumComputer) {
            // Get the computer back
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            ItemStack stack = ItemQuantumComputer.create(computer.getEntanglementFrequency(), 1);
            ItemQuantumComputer.setStoredData(stack, computer.getStoredData());
            blocks.add(stack);
        }
        return blocks;
    }

    protected boolean shouldDropItemsInCreative(World world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (world.isRemote) {
            return false;
        }

        if (!player.capabilities.isCreativeMode || shouldDropItemsInCreative(world, pos)) {
            // Regular and silk touch block (identical)
            List<ItemStack> items = getDrops(world, pos, world.getBlockState(pos), 0);
            Iterator<ItemStack> it = items.iterator();
            while (it.hasNext()) {
                ItemStack item = it.next();
                if (!world.isRemote && !world.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
                {
                    if (world.rand.nextFloat() <= 1) {
                        spawnAsEntity(world, pos, item);
                    }
                }
            }
        }

        return super.removedByPlayer(world, pos, player, willHarvest);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) {
        List<ItemStack> items = getDrops(world, pos, world.getBlockState(pos), 0);
        if (items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float m, float n, float o) {
        if (player.isSneaking()) {
            return false;
        }

        if (!world.isRemote) {
            // Show GUI
            TileEntity entity = world.getTileEntity(pos);
            if (entity != null && entity instanceof TileEntityQuantumComputer) {
                TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
                QCraft.openQuantumComputerGUI(player, computer);
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQuantumComputer) {
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            computer.onDestroy();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        int direction = ((MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 0x3) + 2) % 4;
        int metadata = (direction & 0x3);
        world.setBlockState(pos, this.getStateFromMeta(metadata), 3); //sets the front of the machine facing the player
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(world, pos, state, neighborBlock);

        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity instanceof TileEntityQuantumComputer) {
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            computer.setRedstonePowered(world.isBlockIndirectlyGettingPowered(pos) > 0);
        }
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityQuantumComputer();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createNewTileEntity(world, 0); //0 -> metadata gets discarded anyway, so why bother?
    }
}
