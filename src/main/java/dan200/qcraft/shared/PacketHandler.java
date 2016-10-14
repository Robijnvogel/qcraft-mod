/**
 * This file is part of qCraft - http://www.qcraft.org Copyright Daniel Ratcliffe and
 * TeacherGaming LLC, 2013. Do not distribute without permission. Send enquiries
 * to dratcliffe@gmail.com
 */
package dan200.qcraft.shared;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import dan200.QCraft;
import net.minecraft.network.NetHandlerPlayServer;

public class PacketHandler {

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        try {
            QCraftPacket packet = new QCraftPacket();
            packet.fromBytes(event.packet.payload());
            QCraft.handleClientPacket(packet);
        } catch (Exception e) {
            QCraft.log("Something went wrong while handling a client packet: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        try {
            QCraftPacket packet = new QCraftPacket();
            packet.fromBytes(event.packet.payload());
            QCraft.handleServerPacket(packet, ((NetHandlerPlayServer) event.handler).playerEntity);
        } catch (Exception e) {
            QCraft.log("Something went wrong while handling a server packet: " + e.getMessage());
        }
    }
}
