package org.opends.server.protocols.http.query;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

@Path("/delete")

public class Delete {
	@Context HttpContext request;
	@OpenDSInfo  ConnectionInfo connInfo;

	@GET
	@Produces("application/json")    
	public Response delete(@Context UriInfo uri) {
		Response reponse;
		try {
			ClientConnection connection = 
				RESTUtils.getClientConnection(uri.getQueryParameters(), 
						(ContainerRequest) request.getRequest(), 
						connInfo);
			reponse = RESTUtils.delete(uri.getQueryParameters(), connection);  

		} catch (DirectoryException e1) {
			reponse = Response.status(Status.BAD_REQUEST).build();
		}
		return reponse;
	}
}
