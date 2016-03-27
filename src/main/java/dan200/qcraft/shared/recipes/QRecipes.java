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

import dan200.QCraft;
import dan200.qcraft.shared.blocks.BlockQuantumLogic;
import dan200.qcraft.shared.blocks.QBlocks;
import dan200.qcraft.shared.items.ItemEOS;
import dan200.qcraft.shared.items.ItemQuantumComputer;
import dan200.qcraft.shared.items.ItemQuantumGoggles;
import dan200.qcraft.shared.items.QItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;

/**
 *
 * @author Robijnvogel 2016-03-25
 */
public class QRecipes {

    public static void createRecipes() {
        // Automated Observer recipe
        ItemStack observer = new ItemStack(QBlocks.quantumLogic, 1, BlockQuantumLogic.SubType.ObserverOff);
        GameRegistry.addRecipe(observer, new Object[]{
            "XXX", "XYX", "XZX",
            'X', Blocks.stone,
            'Y', QItems.eos,
            'Z', Items.redstone
        });

        // EOS recipe
        ItemStack eos = new ItemStack(QItems.eos, 1, ItemEOS.SubType.SUPERPOSITION);
        GameRegistry.addRecipe(eos, new Object[]{
            "XX", "XX", 'X', QItems.quantumDust,});

        // EOO recipe
        ItemStack eoo = new ItemStack(QItems.eos, 1, ItemEOS.SubType.OBSERVATION);
        GameRegistry.addRecipe(eoo, new Object[]{
            " X ", "X X", " X ",
            'X', QItems.quantumDust,});

        // EOE recipe
        ItemStack eoe = new ItemStack(QItems.eos, 1, ItemEOS.SubType.ENTANGLEMENT);
        GameRegistry.addRecipe(eoe, new Object[]{
            "X X", " Y ", "X X",
            'X', QItems.quantumDust,
            'Y', eos,});

        // qBlock recipes
        GameRegistry.addRecipe(new RecipeQBlock());
        RecipeSorter.register("qCraft:qBlock", RecipeQBlock.class, SHAPED, "after:minecraft:shapeless");

        GameRegistry.addRecipe(new RecipeEntangledQBlock());
        RecipeSorter.register("qCraft:entangled_qBlock", RecipeEntangledQBlock.class, SHAPED, "after:minecraft:shapeless");

        // Quantum Computer recipe
        ItemStack regularQuantumComputer = ItemQuantumComputer.create(-1, 1);
        GameRegistry.addRecipe(regularQuantumComputer, new Object[]{
            "XXX", "XYX", "XZX", 
            'X', Items.iron_ingot, 
            'Y', QItems.quantumDust, 
            'Z', Blocks.glass_pane,});

        // Entangled Quantum Computer
        GameRegistry.addRecipe(new RecipeEntangledQuantumComputer());
        RecipeSorter.register("qCraft:entangled_computer", RecipeEntangledQuantumComputer.class, SHAPED, "after:minecraft:shapeless");

        // Quantum Goggles recipe
        ItemStack quantumGoggles = new ItemStack(QItems.quantumGoggles, 1, ItemQuantumGoggles.SubTypes.QUANTUM);
        GameRegistry.addRecipe(quantumGoggles, new Object[]{
            "XYX", 
            'X', Blocks.glass_pane,
            'Y', QItems.quantumDust,});

        // Anti-observation goggles recipe
        ItemStack aoGoggles = new ItemStack(QItems.quantumGoggles, 1, ItemQuantumGoggles.SubTypes.ANTIOBSERVATION);
        GameRegistry.addRecipe(aoGoggles, new Object[]{
            "XYX", 
            'X', Blocks.glass_pane, 
            'Y', new ItemStack(QItems.eos, 1, ItemEOS.SubType.OBSERVATION),});

        if (QCraft.enableWorldGenReplacementRecipes) {
            // Quantum dust recipe
            GameRegistry.addRecipe(new ItemStack(QItems.quantumDust, 2), new Object[]{
                "XY", 
                'X', Items.redstone, 
                'Y', new ItemStack(Items.dye, 1, 10) // Lime green
            });
        }
    }

}
