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


package dan200.qcraft.shared.recipes;

import dan200.qcraft.shared.blocks.BlockQBlock;
import dan200.qcraft.shared.items.ItemQBlock;
import dan200.qcraft.shared.items.ItemEOS;
import dan200.QCraft;
import dan200.qcraft.shared.items.QItems;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class RecipeQBlock implements IRecipe
{
    public RecipeQBlock()
    {
    }

    @Override
    public int getRecipeSize()
    {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return ItemQBlock.create(BlockQBlock.SubType.STANDARD, null, -1, 1 );
    }

    @Override
    public boolean matches( InventoryCrafting _inventory, World world )
    {
        return ( getCraftingResult( _inventory ) != null );
    }

    private int getImpostorType( ItemStack stack )
    {
        if( stack == null )
        {
            return 0;
        }

        ItemStack[] blocks = BlockQBlock.getImpostorBlockList();
        for( int i = 1; i < blocks.length; ++i )
        {
            ItemStack block = blocks[ i ];
            if( block.getItem() == stack.getItem() && block.getItemDamage() == stack.getItemDamage() )
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ItemStack getCraftingResult( InventoryCrafting inventory )
    {
        // Find the stone
        int stonePosX = -1;
        int stonePosY = -1;
        int stoneType = -1;
        for( int y = 0; y < 3; ++y )
        {
            for( int x = 0; x < 3; ++x )
            {
                ItemStack item = inventory.getStackInRowAndColumn( x, y );
                if( item != null &&
                        item.getItem() == QItems.eos &&
                        ( item.getItemDamage() == ItemEOS.SubType.OBSERVATION || item.getItemDamage() == ItemEOS.SubType.SUPERPOSITION ) )
                {
                    stonePosX = x;
                    stonePosY = y;
                    stoneType = item.getItemDamage();
                    break;
                }
            }
        }

        // Fail if no stone found:
        if( stonePosX < 0 || stonePosY < 0 )
        {
            return null;
        }

        // Find the types of the things around the stone
        int numTypes = 0;
        Map<EnumFacing, Integer> types = new EnumMap<EnumFacing, Integer>(EnumFacing.class);
        for( int y = 0; y < 3; ++y )
        {
            for( int x = 0; x < 3; ++x )
            {
                if( !( x == stonePosX && y == stonePosY ) )
                {
                    ItemStack item = inventory.getStackInRowAndColumn( x, y );
                    int type = getImpostorType( item );

                    if( type < 0 )
                    {
                        return null;
                    }

                    int lx = x - stonePosX;
                    int ly = y - stonePosY;
                    if( lx == 0 && ly == -1 )
                    {
                        // North
                        types.put(EnumFacing.NORTH, type);
                    }
                    else if( lx == 0 && ly == 1 )
                    {
                        // South
                        types.put(EnumFacing.SOUTH, type);
                    }
                    else if( lx == -1 && ly == 0 )
                    {
                        // West
                        types.put(EnumFacing.WEST, type);
                    }
                    else if( lx == 1 && ly == 0 )
                    {
                        // East
                        types.put(EnumFacing.EAST, type);
                    }
                    else if( lx == -1 && ly == 1 )
                    {
                        // Up
                        types.put(EnumFacing.UP, type);
                    }
                    else if( lx == -1 && ly == -1 )
                    {
                        // Down
                        types.put(EnumFacing.DOWN, type);
                    }
                    else if( type != 0 )
                    {
                        return null;
                    }

                    if( type > 0 )
                    {
                        numTypes++;
                    }
                }
            }
        }

        if( numTypes > 0 )
        {
            // Create the item
            if( stoneType == ItemEOS.SubType.OBSERVATION )
            {
                return ItemQBlock.create(BlockQBlock.SubType.STANDARD, types, -1, 1 );
            }
            else if( stoneType == ItemEOS.SubType.SUPERPOSITION )
            {
                return ItemQBlock.create(BlockQBlock.SubType.FIFTYFIFTY, types, -1, 1 );
            }
        }
        return null;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        return null;
    }
}
