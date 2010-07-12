package org.opends.server.protocols.http;

import java.security.Principal;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opends.server.api.IdentityMapper;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.PasswordPolicyState;
import org.opends.server.types.AuthenticationInfo;
import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;

import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class SecurityFilter implements ContainerRequestFilter {
    @Context
    UriInfo uriInfo;
    @Context
    HttpHeaders headers;
    private IdentityMapper<?> identityMapper;
    private AuthenticationInfo authInfo;

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        identityMapper = HTTPConnectionHandler.identityMapper;
        User user = authenticate(request);
        Authorizer auth = new Authorizer(user, authInfo);
        request.setSecurityContext(auth);
        Map<String, Object> prop = request.getProperties();
        prop.put("secureContext", auth);
        return request;
    }

    private User authenticate(ContainerRequest request) {
        String authentication = request
                .getHeaderValue(ContainerRequest.AUTHORIZATION);
        if (authentication == null) {
           authInfo = new AuthenticationInfo();
           return new User("","");
        }
        if (!authentication.startsWith("Basic ")) {
            return null;
        }
        authentication = authentication.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(authentication))
                .split(":");
        if (values.length < 2) {
            throw new WebApplicationException(400);
        }
        String username = values[0];
        ByteString password = ByteString.wrap(values[1].getBytes());
        if ((username == null) || (password == null)) {
            throw new WebApplicationException(400);
        }
        Entry authEntry;
        try {
            authEntry = identityMapper.getEntryForID(username);
        } catch (DirectoryException e) {
            throw new WebApplicationException(400);
        }
        if (authEntry == null) {
            throw new WebApplicationException(400);
        }
        try {
            PasswordPolicyState pwPolicyState = new PasswordPolicyState(
                    authEntry, false);
            if (!pwPolicyState.passwordMatches(password)) {
                throw new WebApplicationException(400);
            }
        } catch (Exception e) {
            throw new WebApplicationException(400);
        }
        authInfo = new AuthenticationInfo(authEntry,
                DirectoryServer.isRootDN(authEntry.getDN()));

        return new User(username, "user");
    }

    public class Authorizer implements SecurityContext {

        private final User user;
        private final Principal principal;
        private final AuthenticationInfo authInfo;

        public Authorizer(final User user, AuthenticationInfo authInfo) {
            this.user = user;
            this.authInfo = authInfo;
            this.principal = new Principal() {
                @Override
                public String getName() {
                    return user.username;
                }
            };
        }

        @Override
        public Principal getUserPrincipal() {
            return this.principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return (role.equals(user.role));
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }

        public AuthenticationInfo getAuthInfo() {
            return authInfo;
        }
    }

    public class User {
        public String username;
        public String role;

        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }

}
