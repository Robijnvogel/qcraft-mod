/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dan200.qcraft.client.blocks;

import dan200.qcraft.shared.blocks.BlockQBlock;
import dan200.qcraft.shared.blocks.BlockQBlock.Appearance;
import dan200.qcraft.shared.blocks.BlockQuantumLogic;
import dan200.qcraft.shared.blocks.QBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;

/**
 *
 * @author Mathijs Riezebos <0766426>
 */
public class QBlockRenderRegister {
    
    public static void registerBlockRenderer() {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        
        mesher.register(Item.getItemFromBlock(QBlocks.qBlock), 0, new ModelResourceLocation("qcraft:transparent", "inventory"));
        mesher.register(Item.getItemFromBlock(QBlocks.qBlock), 1, new ModelResourceLocation("qcraft:qblock_fuzz", "inventory"));
        mesher.register(Item.getItemFromBlock(QBlocks.qBlock), 2, new ModelResourceLocation("qcraft:qblock_swirl", "inventory"));
        
        mesher.register(Item.getItemFromBlock(QBlocks.quantumComputer), 0, new ModelResourceLocation("qcraft:computer", "inventory"));
        mesher.register(Item.getItemFromBlock(QBlocks.quantumComputer), 1, new ModelResourceLocation("qcraft:computer_top", "inventory"));
        mesher.register(Item.getItemFromBlock(QBlocks.quantumComputer), 2, new ModelResourceLocation("qcraft:computer_side", "inventory"));
        
        mesher.register(Item.getItemFromBlock(QBlocks.quantumLogic), BlockQuantumLogic.SubType.Count, new ModelResourceLocation("", "inventory"));
        mesher.register(Item.getItemFromBlock(QBlocks.quantumLogic), BlockQuantumLogic.SubType.ObserverOff, new ModelResourceLocation("qcraft:automatic_observer", "inventory"));
        mesher.register(Item.getItemFromBlock(QBlocks.quantumLogic), BlockQuantumLogic.SubType.ObserverOn, new ModelResourceLocation("qcraft:automatic_observer_on", "inventory"));
        
        mesher.register(Item.getItemFromBlock(QBlocks.quantumOre), 0, new ModelResourceLocation("qcraft:ore", "inventory"));
        
        mesher.register(Item.getItemFromBlock(QBlocks.quantumPortal), 0, new ModelResourceLocation("qcraft:portal", "inventory"));
    }    
}