/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dan200.qcraft.client.blocks;

import dan200.qcraft.client.TileEntityQBlockRender;
import dan200.qcraft.client.TileEntityQuantumLogicRender;
import dan200.qcraft.shared.TileEntityQBlock;
import dan200.qcraft.shared.TileEntityQuantumLogic;
import dan200.qcraft.shared.blocks.BlockQuantumLogic;
import dan200.qcraft.shared.blocks.QBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;

/**
 *
 * @author Robijnvogel 2016-03-25
 */
public class QBlockRenderRegister {
    
    public static void registerBlockRenderer() {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        
        mesher.register(Item.getItemFromBlock(QBlocks.qBlock), 0, new ModelResourceLocation("qcraft:qblock", "inventory"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityQBlock.class, new TileEntityQBlockRender());
        
        mesher.register(Item.getItemFromBlock(QBlocks.quantumComputer), 0, new ModelResourceLocation("qcraft:computer", "inventory"));
        
        mesher.register(Item.getItemFromBlock(QBlocks.quantumLogic), 0, new ModelResourceLocation("qcraft:qlogic", "inventory"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityQuantumLogic.class, new TileEntityQuantumLogicRender());
        
        mesher.register(Item.getItemFromBlock(QBlocks.quantumOre), 0, new ModelResourceLocation("qcraft:ore", "inventory"));
        
        mesher.register(Item.getItemFromBlock(QBlocks.quantumPortal), 0, new ModelResourceLocation("qcraft:portal", "inventory"));
    }    
}
