package org.opends.server.protocols.http;

import java.lang.reflect.Type;

import javax.ws.rs.ext.Provider;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class OpenDSInfoProvider implements
        InjectableProvider<OpenDSInfo, Type> {

    public Injectable getInjectable(ComponentContext ic,
            OpenDSInfo a, Type t) {
        if (!(t instanceof Class<?>))
            return null;
        final Class<?> c = (Class<?>) t;
        if (c.isPrimitive())
            return null;
        return new Injectable() {
            public ConnectionInfo getValue() {
                return HTTPConnectionHandler.getConnectionInfo();
            }
        };
    }

    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }
}
