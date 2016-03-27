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
package dan200.qcraft.shared.blocks;

import dan200.qcraft.shared.items.*;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 *
 * @author Robijnvogel 2016-03-25
 */
public class QBlocks {

    public static BlockQuantumOre quantumOre;
    public static BlockQuantumOre quantumOreGlowing;
    public static BlockQuantumLogic quantumLogic;
    public static BlockQBlock qBlock;
    public static BlockQuantumComputer quantumComputer;
    public static BlockQuantumPortal quantumPortal;

    public static void createBlocks() {

        // Quantum ore blocks
        quantumOre = new BlockQuantumOre(false);
        GameRegistry.registerBlock(quantumOre, "quantumore");

        quantumOreGlowing = new BlockQuantumOre(true);
        GameRegistry.registerBlock(quantumOreGlowing, "quantumoreglowing");

        // Quantum logic block
        quantumLogic = new BlockQuantumLogic();
        GameRegistry.registerBlock(quantumLogic, ItemQuantumLogic.class, "quantumlogic");

        // qBlock block
        qBlock = new BlockQBlock();
        GameRegistry.registerBlock(qBlock, ItemQBlock.class, "qblock");

        // Quantum Computer block
        quantumComputer = new BlockQuantumComputer();
        GameRegistry.registerBlock(quantumComputer, ItemQuantumComputer.class, "quantumcomputer");

        // Quantum Portal block
        quantumPortal = new BlockQuantumPortal();
        GameRegistry.registerBlock(quantumPortal, "quantumportal");
    }
}
