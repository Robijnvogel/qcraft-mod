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

public class ItemQuantumDust extends Item
{

    public ItemQuantumDust()
    {
        super();
        setMaxStackSize( 64 );
        setHasSubtypes( false );
        setUnlocalizedName( "qcraft:dust" );
        setCreativeTab( QCraft.getCreativeTab() );
    }

    @Override
    public void getSubItems( Item item, CreativeTabs tabs, List list )
    {
        list.add( new ItemStack( QItems.quantumDust, 1, 0 ) );
    }
}
