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
package dan200.qcraft.client;

import dan200.qcraft.shared.blocks.BlockQuantumLogic;
import dan200.qcraft.shared.blocks.BlockQBlock;
import dan200.qcraft.client.items.QItemRenderRegister;
import dan200.qcraft.client.blocks.QBlockRenderRegister;
import dan200.qcraft.shared.items.ItemQuantumGoggles;
import dan200.qcraft.shared.items.ItemQBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import dan200.QCraft;
import dan200.qcraft.shared.*;
import dan200.qcraft.shared.blocks.QBlocks;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.*;
import org.lwjgl.opengl.GL11;

public class QCraftProxyClient extends QCraftProxyCommon {

    private static final ResourceLocation QUANTUM_GOGGLE_HUD = new ResourceLocation("qcraft", "textures/gui/goggles.png");
    private static final ResourceLocation AO_GOGGLE_HUD = new ResourceLocation("qcraft", "textures/gui/ao_goggles.png");

    private long m_tickCount;

    public QCraftProxyClient() {
        m_tickCount = 0;
    }

    // IQCraftProxy implementation
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        QItemRenderRegister.registerItemRenderer();
        QBlockRenderRegister.registerBlockRenderer(); //register block and tile-entity renderers       

        //ItemQuantumGoggles.s_renderIndex = RenderingRegistry.addNewArmourRendererPrefix("qcraft:goggles");
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public Object getQuantumComputerGUI(InventoryPlayer inventory, TileEntityQuantumComputer computer) {
        return new GuiQuantumComputer(inventory, computer);
    }

    @Override
    public void showItemTransferGUI(EntityPlayer entityPlayer, TileEntityQuantumComputer computer) {
        if (Minecraft.getMinecraft().currentScreen == null) {
            FMLClientHandler.instance().displayGuiScreen(entityPlayer, new GuiItemTransfer(computer));
        }
    }

    @Override
    public void travelToServer(LostLuggage.Address address) {
        // Disconnect from current server
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.theWorld.sendQuittingDisconnectingPacket();
        minecraft.loadWorld((WorldClient) null);
        minecraft.displayGuiScreen(new GuiTravelStandby(address));
    }

    @Override
    public void spawnQuantumDustFX(World world, BlockPos blockPos) {
        Minecraft mc = Minecraft.getMinecraft();
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        double dx = mc.getRenderViewEntity().posX - x;
        double dy = mc.getRenderViewEntity().posY - y;
        double dz = mc.getRenderViewEntity().posZ - z;
        if (dx * dx + dy * dy + dz * dz < 16.0 * 16.0) {
            EntityFX fx = new EntityQuantumDustFX(world, x, y, z, 1.0f);
            mc.effectRenderer.addEffect(fx);
        }
    }

    @Override
    public EntityPlayer getLocalPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public boolean isLocalPlayerWearingGoggles() {
        EntityPlayer player = getLocalPlayer();
        if (player != null) {
            return isPlayerWearingGoggles(player);
        }
        return false;
    }

    @Override
    public boolean isLocalPlayerWearingQuantumGoggles() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            return isPlayerWearingQuantumGoggles(player);
        }
        return false;
    }

    public class ForgeHandlers {

        public ForgeHandlers() {
        }

        // Forge/FML event responses
        @SubscribeEvent
        public void handleTick(TickEvent.ClientTickEvent clientTickEvent) {
            if (clientTickEvent.phase == TickEvent.Phase.START) {
                m_tickCount++;
            }

            if (QCraft.travelNextTick != null) {
                travelToServer(QCraft.travelNextTick);
                QCraft.travelNextTick = null;
            }
        }
    }
    
    private void renderInventoryQBlock(int renderblocks, BlockQBlock block, ItemStack item) {
        Map<EnumFacing, Integer> types = ItemQBlock.getTypes(item);
        int type = cycleType(types);
        if (type < 0) {
            renderInventoryQBlock(renderblocks, block, 0, BlockQBlock.Appearance.Fuzz);
        } else {
            renderInventoryQBlock(renderblocks, block, type, BlockQBlock.Appearance.Block);
        }
    }

    private int cycleType(Map<EnumFacing, Integer> types) {
        int type = -99;
        int cycle = (int) (m_tickCount % (6 * 20));
        int subcycle = (cycle % 20);
        if (subcycle > 5) {
            type = types.get(EnumFacing.getFront(cycle / 20));
        }
        return type;
    }

    private void bindColor(int c) {
        float r = (float) (c >> 16 & 255) / 255.0F;
        float g = (float) (c >> 8 & 255) / 255.0F;
        float b = (float) (c & 255) / 255.0F;
        GL11.glColor4f(r, g, b, 1.0f);
    }

    private void renderInventoryQBlock(int renderblocks, BlockQBlock block, int type, BlockQBlock.Appearance appearance) {
        Tessellator tessellator = Tessellator.getInstance();
        //tessellator.startDrawingQuads();
        bindColor(block.getColorForType(EnumFacing.DOWN, type));
        //tessellator.setNormal(0.0F, -1F, 0.0F);
        //renderblocks.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIconForType(0, type, appearance));
        tessellator.draw();

        //tessellator.startDrawingQuads();
        bindColor(block.getColorForType(EnumFacing.UP, type));
        //tessellator.setNormal(0.0F, 1.0F, 0.0F);
        //renderblocks.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIconForType(1, type, appearance));
        tessellator.draw();

        //tessellator.startDrawingQuads();
        bindColor(block.getColorForType(EnumFacing.NORTH, type));
        //tessellator.setNormal(0.0F, 0.0F, -1F);
        //renderblocks.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIconForType(2, type, appearance));
        tessellator.draw();

        //tessellator.startDrawingQuads();
        bindColor(block.getColorForType(EnumFacing.SOUTH, type));
        //tessellator.setNormal(0.0F, 0.0F, 1.0F);
        //renderblocks.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIconForType(3, type, appearance));
        tessellator.draw();

        //tessellator.startDrawingQuads();
        bindColor(block.getColorForType(EnumFacing.WEST, type));
        //tessellator.setNormal(-1F, 0.0F, 0.0F);
        //renderblocks.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIconForType(4, type, appearance));
        tessellator.draw();

        //tessellator.startDrawingQuads();
        bindColor(block.getColorForType(EnumFacing.EAST, type));
        //tessellator.setNormal(1.0F, 0.0F, 0.0F);
        //renderblocks.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIconForType(5, type, appearance));
        tessellator.draw();
    }

    @Override
    public void renderQuantumGogglesOverlay(float width, float height) {
        renderOverlay(QUANTUM_GOGGLE_HUD, width, height);
    }

    @Override
    public void renderAOGogglesOverlay(float width, float height) {
        renderOverlay(AO_GOGGLE_HUD, width, height);
    }

    @SuppressWarnings("empty-statement")
    private void renderOverlay(ResourceLocation texture, float width, float height) {
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        mc.renderEngine.bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        //tessellator.startDrawingQuads();
        int[] temp = {0, (int) height, -90, 0, 1};
        tessellator.getWorldRenderer().addVertexData(temp);
        int[] temp2 = {(int) width, (int) height, -90, 1, 1};
        tessellator.getWorldRenderer().addVertexData(temp2);
        int[] temp3 = {(int) width, 0, -90, 1, 0};
        tessellator.getWorldRenderer().addVertexData(temp3);
        int[] temp4 = {0, 0, -90, 0, 0};
        tessellator.getWorldRenderer().addVertexData(temp4);
        tessellator.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
