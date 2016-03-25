/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dan200.qcraft.shared.blocks;

import dan200.QCraft;
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
