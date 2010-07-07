package org.opends.server.protocols.http;

import java.nio.channels.SocketChannel;

public class ConnectionInfo {

    private final SocketChannel sockChannel;

    public ConnectionInfo(SocketChannel sc) {
        sockChannel = sc;
    }

    public SocketChannel getSocketChannel() {
        return sockChannel;
    }
}
