/**
 * 
 */
package org.opends.server.protocols.http;

import java.nio.channels.SocketChannel;

import com.sun.grizzly.standalone.StaticStreamAlgorithm;

public class HTTPStrAlgorithm extends StaticStreamAlgorithm {

    public HTTPStrAlgorithm() {
        super();
    }

    @Override
    public void setChannel(SocketChannel sc) {
        super.setChannel(sc);
        HTTPConnectionHandler.putConnectionInfo(new ConnectionInfo(sc));
    }
}