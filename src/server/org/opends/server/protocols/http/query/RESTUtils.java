package org.opends.server.protocols.http.query;

import static org.opends.server.authorization.dseecompat.Aci.*;
import static org.opends.server.util.StaticUtils.toLowerCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.*;
import org.opends.messages.Category;
import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.messages.Severity;
import org.opends.server.api.AccessControlHandler;
import org.opends.server.api.ClientConnection;
import org.opends.server.core.AccessControlConfigManager;
import org.opends.server.core.AddOperation;
import org.opends.server.core.AddOperationBasis;
import org.opends.server.core.DeleteOperation;
import org.opends.server.core.DeleteOperationBasis;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.SearchOperation;
import org.opends.server.protocols.http.ConnectionInfo;
import org.opends.server.protocols.http.SecurityFilter.Authorizer;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.protocols.internal.InternalSearchOperation;
import org.opends.server.types.*;
import org.opends.server.workflowelement.localbackend.*;

import com.sun.jersey.spi.container.ContainerRequest;

public class RESTUtils {
    private RESTUtils() {
    }

    public static Response add(Entry entry, ClientConnection conn)
            throws DirectoryException {
        AccessControlHandler<?> handler = AccessControlConfigManager
                .getInstance().getAccessControlHandler();
        HTTPAddOperation addOperation = new HTTPAddOperation(
                new AddOperationBasis(conn, 0, 0, null, entry.getDN(),
                        entry.getObjectClasses(), entry.getUserAttributes(),
                        entry.getOperationalAttributes()), entry);
        if (!handler.isAllowed(addOperation))
            return Response.status(Status.FORBIDDEN).build();
        InternalClientConnection icc = InternalClientConnection
                .getRootConnection();
        AddOperation addOp = icc.processAdd(entry);
        if (addOp.getResultCode() != ResultCode.SUCCESS) {
            Message msg = Message.raw(Category.PROTOCOL, Severity.SEVERE_ERROR,
                    "add error");
            throw new DirectoryException(ResultCode.PROTOCOL_ERROR, msg);
        }
        return Response.status(Status.OK).build();
    }

    public static Response delete(MultivaluedMap<String, String> queryParams,
            ClientConnection conn) throws DirectoryException {
        DeleteOperation delOp;
        if (queryParams.containsKey("dn") && queryParams.size() == 1) {
            SearchOperation op = getSearchOperation(queryParams, conn, "dn");
            LinkedList<SearchResultEntry> entries = search(op);
            if (entries.size() != 1)
                return Response.status(Status.NOT_FOUND).build();
            Entry e = entries.getFirst();
            AccessControlHandler<?> handler = AccessControlConfigManager
                    .getInstance().getAccessControlHandler();
            LocalBackendDeleteOperation delOperation = new LocalBackendDeleteOperation(
                    new DeleteOperationBasis(conn, 0, 0, null, e.getDN()));
            if (!handler.isAllowed(delOperation))
                return Response.status(Status.FORBIDDEN).build();
            InternalClientConnection icc = InternalClientConnection
                    .getRootConnection();
            delOp = icc.processDelete(e.getDN());
        } else {
            Message msg = Message.raw(Category.PROTOCOL, Severity.SEVERE_ERROR,
                    "Invalid Params");
            throw new DirectoryException(ResultCode.PROTOCOL_ERROR, msg);
        }
        if (delOp.getResultCode() != ResultCode.SUCCESS)
            return Response.status(Status.FORBIDDEN).build();
        else
            return Response.status(Status.OK).build();
    }

    public static ClientConnection getClientConnection(
            MultivaluedMap<String, String> queryParams,
            ContainerRequest cRequest, ConnectionInfo connInfo) {

        Map<String, Object> prop = cRequest.getProperties();
        Authorizer auth = (Authorizer) prop.get("secureContext");
        HTTPConnection connection = new HTTPConnection(
                connInfo.getSocketChannel());
        connection.setAuthenticationInfo(auth.getAuthInfo());
        return connection;
    }

    public static SearchOperation getSearchOperation(
            MultivaluedMap<String, String> queryParams, ClientConnection conn,
            String dnParam) throws DirectoryException {
        DN baseDN = DN.decode(queryParams.getFirst(dnParam));
        SearchScope scope = SearchScope.BASE_OBJECT;
        SearchFilter filter = SearchFilter
                .createFilterFromString("(Objectclass=*)");
        LinkedHashSet<String> attrs = new LinkedHashSet<String>(0);
        if (queryParams.containsKey("scope")) {
            String s = queryParams.getFirst("scope");
            if (s.equalsIgnoreCase("base")) {
                scope = SearchScope.BASE_OBJECT;
            } else if (s.equalsIgnoreCase("one")) {
                scope = SearchScope.SINGLE_LEVEL;
            } else if (s.equalsIgnoreCase("sub")) {
                scope = SearchScope.WHOLE_SUBTREE;
            } else if (s.equalsIgnoreCase("subord")) {
                scope = SearchScope.SUBORDINATE_SUBTREE;
            } else {
                Message msg = Message.raw(Category.PROTOCOL,
                        Severity.SEVERE_ERROR, "HTTP Invalid scope.");
                throw new DirectoryException(ResultCode.PROTOCOL_ERROR, msg);
            }
        }
        if (queryParams.containsKey("filter")) {
            String f = queryParams.getFirst("filter");
            filter = SearchFilter.createFilterFromString(f);
        }
        if (queryParams.containsKey("attrs")) {
            String a = queryParams.getFirst("attrs");
            Pattern separatorPattern = Pattern.compile(",");
            String attrString = a.replaceAll(ZERO_OR_MORE_WHITESPACE, "");
            String[] attributeArray = separatorPattern.split(attrString);
            for (String s : attributeArray) {
                attrs.add(s);
            }
        }
        return new HTTPSearchOperation(conn, baseDN, scope, filter, attrs);
    }

    private static LinkedList<SearchResultEntry> search(SearchOperation op) {
        InternalClientConnection icc = InternalClientConnection
                .getRootConnection();
        InternalSearchOperation searchOp = icc.processSearch(op.getBaseDN(),
                op.getScope(), DereferencePolicy.NEVER_DEREF_ALIASES, 0, 0,
                false, op.getFilter(), op.getAttributes());
        return searchOp.getSearchEntries();
    }

    public static JSONObject searchToJSON(SearchOperation op)
            throws JSONException {
        JSONObject returnObj = new JSONObject();
        AccessControlHandler<?> handler = AccessControlConfigManager
                .getInstance().getAccessControlHandler();
        InternalClientConnection icc = InternalClientConnection
                .getRootConnection();
        InternalSearchOperation searchOp = icc.processSearch(op.getBaseDN(),
                op.getScope(), DereferencePolicy.NEVER_DEREF_ALIASES, 0, 0,
                false, op.getFilter(), op.getAttributes());
        for (SearchResultEntry e : searchOp.getSearchEntries()) {
            if (!handler.maySend(op, e))
                continue;
            SearchResultEntry fEntry = handler.filterEntry(op, e);
            JSONObject jObj = entryToJSONObject(fEntry);
            returnObj.accumulate(e.getDN().toNormalizedString(), jObj);
        }
        return returnObj;
    }

    private static JSONObject entryToJSONObject(org.opends.server.types.Entry e)
            throws JSONException {
        JSONObject entryObj = new JSONObject();
        entryObj.accumulate("dn", e.getDN().toString());
        for (String s : e.getObjectClasses().values()) {
            entryObj.accumulate("objectclass", s);
        }
        for (List<Attribute> attrList : e.getUserAttributes().values()) {
            for (Attribute a : attrList) {
                JSONObject aObj = new JSONObject();
                if (a.getName().equalsIgnoreCase("objectclass"))
                    continue;
                StringBuilder attrName = new StringBuilder(a.getName());
                for (String o : a.getOptions()) {
                    attrName.append(";");
                    attrName.append(o);
                }
                String attr = attrName.toString();
                for (AttributeValue v : a) {
                    aObj.accumulate(attr, v.getValue().toString());
                }
                entryObj.accumulate("attributes", aObj);
            }
        }
        return entryObj;
    }

    private static HashMap<ObjectClass, String> getObjectClasses(JSONObject jObj)
            throws JSONException, DirectoryException {
        HashMap<ObjectClass, String> objectClasses = new HashMap<ObjectClass, String>();

        Object oObj = jObj.opt("objectclass");
        if (oObj == null) {
            Message msg = Message.raw(Category.PROTOCOL, Severity.SEVERE_ERROR,
                    "missing oc");
            throw new DirectoryException(ResultCode.PROTOCOL_ERROR, msg);
        } else if (oObj instanceof JSONArray) {
            JSONArray ocs = (JSONArray) oObj;
            for (int i = 0; i < ocs.length(); i++) {
                String oc = toLowerCase(ocs.getString(i));
                ObjectClass objectClass = DirectoryServer.getObjectClass(oc);
                if (objectClass == null)
                    objectClass = DirectoryServer.getDefaultObjectClass(oc);
                if (objectClasses.containsKey(objectClass)) {
                    Message msg = Message.raw(Category.PROTOCOL,
                            Severity.SEVERE_ERROR, "duplicate oc");
                    throw new DirectoryException(ResultCode.PROTOCOL_ERROR, msg);
                } else
                    objectClasses.put(objectClass, oc);
            }
        } else {
            String oc = toLowerCase(jObj.getString("objectclass"));
            ObjectClass objectClass = DirectoryServer.getObjectClass(oc);
            if (objectClass == null)
                objectClass = DirectoryServer.getDefaultObjectClass(oc);
            objectClasses.put(objectClass, oc);
        }
        return objectClasses;
    }

    private static HashMap<AttributeType, List<AttributeBuilder>> getAttributes(
            JSONObject jObj) throws DirectoryException, JSONException {
        HashMap<AttributeType, List<AttributeBuilder>> userAttrBuilders =
            new HashMap<AttributeType, List<AttributeBuilder>>();
        JSONArray attrs = jObj.getJSONArray("attributes");
        for (int i = 0; i < attrs.length(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            Iterator<?> it = attr.keys();
            String attrName = (String) it.next();
            Object obj = attr.opt(attrName);
            if (obj == null) {
                Message msg = Message.raw(Category.PROTOCOL,
                        Severity.SEVERE_ERROR, "attribute format error");
                throw new DirectoryException(ResultCode.PROTOCOL_ERROR, msg);
            } else if (obj instanceof JSONArray) {
                JSONArray jarray = (JSONArray) obj;
                attrName = toLowerCase(attrName);
                AttributeType type = DirectoryServer.getAttributeType(attrName);
                if (type == null)
                    type = DirectoryServer.getDefaultAttributeType(attrName);
                AttributeBuilder builder = new AttributeBuilder(type, attrName);
                for (int k = 0; k < jarray.length(); k++) {
                    String v = jarray.getString(k);
                    builder.add(AttributeValues.create(type, v));
                }
                List<AttributeBuilder> attrList =
                    new ArrayList<AttributeBuilder>();
                attrList.add(builder);
                userAttrBuilders.put(type, attrList);
            } else {
                String v = attr.getString(attrName);
                attrName = toLowerCase(attrName);
                AttributeType type = DirectoryServer.getAttributeType(attrName);
                if (type == null)
                    type = DirectoryServer.getDefaultAttributeType(attrName);
                AttributeBuilder builder = new AttributeBuilder(type, attrName);
                builder.add(AttributeValues.create(type, v));
                List<AttributeBuilder> attrList =
                    new ArrayList<AttributeBuilder>();
                attrList.add(builder);
                userAttrBuilders.put(type, attrList);
            }
        }
        return userAttrBuilders;
    }

    public static Entry JSON2Entry(JSONObject jObj) throws JSONException,
            DirectoryException {
        DN dn = DN.decode(jObj.getString("dn"));
        HashMap<ObjectClass, String> objectClasses = getObjectClasses(jObj);
        HashMap<AttributeType, List<AttributeBuilder>> userAttrBuilders =
                                                    getAttributes(jObj);
        HashMap<AttributeType, List<Attribute>> userAttributes =
                                   new HashMap<AttributeType, List<Attribute>>(
                userAttrBuilders.size());
        for (Map.Entry<AttributeType, List<AttributeBuilder>> attrTypeEntry :
            userAttrBuilders.entrySet()) {
            AttributeType attrType = attrTypeEntry.getKey();
            List<AttributeBuilder> attrBuilderList = attrTypeEntry.getValue();
            List<Attribute> attrList = new ArrayList<Attribute>(
                    attrBuilderList.size());
            for (AttributeBuilder builder : attrBuilderList) {
                attrList.add(builder.toAttribute());
            }
            userAttributes.put(attrType, attrList);
        }
        HashMap<AttributeType, List<Attribute>> opAttributes =
            new HashMap<AttributeType, List<Attribute>>();
        Entry entry = new Entry(dn, objectClasses, userAttributes,opAttributes);
        MessageBuilder invalidReason = new MessageBuilder();
        if (!entry.conformsToSchema(null, false, true, false, invalidReason)) {
            Message msg = Message.raw(Category.PROTOCOL, Severity.SEVERE_ERROR,
                    "schema violation" + invalidReason.toString());
            throw new DirectoryException(ResultCode.PROTOCOL_ERROR, msg);
        }
        return entry;
    }
}
