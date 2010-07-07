package org.opends.server.protocols.http.query;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.Collection;

import org.opends.messages.Message;
import org.opends.server.api.ClientConnection;
import org.opends.server.api.ConnectionHandler;
import org.opends.server.core.SearchOperation;
import org.opends.server.types.CancelRequest;
import org.opends.server.types.CancelResult;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.DisconnectReason;
import org.opends.server.types.IntermediateResponse;
import org.opends.server.types.Operation;
import org.opends.server.types.SearchResultEntry;
import org.opends.server.types.SearchResultReference;

public class HTTPConnection extends ClientConnection {

    private final SocketChannel clientChannel;
    private final String clientAddress;
    private final int clientPort;

    public HTTPConnection(SocketChannel sockChannel) {
        clientChannel = sockChannel;
        clientAddress = clientChannel.socket().getInetAddress()
                .getHostAddress();
        clientPort = clientChannel.socket().getPort();
    }

    @Override
    public void cancelAllOperations(CancelRequest cancelRequest) {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancelAllOperationsExcept(CancelRequest cancelRequest,
            int messageID) {
        // TODO Auto-generated method stub

    }

    @Override
    public CancelResult cancelOperation(int messageID,
            CancelRequest cancelRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void disconnect(DisconnectReason disconnectReason,
            boolean sendNotification, Message message) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getClientAddress() {
        return clientAddress;
    }

    @Override
    public int getClientPort() {
        return clientPort;
    }

    @Override
    public ConnectionHandler<?> getConnectionHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getConnectionID() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public InetAddress getLocalAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMonitorSummary() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getNumberOfOperations() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Operation getOperationInProgress(int messageID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Operation> getOperationsInProgress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProtocol() {
        return "HTTP";
    }

    @Override
    public InetAddress getRemoteAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSSF() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getServerAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeOperationInProgress(int messageID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean sendIntermediateResponseMessage(
            IntermediateResponse intermediateResponse) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sendResponse(Operation operation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendSearchEntry(SearchOperation searchOperation,
            SearchResultEntry searchEntry) throws DirectoryException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean sendSearchReference(SearchOperation searchOperation,
            SearchResultReference searchReference) throws DirectoryException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void toString(StringBuilder buffer) {
        // TODO Auto-generated method stub

    }

}
