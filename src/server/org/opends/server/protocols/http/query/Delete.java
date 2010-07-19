package org.opends.server.protocols.http.query;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opends.server.api.ClientConnection;
import org.opends.server.protocols.http.ConnectionInfo;
import org.opends.server.protocols.http.OpenDSInfo;
import org.opends.server.types.DirectoryException;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;

@Path("/{dn}")

public class Delete {
  @Context HttpContext request;
  @OpenDSInfo  ConnectionInfo connInfo;


  @DELETE
  public Response delete(@PathParam("dn") String dn, @Context UriInfo uri) {
    Response response;
    try {
      ClientConnection connection =
        RESTUtils.getClientConnection(uri.getQueryParameters(),
            (ContainerRequest) request.getRequest(),
            connInfo);
      response = RESTUtils.delete(dn, uri.getQueryParameters(), connection);
    } catch (DirectoryException e) {
      response = Response.status(Status.BAD_REQUEST).build();
    }
    return response;
  }
}
