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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.List;

public class ItemEOS extends Item
{

    public static class SubType
    {
        public static final int SUPERPOSITION = 0;
        public static final int OBSERVATION = 1;
        public static final int ENTANGLEMENT = 2;
        public static final int COUNT = 3;
    }

    public ItemEOS()
    {
        super();
        setMaxStackSize( 64 );
        setHasSubtypes( true );
        setUnlocalizedName( "qcraft:eos" );
        setCreativeTab( QCraft.getCreativeTab() );
    }

    @Override
    public void getSubItems( Item itemID, CreativeTabs tabs, List list )
    {
        for( int i = 0; i < SubType.COUNT; ++i )
        {
            list.add( new ItemStack( itemID, 1, i ) );
        }
    }

    @Override
    public String getUnlocalizedName( ItemStack stack )
    {
        int damage = stack.getItemDamage();
        switch( damage )
        {
            case SubType.SUPERPOSITION:
            default:
            {
                return "item.qcraft:eos";
            }
            case SubType.OBSERVATION:
            {
                return "item.qcraft:eoo";
            }
            case SubType.ENTANGLEMENT:
            {
                return "item.qcraft:eoe";
            }
        }
    }
}
