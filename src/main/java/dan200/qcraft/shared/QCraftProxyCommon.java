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


package dan200.qcraft.shared;

import dan200.qcraft.shared.items.QItems;
import dan200.qcraft.shared.items.ItemQuantumGoggles;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import dan200.QCraft;
import dan200.qcraft.shared.blocks.QBlocks;
import dan200.qcraft.shared.recipes.QRecipes;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.*;
import net.minecraft.util.BlockPos;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;
import net.minecraftforge.fml.common.event.*;

public abstract class QCraftProxyCommon implements IQCraftProxy
{
    public QCraftProxyCommon()
    {
    }

    // IQCraftProxy implementation

    @Override
    public void preLInit(FMLPreInitializationEvent e)
    {
        // Register our own creative tab
        QCraft.creativeTab = new CreativeTabQuantumCraft(CreativeTabs.getNextID(), "qCraft"); //@todo move this
        QItems.createItems();
        QBlocks.createBlocks();
        QRecipes.createRecipes();
    }

    @Override
    public void init(FMLInitializationEvent e)
    {
        registerTileEntities();
        registerForgeHandlers();
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent e) {
        
    }

    @Override
    public abstract boolean isClient();

    @Override
    public abstract Object getQuantumComputerGUI( InventoryPlayer inventory, TileEntityQuantumComputer computer );

    @Override
    public abstract void showItemTransferGUI( EntityPlayer entityPlayer, TileEntityQuantumComputer computer );

    @Override
    public abstract void travelToServer( LostLuggage.Address address );

    @Override
    public boolean isPlayerWearingGoggles( EntityPlayer player )
    {
        ItemStack headGear = player.inventory.armorItemInSlot( 3 );
        if( headGear != null &&
                headGear.getItem() == QItems.quantumGoggles )
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlayerWearingQuantumGoggles( EntityPlayer player )
    {
        ItemStack headGear = player.inventory.armorItemInSlot( 3 );
        if( headGear != null &&
                headGear.getItem() == QItems.quantumGoggles &&
                headGear.getItemDamage() == ItemQuantumGoggles.SubTypes.QUANTUM )
        {
            return true;
        }
        return false;
    }

    @Override
    public abstract boolean isLocalPlayerWearingGoggles();

    @Override
    public abstract boolean isLocalPlayerWearingQuantumGoggles();

    @Override
    public abstract void renderQuantumGogglesOverlay( float width, float height );

    @Override
    public abstract void renderAOGogglesOverlay( float width, float height );

    @Override
    public abstract void spawnQuantumDustFX( World world, BlockPos blockpos );
    
    private void registerTileEntities()
    {
        // Tile Entities
        GameRegistry.registerTileEntity( TileEntityQBlock.class, "qblock" );
        GameRegistry.registerTileEntity( TileEntityQuantumComputer.class, "qcomputer" );
    }

    private void registerForgeHandlers()
    {
        QCraftProxyCommon.ForgeHandlers handlers = new QCraftProxyCommon.ForgeHandlers();
        MinecraftForge.EVENT_BUS.register( handlers );
        FMLCommonHandler.instance().bus().register( handlers );
        if( QCraft.enableWorldGen )
        {
            GameRegistry.registerWorldGenerator( new QuantumOreGenerator(), 1 );
        }
        NetworkRegistry.INSTANCE.registerGuiHandler( QCraft.instance, handlers );

        ConnectionHandler connectionHandler = new ConnectionHandler();
        MinecraftForge.EVENT_BUS.register( connectionHandler );
        FMLCommonHandler.instance().bus().register( connectionHandler );
    }
    
    private File getWorldSaveLocation( World world, String subPath )
    {
        File rootDir = FMLCommonHandler.instance().getMinecraftServerInstance().getFile( "." );
        File saveDir = null;
        if( QCraft.isServer() )
        {
            saveDir = new File( rootDir, world.getSaveHandler().getWorldDirectoryName() );
        }
        else
        {
            saveDir = new File( rootDir, "saves/" + world.getSaveHandler().getWorldDirectoryName() );
        }
        return new File( saveDir, subPath );
    }

    private File getEntanglementSaveLocation( World world )
    {
        return getWorldSaveLocation( world, "quantum/entanglements.bin" );
    }

    private File getEncryptionSaveLocation( World world )
    {
        return getWorldSaveLocation( world, "quantum/encryption.bin" );
    }

    public class ForgeHandlers implements
        IGuiHandler
    {
        //vars for tracking last saved data to avoid unnecessary disk IOs during world save if data did not change
        private NBTTagCompound currRootNbt = null;
        private NBTTagCompound currEncrpytionNbt = null;
        
        private ForgeHandlers()
        {
        }

        // IGuiHandler implementation

        @Override
        public Object getServerGuiElement( int id, EntityPlayer player, World world, int x, int y, int z )
        {
            TileEntity tile = world.getTileEntity( new BlockPos(x, y, z) );
            switch( id )
            {
                case QCraft.quantumComputerGUIID:
                {
                    if( tile != null && tile instanceof TileEntityQuantumComputer )
                    {
                        TileEntityQuantumComputer computer = (TileEntityQuantumComputer) tile;
                        return new ContainerQuantumComputer( player.inventory, computer );
                    }
                    break;
                }
            }
            return null;
        }

        @Override
        public Object getClientGuiElement( int id, EntityPlayer player, World world, int x, int y, int z )
        {
            TileEntity tile = world.getTileEntity( new BlockPos(x, y, z) );
            switch( id )
            {
                case QCraft.quantumComputerGUIID:
                {
                    if( tile != null && tile instanceof TileEntityQuantumComputer )
                    {
                        TileEntityQuantumComputer drive = (TileEntityQuantumComputer) tile;
                        return getQuantumComputerGUI( player.inventory, drive );
                    }
                    break;
                }
            }
            return null;
        }

        // Forge event responses

        @SubscribeEvent
        public void onWorldLoad( Load event )
        {
            if( !event.world.isRemote )
            {
                // Reset
                TileEntityQBlock.QBlockRegistry.reset();
                TileEntityQuantumComputer.ComputerRegistry.reset();
                PortalRegistry.PortalRegistry.reset();
                EncryptionRegistry.Instance.reset();

                // Load NBT
                NBTTagCompound rootnbt = loadNBTFromPath( getEntanglementSaveLocation( event.world ) );
                NBTTagCompound encryptionnbt = loadNBTFromPath( getEncryptionSaveLocation( event.world ) );
               
                // Load from NBT
                if( rootnbt != null )
                {
                    currRootNbt = rootnbt;
                    if( rootnbt.hasKey( "qblocks" ) )
                    {
                        NBTTagCompound qblocks = rootnbt.getCompoundTag( "qblocks" );
                        TileEntityQBlock.QBlockRegistry.readFromNBT( qblocks );
                    }
                    if( rootnbt.hasKey( "qcomputers" ) )
                    {
                        NBTTagCompound qcomputers = rootnbt.getCompoundTag( "qcomputers" );
                        TileEntityQuantumComputer.ComputerRegistry.readFromNBT( qcomputers );
                    }
                    if( rootnbt.hasKey( "portals" ) )
                    {
                        NBTTagCompound portals = rootnbt.getCompoundTag( "portals" );
                        PortalRegistry.PortalRegistry.readFromNBT( portals );
                    }
                }
                if( encryptionnbt != null )
                {
                    currEncrpytionNbt = encryptionnbt;
                    if( encryptionnbt.hasKey( "encryption" ) )
                    {
                        NBTTagCompound encyption = encryptionnbt.getCompoundTag( "encryption" );
                        EncryptionRegistry.Instance.readFromNBT( encyption );
                    }
                }
            }
        }

        @SubscribeEvent
        public void onWorldUnload( Unload event )
        {
            if( !event.world.isRemote )
            {
                // Reset
                TileEntityQBlock.QBlockRegistry.reset();
                TileEntityQuantumComputer.ComputerRegistry.reset();
                PortalRegistry.PortalRegistry.reset();
                EncryptionRegistry.Instance.reset();
            }
        }

        @SubscribeEvent
        public void onWorldSave( Save event )
        {
            if( !event.world.isRemote )
            {
                // Write to NBT
                NBTTagCompound rootnbt = new NBTTagCompound();
                NBTTagCompound encryptionnbt = new NBTTagCompound();

                NBTTagCompound qblocks = new NBTTagCompound();
                TileEntityQBlock.QBlockRegistry.writeToNBT( qblocks );
                rootnbt.setTag( "qblocks", qblocks );

                NBTTagCompound qcomputers = new NBTTagCompound();
                TileEntityQuantumComputer.ComputerRegistry.writeToNBT( qcomputers );
                rootnbt.setTag( "qcomputers", qcomputers );

                NBTTagCompound portals = new NBTTagCompound();
                PortalRegistry.PortalRegistry.writeToNBT( portals );
                rootnbt.setTag( "portals", portals );

                NBTTagCompound encrpytion = new NBTTagCompound();
                EncryptionRegistry.Instance.writeToNBT( encrpytion );
                encryptionnbt.setTag( "encryption", encrpytion );

                // Save NBT only if changed
                if(!rootnbt.equals(currRootNbt)){
                    saveNBTToPath( getEntanglementSaveLocation( event.world ), rootnbt );
                    currRootNbt = rootnbt;
                }
                if(!encryptionnbt.equals(currEncrpytionNbt)){
                    saveNBTToPath( getEncryptionSaveLocation( event.world ), encryptionnbt );
                    currEncrpytionNbt = encryptionnbt;
                }
            }
        }

        @SubscribeEvent
        public void onPlayerLogin( PlayerEvent.PlayerLoggedInEvent event )
        {
            EntityPlayer player = event.player;
            if( FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER )
            {
                QCraft.clearUnverifiedLuggage( player ); // Shouldn't be necessary, but can't hurt
                QCraft.requestLuggage( player );
            }
        }

        @SubscribeEvent
        public void onPlayerLogout( PlayerEvent.PlayerLoggedOutEvent event )
        {
            EntityPlayer player = event.player;
            if( FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER )
            {
                QCraft.clearUnverifiedLuggage( player );
            }
        }
    }

    public static NBTTagCompound loadNBTFromPath( File file )
    {
        try
        {
            if( file != null && file.exists() )
            {
                InputStream input = new BufferedInputStream( new FileInputStream( file ) );
                try
                {
                    return CompressedStreamTools.readCompressed( input );
                }
                finally
                {
                    input.close();
                }
            }
        }
        catch( IOException e )
        {
            QCraft.log( "Warning: failed to load QCraft entanglement info" );
        }
        return null;
    }

    public static void saveNBTToPath( File file, NBTTagCompound nbt )
    {
        try
        {
            if( file != null )
            {
                file.getParentFile().mkdirs();
                OutputStream output = new BufferedOutputStream( new FileOutputStream( file ) );
                try
                {
                    CompressedStreamTools.writeCompressed( nbt, output );
                }
                finally
                {
                    output.close();
                }
            }
        }
        catch( IOException e )
        {
            QCraft.log( "Warning: failed to save QCraft entanglement info" );
        }
    }
}
