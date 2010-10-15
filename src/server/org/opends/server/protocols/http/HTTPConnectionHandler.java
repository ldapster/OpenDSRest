package org.opends.server.protocols.http;

import static org.opends.messages.ProtocolMessages.*;
import static org.opends.server.util.StaticUtils.getExceptionMessage;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.servlet.Servlet;
import javax.ws.rs.core.UriBuilder;

import org.opends.messages.Message;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.HTTPConnectionHandlerCfg;
import org.opends.server.api.AlertGenerator;
import org.opends.server.api.ClientConnection;
import org.opends.server.api.ConnectionHandler;
import org.opends.server.api.IdentityMapper;
import org.opends.server.api.KeyManagerProvider;
import org.opends.server.api.ServerShutdownListener;
import org.opends.server.api.TrustManagerProvider;
import org.opends.server.config.ConfigException;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.QueueingStrategy;
import org.opends.server.core.WorkQueueStrategy;
import org.opends.server.extensions.NullKeyManagerProvider;
import org.opends.server.extensions.NullTrustManagerProvider;
import org.opends.server.monitors.ClientConnectionMonitorProvider;
import org.opends.server.protocols.ldap.LDAPStatistics;
import org.opends.server.types.ConfigChangeResult;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.HostPort;
import org.opends.server.types.InitializationException;
import org.opends.server.util.SelectableCertificateKeyManager;

import com.sun.grizzly.SSLConfig;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.ssl.SSLSelectorThread;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.util.net.jsse.JSSEImplementation;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class HTTPConnectionHandler extends
        ConnectionHandler<HTTPConnectionHandlerCfg> implements
        ConfigurationChangeListener<HTTPConnectionHandlerCfg>,
        ServerShutdownListener, AlertGenerator {

    private static final String CLASS_NAME =
         "org.opends.server.protocols.http.HTTPConnectionHandler";
    private static final String DEFAULT_FRIENDLY_NAME = "HTTP  Handler";
    private boolean enabled;
    final private String friendlyName;
    private String handlerName;
    private String protocol;
    private HTTPConnectionHandlerCfg currentConfig;
    private int listenPort;
    private List<HostPort> listeners;
    private LDAPStatistics statTracker;
    private ClientConnectionMonitorProvider connMonitor;
    private static final String SSL_CONTEXT_INSTANCE_NAME = "TLS";
    final Map<String, String> initParams = new HashMap<String, String>();
    
    //Used to store connection information for each connection. This works
    //because each HTTP connection is tied to a thread, unlike LDAP.
    private static final ThreadLocal<ConnectionInfo> connInfo =
                                       new ThreadLocal<ConnectionInfo>();
    
    private final static String RESOURCE_PATH = "/directory";
    private final int DEFAULT_PORT = 80;
    private SelectorThread selectorThread;
    private boolean isSSL = false;
    private SSLContext sslContext;
    static IdentityMapper<?> identityMapper;

    public HTTPConnectionHandler() {
        this(new WorkQueueStrategy(), DEFAULT_FRIENDLY_NAME);
    }

    public HTTPConnectionHandler(QueueingStrategy strategy, String name) {
        super(DEFAULT_FRIENDLY_NAME + " Thread");

        if (name == null) {
            this.friendlyName = DEFAULT_FRIENDLY_NAME;
        } else {
            this.friendlyName = name;
        }
        initParams.put("com.sun.jersey.config.property.packages",
                "org.opends.server.protocols.http");

        initParams.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                SecurityFilter.class.getName());

    }

    @Override
    public void finalizeConnectionHandler(Message finalizeReason) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<ClientConnection> getClientConnections() {
        return new LinkedList<ClientConnection>();
    }

    @Override
    public DN getComponentEntryDN() {
        return currentConfig.dn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectionHandlerName() {
        return handlerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<HostPort> getListeners() {
        return listeners;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void initializeConnectionHandler(HTTPConnectionHandlerCfg config)
            throws ConfigException, InitializationException {
        if (config.isUseSSL()) {
            isSSL = true;
            initSSL(config);
        }
        protocol = "HTTP";
        currentConfig = config;
        enabled = config.isEnabled();
        listenPort = config.getListenPort();
        DN identityMapperDN = config.getIdentityMapperDN();
        identityMapper = DirectoryServer.getIdentityMapper(identityMapperDN);
        statTracker = new LDAPStatistics(handlerName + " Statistics");
        DirectoryServer.registerMonitorProvider(statTracker);
        connMonitor = new ClientConnectionMonitorProvider(this);
        DirectoryServer.registerMonitorProvider(connMonitor);
        StringBuilder nameBuffer = new StringBuilder();
        nameBuffer.append(friendlyName);
        nameBuffer.append(" port ");
        nameBuffer.append(listenPort);
        handlerName = nameBuffer.toString();
        listeners = new LinkedList<HostPort>();
        System.setProperty(protocol + "_port", String.valueOf(listenPort));
        config.addHTTPChangeListener(this);
        selectorThread = getSelectorThread();
    }

    @Override
    public void run() {

        try {
            selectorThread.initEndpoint();
            selectorThread.startEndpoint();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
            if (selectorThread.isRunning()) {
                selectorThread.stopEndpoint();
            }
        }
    }

    @Override
    public void toString(StringBuilder buffer) {
        buffer.append(handlerName);
    }

    @Override
    public ConfigChangeResult applyConfigurationChange(
            HTTPConnectionHandlerCfg configuration) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigurationChangeAcceptable(
            HTTPConnectionHandlerCfg config, List<Message> unacceptableReasons) {
        // All validation is performed by the admin framework.
        return true;
    }

    @Override
    public String getShutdownListenerName() {
        return handlerName;
    }

    @Override
    public void processServerShutdown(Message reason) {
        if (selectorThread.isRunning()) {
            selectorThread.stopEndpoint();
        }
    }

    @Override
    public LinkedHashMap<String, String> getAlerts() {
        LinkedHashMap<String, String> alerts =
                               new LinkedHashMap<String, String>();
        alerts.put("HTTP Alert", " alert");
        return alerts;
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    private SelectorThread getSelectorThread() {
        SelectorThread selectorThread;
        ServletAdapter adapter = new ServletAdapter();
        for (Map.Entry<String, String> e : initParams.entrySet()) {
            adapter.addInitParameter(e.getKey(), e.getValue());
        }
        adapter.setServletInstance(getInstance(ServletContainer.class));
        adapter.setContextPath(RESOURCE_PATH);
        if (adapter instanceof GrizzlyAdapter) {
            GrizzlyAdapter ga = adapter;
            ga.setResourcesContextPath(RESOURCE_PATH);
        }
        if (isSSL) {
            SSLSelectorThread SSLselectorThread = new SSLSelectorThread();
            SSLselectorThread.setSSLContext(sslContext);
            try {
                SSLselectorThread
                        .setSSLImplementation(new JSSEImplementation());
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            selectorThread = SSLselectorThread;
        } else
            selectorThread = new SelectorThread();
        //Set HTTP stream algorithm class to grab socket channel info
        //from grizzly.
        selectorThread.setAlgorithmClassName(HTTPStreamAlgorithm.class.getName());
        selectorThread.setPort(listenPort);
        selectorThread.setAdapter(adapter);
        return selectorThread;
    }

    private static Servlet getInstance(Class<? extends Servlet> c) {
        try {
            return c.newInstance();
        } catch (Exception e) {
            throw new ContainerException(e);
        }
    }

    public static void putConnectionInfo(ConnectionInfo i) {
        connInfo.set(i);
    }

    public static ConnectionInfo getConnectionInfo() {
        return connInfo.get();
    }

    public static void delConnectionInfo() {
        connInfo.remove();
    }

    private void initSSL(HTTPConnectionHandlerCfg config)
            throws ConfigException {
        if (this.isSSL) {
            protocol = "LDAPS";
        }
        try {
            String alias = config.getSSLCertNickname();
            DN keyMgrDN = config.getKeyManagerProviderDN();
            DN trustMgrDN = config.getTrustManagerProviderDN();
            KeyManagerProvider<?> keyManagerProvider = DirectoryServer
                    .getKeyManagerProvider(keyMgrDN);
            if (keyManagerProvider == null)
                keyManagerProvider = new NullKeyManagerProvider();
            TrustManagerProvider<?> trustManagerProvider = DirectoryServer
                    .getTrustManagerProvider(trustMgrDN);
            if (trustManagerProvider == null)
                trustManagerProvider = new NullTrustManagerProvider();
            sslContext = SSLContext.getInstance(SSL_CONTEXT_INSTANCE_NAME);
            if (alias == null) {
                sslContext.init(keyManagerProvider.getKeyManagers(),
                        trustManagerProvider.getTrustManagers(), null);
            } else {
                sslContext.init(
                        SelectableCertificateKeyManager.wrap(
                                keyManagerProvider.getKeyManagers(), alias),
                        trustManagerProvider.getTrustManagers(), null);
            }
        } catch (NoSuchAlgorithmException nsae) {
            Message message = ERR_CONNHANDLER_SSL_CANNOT_INITIALIZE
                    .get(getExceptionMessage(nsae));
            throw new ConfigException(message);
        } catch (KeyManagementException kme) {
            Message message = ERR_CONNHANDLER_SSL_CANNOT_INITIALIZE
                    .get(getExceptionMessage(kme));
            throw new ConfigException(message);
        } catch (DirectoryException de) {
            Message message = ERR_CONNHANDLER_SSL_CANNOT_INITIALIZE
                    .get(getExceptionMessage(de));
            throw new ConfigException(message);
        }
    }
}
