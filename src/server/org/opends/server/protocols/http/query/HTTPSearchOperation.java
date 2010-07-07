package org.opends.server.protocols.http.query;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.server.api.ClientConnection;
import org.opends.server.controls.ControlDecoder;
import org.opends.server.controls.MatchedValuesControl;
import org.opends.server.core.SearchOperation;
import org.opends.server.types.ByteString;
import org.opends.server.types.CancelRequest;
import org.opends.server.types.CancelResult;
import org.opends.server.types.CanceledOperationException;
import org.opends.server.types.Control;
import org.opends.server.types.DN;
import org.opends.server.types.DereferencePolicy;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.DisconnectReason;
import org.opends.server.types.Entry;
import org.opends.server.types.OperationType;
import org.opends.server.types.RawFilter;
import org.opends.server.types.ResultCode;
import org.opends.server.types.SearchFilter;
import org.opends.server.types.SearchResultEntry;
import org.opends.server.types.SearchResultReference;
import org.opends.server.types.SearchScope;

public class HTTPSearchOperation implements SearchOperation {

    // The base DN for the search operation.
    private final DN baseDN;

    // The search filter for the search operation.
    private final SearchFilter filter;

    // The search scope for the search operation.
    private final SearchScope scope;

    // The set of attributes that should be returned in matching entries.
    private final LinkedHashSet<String> attributes;

    private final ClientConnection clientConnection;

    public HTTPSearchOperation(ClientConnection clientConnection, DN baseDN,
            SearchScope scope, SearchFilter filter,
            LinkedHashSet<String> attributes) {
        this.baseDN = baseDN;
        this.scope = scope;
        this.filter = filter;
        this.clientConnection = clientConnection;
        if (attributes == null) {
            this.attributes = new LinkedHashSet<String>(0);
        } else {
            this.attributes = attributes;
        }
    }

    @Override
    public LinkedHashSet<String> getAttributes() {
        return attributes;
    }

    @Override
    public DN getBaseDN() {
        return baseDN;
    }

    @Override
    public DereferencePolicy getDerefPolicy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getEntriesSent() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SearchFilter getFilter() {
        return filter;
    }

    @Override
    public MatchedValuesControl getMatchedValuesControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DN getProxiedAuthorizationDN() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ByteString getRawBaseDN() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RawFilter getRawFilter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getReferencesSent() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SearchScope getScope() {
        return scope;
    }

    @Override
    public int getSizeLimit() {
        return clientConnection.getSizeLimit();
    }

    @Override
    public int getTimeLimit() {
        return clientConnection.getTimeLimit();
    }

    @Override
    public Long getTimeLimitExpiration() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getTypesOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void incrementEntriesSent() {
        // TODO Auto-generated method stub

    }

    @Override
    public void incrementReferencesSent() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isClientAcceptsReferrals() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isIncludeUsableControl() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRealAttributesOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReturnLDAPSubentries() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSendResponse() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isVirtualAttributesOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean returnEntry(Entry entry, List<Control> controls) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean returnEntry(Entry entry, List<Control> controls,
            boolean evaluateAci) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean returnReference(DN dn, SearchResultReference reference) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean returnReference(DN dn, SearchResultReference reference,
            boolean evaluateAci) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sendSearchEntry(SearchResultEntry entry)
            throws DirectoryException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean sendSearchReference(SearchResultReference reference)
            throws DirectoryException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sendSearchResultDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAttributes(LinkedHashSet<String> attributes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBaseDN(DN baseDN) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClientAcceptsReferrals(boolean clientAcceptReferrals) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDerefPolicy(DereferencePolicy derefPolicy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIncludeUsableControl(boolean includeUsableControl) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMatchedValuesControl(MatchedValuesControl controls) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProxiedAuthorizationDN(DN proxiedAuthorizationDN) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRawBaseDN(ByteString rawBaseDN) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRawFilter(RawFilter rawFilter) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRealAttributesOnly(boolean realAttributesOnly) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setReturnLDAPSubentries(boolean returnLDAPSubentries) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setScope(SearchScope scope) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSendResponse(boolean sendResponse) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSizeLimit(int sizeLimit) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTimeLimit(int timeLimit) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTimeLimitExpiration(Long timeLimitExpiration) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTypesOnly(boolean typesOnly) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setVirtualAttributesOnly(boolean virtualAttributesOnly) {
        // TODO Auto-generated method stub

    }

    @Override
    public void abort(CancelRequest cancelRequest) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addRequestControl(Control control) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addResponseControl(Control control) {
        // TODO Auto-generated method stub

    }

    @Override
    public void appendAdditionalLogMessage(Message message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void appendErrorMessage(Message message) {
        // TODO Auto-generated method stub

    }

    @Override
    public CancelResult cancel(CancelRequest cancelRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void checkIfCanceled(boolean signalTooLate)
            throws CanceledOperationException {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnectClient(DisconnectReason disconnectReason,
            boolean sendNotification, Message message) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean dontSynchronize() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public MessageBuilder getAdditionalLogMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getAttachment(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getAttachments() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DN getAuthorizationDN() {
        return clientConnection.getAuthenticationInfo().getAuthorizationDN();
    }

    @Override
    public Entry getAuthorizationEntry() {
        return clientConnection.getAuthenticationInfo().getAuthorizationEntry();
    }

    @Override
    public CancelRequest getCancelRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CancelResult getCancelResult() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    @Override
    public String[][] getCommonLogElements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getConnectionID() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public MessageBuilder getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DN getMatchedDN() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMessageID() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getOperationID() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public OperationType getOperationType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getProcessingNanoTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getProcessingStartTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getProcessingStopTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getProcessingTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<String> getReferralURLs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Control> T getRequestControl(ControlDecoder<T> d)
            throws DirectoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Control> getRequestControls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[][] getRequestLogElements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Control> getResponseControls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[][] getResponseLogElements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultCode getResultCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInternalOperation() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSynchronizationOperation() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void operationCompleted() {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerPostResponseCallback(Runnable callback) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object removeAttachment(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeRequestControl(Control control) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeResponseControl(Control control) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAdditionalLogMessage(MessageBuilder additionalLogMessage) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object setAttachment(String name, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttachments(Map<String, Object> attachments) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAuthorizationEntry(Entry authorizationEntry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDontSynchronize(boolean dontSynchronize) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setErrorMessage(MessageBuilder errorMessage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setInternalOperation(boolean isInternalOperation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMatchedDN(DN matchedDN) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setReferralURLs(List<String> referralURLs) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setResponseData(DirectoryException directoryException) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setResultCode(ResultCode resultCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSynchronizationOperation(boolean isSynchronizationOperation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void toString(StringBuilder buffer) {
        // TODO Auto-generated method stub

    }

}
