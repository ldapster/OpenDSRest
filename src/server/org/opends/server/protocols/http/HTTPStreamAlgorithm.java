/**
 * 
 */
package org.opends.server.protocols.http;

import java.nio.channels.SocketChannel;

import com.sun.grizzly.standalone.StaticStreamAlgorithm;

/*
 * This class is needed to grab the socket channel instance and give it
 * back to the HTTP handler.
 */
public class HTTPStreamAlgorithm extends StaticStreamAlgorithm {

    public HTTPStreamAlgorithm() {
        super();
    }

    @Override
    public void setChannel(SocketChannel sc) {
        super.setChannel(sc);
        HTTPConnectionHandler.putConnectionInfo(new ConnectionInfo(sc));
    }
}