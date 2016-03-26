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


package dan200.qcraft.shared.items;

import dan200.QCraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import java.util.List;

public class ItemQuantumGoggles extends ItemArmor
{
    public static int s_renderIndex;

    public static class SubTypes
    {
        public static final int QUANTUM = 0;
        public static final int ANTIOBSERVATION = 1;
        public static final int COUNT = 2;
    }

    public ItemQuantumGoggles(ArmorMaterial material, int renderIndex, int armorType)
    {
        super(material, renderIndex, armorType);
        setUnlocalizedName( "qcraft:goggles" );
        setCreativeTab( QCraft.getCreativeTab() );
        setHasSubtypes( true );
    }

    @Override
    public void getSubItems( Item itemID, CreativeTabs tabs, List list )
    {
        for( int i = 0; i < SubTypes.COUNT; ++i )
        {
            list.add( new ItemStack( QItems.quantumGoggles, 1, i ) );
        }
    }

    @Override
    public String getArmorTexture( ItemStack stack, Entity entity, int slot, String type )
    {
        switch( stack.getItemDamage() )
        {
            case SubTypes.QUANTUM:
            default:
            {
                return "qcraft:textures/armor/goggles.png";
            }
            case SubTypes.ANTIOBSERVATION:
            {
                return "qcraft:textures/armor/ao_goggles.png";
            }
        }
    }

    @Override
    public String getUnlocalizedName( ItemStack stack )
    {
        switch( stack.getItemDamage() )
        {
            case SubTypes.QUANTUM:
            default:
            {
                return "item.qcraft:goggles";
            }
            case SubTypes.ANTIOBSERVATION:
            {
                return "item.qcraft:ao_goggles";
            }
        }
    }    
}