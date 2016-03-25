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
import dan200.qcraft.shared.CreativeTabQuantumCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 *
 * @author Robijnvogel 2016-03-25
 */
public final class QItems {

    public static ItemQuantumDust quantumDust;
    public static ItemEOS eos;
    public static ItemQuantumGoggles quantumGoggles;
    public static ArmorMaterial QGOGGLES = EnumHelper.addArmorMaterial( "QGOGGLES", "qcraft:goggles", 0, new int[]{ 0, 0, 0, 0 }, 0 );

    public static void createItems() {
        
        // Quantum Dust item
        quantumDust = new ItemQuantumDust();
        GameRegistry.registerItem(quantumDust, "dust");

        // EOS item
        eos = new ItemEOS();
        GameRegistry.registerItem(eos, "essence");

        // Quantum Goggles item
        quantumGoggles = new ItemQuantumGoggles(QGOGGLES, 0, 0);
        GameRegistry.registerItem(quantumGoggles, "goggles");
        
    }
}
