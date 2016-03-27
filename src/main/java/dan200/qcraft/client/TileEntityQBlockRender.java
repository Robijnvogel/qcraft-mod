/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dan200.qcraft.client;

import dan200.qcraft.shared.blocks.QBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
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
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        
        //Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("modid:path/to/image.png"));
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(destroyStage, format);
        //Your rendering code goes here
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    // IItemRenderer implementation
    @Override
    public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
        switch (type) {
        case ENTITY:
        case EQUIPPED:
        case EQUIPPED_FIRST_PERSON:
        case INVENTORY: {
            return true;
        }
        case FIRST_PERSON_MAP:
        default: {
            return false;
        }
        }
    }

    @Override
    public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
        switch (helper) {
        case ENTITY_ROTATION:
        case ENTITY_BOBBING:
        case EQUIPPED_BLOCK:
        case BLOCK_3D:
        case INVENTORY_BLOCK: {
            return true;
        }
        default: {
            return false;
        }
        }
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object[] data) {
        switch (type) {
        case INVENTORY:
        case ENTITY: {
            GL11.glPushMatrix();
            GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
            QBlocks.qBlock.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            m_renderBlocks.setRenderBoundsFromBlock(QBlocks.qBlock);
            renderInventoryQBlock(m_renderBlocks, QBlocks.qBlock, item);
            GL11.glPopMatrix();
            break;
        }
        case EQUIPPED_FIRST_PERSON:
        case EQUIPPED: {
            GL11.glPushMatrix();
            QBlocks.qBlock.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            m_renderBlocks.setRenderBoundsFromBlock(QBlocks.qBlock);
            renderInventoryQBlock(m_renderBlocks, QBlocks.qBlock, item);
            GL11.glPopMatrix();
            break;
        }
        default: {
            break;
        }
        }
    }

    // IRenderFactory implementation
    @Override
    public boolean shouldRender3DInInventory(int modelID) {
        return true;
    }

    @Override
    public int getRenderId() {
        return QBlocks.qBlock.blockRenderID;
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, BlockPos blockPos, Block block, int modelID, RenderBlocks renderblocks) {
        if (modelID == getRenderId() && block == QBlocks.qBlock) {
            QBlocks.qBlock.s_forceGrass = (QBlocks.qBlock.getImpostorBlock(world, blockPos) == Blocks.grass);
            QBlocks.qBlock.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            renderblocks.setRenderBoundsFromBlock(QBlocks.qBlock);
            renderblocks.renderStandardBlock(QBlocks.qBlock, blockPos);
            QBlocks.qBlock.s_forceGrass = false;
            return true;
        }
        return false;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderblocks) {
        // IItemRenderer handles this
    }

}
