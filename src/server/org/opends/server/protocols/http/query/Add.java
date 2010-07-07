package org.opends.server.protocols.http.query;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.opends.server.api.ClientConnection;
import org.opends.server.protocols.http.ConnectionInfo;
import org.opends.server.protocols.http.OpenDSInfo;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;

@Path("/add")
@Consumes("application/json")
public class Add {
    @Context
    HttpContext request;
    @OpenDSInfo
    ConnectionInfo connInfo;

    @POST
    public Response add(@Context UriInfo uri, String content) {
        Response response;
        try {
            JSONObject j = new JSONObject(content);
            ClientConnection connection = RESTUtils.getClientConnection(
                    uri.getQueryParameters(),
                    (ContainerRequest) request.getRequest(), connInfo);
            Entry e = RESTUtils.JSON2Entry(j);
            response = RESTUtils.add(e, connection);
        } catch (JSONException e) {
            response = Response.status(Status.BAD_REQUEST).build();
        } catch (DirectoryException e1) {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }
}
