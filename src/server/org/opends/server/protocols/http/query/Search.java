package org.opends.server.protocols.http.query;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.opends.server.api.ClientConnection;
import org.opends.server.core.SearchOperation;
import org.opends.server.protocols.http.ConnectionInfo;
import org.opends.server.protocols.http.OpenDSInfo;
import org.opends.server.types.DirectoryException;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;

@Path("/search")
public class Search {

	@Context HttpContext request;
	@OpenDSInfo  ConnectionInfo connInfo;

	@GET
	@Produces("application/json")    
	public Response search(@Context UriInfo uri) {
		Response response;
		MultivaluedMap<String, String> queryParams = uri.getQueryParameters();
		try {
			ClientConnection connection = 
				RESTUtils.getClientConnection(uri.getQueryParameters(), 
						(ContainerRequest) request.getRequest(), 
						connInfo);
			SearchOperation op = 
				RESTUtils.getSearchOperation(queryParams, connection, "base");
			JSONObject returnObj = RESTUtils.searchToJSON(op);  
			response = Response.ok(returnObj).build();
		} catch (DirectoryException e1) {
			response = Response.status(Status.BAD_REQUEST).build();
		} catch (JSONException e) {
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}
}
