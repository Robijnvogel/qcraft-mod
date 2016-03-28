/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dan200.qcraft.client;

import dan200.qcraft.shared.TileEntityQBlock;
import dan200.qcraft.shared.blocks.QBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Robijnvogel 2016-03-25
 */
public class TileEntityQBlockRender extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {

        if (te != null && te instanceof TileEntityQBlock) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.enableBlend();
            
            TileEntityQBlock teq = (TileEntityQBlock) te;
            ResourceLocation resLoc = teq.getQStateTextureLocation();
            Minecraft.getMinecraft().getTextureManager().bindTexture(resLoc);
            
            WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
            //wr.begin(glMode, format);
            //@todo actually render the TileEntity
            //Your rendering code goes here
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }
}
