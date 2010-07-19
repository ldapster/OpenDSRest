package org.opends.server.protocols.http.query;

import org.opends.server.core.DeleteOperation;
import org.opends.server.types.Entry;
import org.opends.server.workflowelement.localbackend.LocalBackendDeleteOperation;

public class HTTPDeleteOperation extends LocalBackendDeleteOperation {
  public HTTPDeleteOperation(DeleteOperation delete, Entry e) {
    super(delete);
    this.entry = e;
    this.entryDN = e.getDN();
  }
}
