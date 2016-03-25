/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dan200.qcraft.client.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import dan200.qcraft.shared.items.*;
import net.minecraft.client.resources.model.ModelResourceLocation;

/**
 *
 * @author Robijnvogel 2016-03-25
 */
public final class QItemRenderRegister {

    public static void registerItemRenderer() {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        mesher.register(QItems.eos, ItemEOS.SubType.SUPERPOSITION, new ModelResourceLocation("qCraft.eos", "inventory"));
        mesher.register(QItems.eos, ItemEOS.SubType.OBSERVATION, new ModelResourceLocation("qCraft.eoo", "inventory"));
        mesher.register(QItems.eos, ItemEOS.SubType.ENTANGLEMENT, new ModelResourceLocation("qCraft.eoe", "inventory"));
        mesher.register(QItems.quantumDust, 0, new ModelResourceLocation("qcraft:dust", "inventory"));
        mesher.register(QItems.quantumGoggles, ItemQuantumGoggles.SubTypes.QUANTUM, new ModelResourceLocation("qcraft:goggles", "inventory"));
        mesher.register(QItems.quantumGoggles, ItemQuantumGoggles.SubTypes.ANTIOBSERVATION, new ModelResourceLocation("qcraft:ao_goggles", "inventory"));

        //use 0 and 1 for blocks
    }
}
