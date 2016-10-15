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

import dan200.QCraft;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ConnectionHandler {

    @SubscribeEvent
    public void connectionOpened(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (!event.isLocal) {
            SocketAddress socketAddress = event.manager.getSocketAddress();
            if (socketAddress != null && socketAddress instanceof InetSocketAddress) {
                InetSocketAddress internet = (InetSocketAddress) socketAddress;
                String hostString;
                try {
                    Method getHostString = InetSocketAddress.class.getDeclaredMethod("getHostString", new Class[]{});
                    getHostString.setAccessible(true);
                    hostString = getHostString.invoke(internet).toString();
                } catch (Exception e) {
                    hostString = internet.getHostName();
                }
                QCraft.setCurrentServerAddress(hostString + ":" + internet.getPort());
                return;
            }
        }
        QCraft.setCurrentServerAddress(null);
    }

    @SubscribeEvent
    public void connectionClosed(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        QCraft.setCurrentServerAddress(null);
    }
}
