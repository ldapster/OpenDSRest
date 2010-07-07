package org.opends.server.protocols.http.query;

import org.opends.server.core.AddOperation;
import org.opends.server.types.Entry;
import org.opends.server.workflowelement.localbackend.LocalBackendAddOperation;

public class HTTPAddOperation extends LocalBackendAddOperation {

    public HTTPAddOperation(AddOperation add, Entry e) {
        super(add);
        this.entry = e;
        this.entryDN = e.getDN();
        this.operationalAttributes = e.getOperationalAttributes();
        this.userAttributes = e.getUserAttributes();
        this.objectClasses = e.getObjectClasses();
    }

}
