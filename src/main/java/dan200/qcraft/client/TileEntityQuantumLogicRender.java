/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dan200.qcraft.client;

import dan200.qcraft.shared.blocks.BlockQuantumLogic;
import dan200.qcraft.shared.blocks.QBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Robijnvogel 2016-03-25
 */
public class TileEntityQuantumLogicRender extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        //Your rendering code goes here
        GlStateManager.popMatrix();
    }

    // IRenderFactory implementation
    @Override
    public boolean shouldRender3DInInventory(int modelID) {
        return false;
    }

    @Override
    public int getRenderId() {
        return QBlocks.quantumLogic.blockRenderID;
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, BlockPos blockpos, Block block, int modelID, RenderBlocks renderblocks) {
        IBlockState blockState = world.getBlockState(blockpos);
        if (modelID == QBlocks.quantumLogic.blockRenderID) {
            int metadata = block.getMetaFromState(blockState);
            int direction = blockState.getValue("Direction");
            int subType = ((BlockQuantumLogic) block).getSubType(metadata);

            // Draw Base
            switch (direction) {
            case 0:
                renderblocks.uvRotateTop = 0;
                break;
            case 1:
                renderblocks.uvRotateTop = 1;
                break;
            case 2:
                renderblocks.uvRotateTop = 3;
                break;
            case 3:
                renderblocks.uvRotateTop = 2;
                break;
            }
            renderblocks.setRenderBoundsFromBlock(block);
            renderblocks.renderStandardBlock(block, i, j, k);
            renderblocks.uvRotateTop = 0;

            return true;
        }
        return false;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderblocks) {

    }
}
