/*
 *     (C) Copyright 2013 CoNWeT Lab - Universidad Politécnica de Madrid
 *
 *     This file is part of ngsijava.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.conwet.samson;

import java.util.List;
import java.util.Objects;

//import javax.ws.rs.client.ClientBuilder;
//import javax.ws.rs.client.Entity;
//import javax.ws.rs.client.WebTarget;
//import javax.ws.rs.core.MediaType;
import javax.xml.datatype.Duration;

import com.conwet.samson.jaxb.BaseContextRequest;
import com.conwet.samson.jaxb.CondValueList;
import com.conwet.samson.jaxb.ContextElement;
import com.conwet.samson.jaxb.ContextElementList;
import com.conwet.samson.jaxb.ContextRegistration;
import com.conwet.samson.jaxb.ContextRegistrationList;
import com.conwet.samson.jaxb.EntityId;
import com.conwet.samson.jaxb.EntityIdList;
import com.conwet.samson.jaxb.NotifyCondition;
import com.conwet.samson.jaxb.NotifyConditionList;
import com.conwet.samson.jaxb.NotifyConditionType;
import com.conwet.samson.jaxb.ObjectFactory;
import com.conwet.samson.jaxb.QueryContextRequest;
import com.conwet.samson.jaxb.QueryContextResponse;
import com.conwet.samson.jaxb.RegisterContextRequest;
import com.conwet.samson.jaxb.RegisterContextResponse;
import com.conwet.samson.jaxb.SubscribeContextRequest;
import com.conwet.samson.jaxb.SubscribeContextResponse;
import com.conwet.samson.jaxb.SubscribeResponse;
import com.conwet.samson.jaxb.UpdateActionType;
import com.conwet.samson.jaxb.UpdateContextRequest;
import com.conwet.samson.jaxb.UpdateContextResponse;
import com.conwet.samson.jaxb.UpdateContextSubscriptionRequest;
import com.conwet.samson.jaxb.UpdateContextSubscriptionResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class execute queries to Context Broker and unmarshal its XML response
 * into Java object.
 *
 * @author sergio
 */
public class Querier implements QueryBroker {

    private static final String QUERY = "/NGSI10/queryContext";
    private static final String REGISTER = "/NGSI9/registerContext";
    private static final String UPDATE = "/NGSI10/updateContext";
    private static final String SUBSCRIBE = "/NGSI10/subscribeContext";
    private static final String UNSUBSCRIBE = "/NGSI10/unsubscribeContext";
    private static final String UPDATE_SUBSCRIBE = "/NGSI10/updateContextSubscription";

    private static final String DISCOVER_AVAIL = "/NGSI9/discoverContextAvailability";
    private static final String SUBSCRIBE_AVAIL = "/NGSI9/subscribeContextAvailability";
    private static final String UNSUBSCRIBE_AVAIL = "/NGSI9/unsubscribeContextAvailability";
    private static final String UPDATE_AVAIL = "/NGSI9/updateContextAvailabilitySubscription";

    private String url;
    private ObjectFactory factory;

    static final Logger logger = Logger.getLogger(QueryBroker.class);

    /**
     * Construct a {@linkplain Querier} object to query the context broker at
     * the specified address.
     *
     * @param host the hostname where the context broker is running
     * @param port the port used by the context broker, must be greater than
     * zero
     */
    Querier(String host, int port) {

        Objects.requireNonNull(host, "Hostname is null");

        if (port <= 0) {

            throw new IllegalArgumentException("port must be positive: port=" + port);
        }

        this.url = "http://" + host + ":" + port;
        logger.info("orion broker base url: " + this.url);
        this.factory = new ObjectFactory();
    }

    /**
     * Construct a {@linkplain Querier} object to query the context broker at
     * the specified address.
     *
     * @param host the hostname where the context broker is running
     * @param port the port used by the context broker, must be greater than
     * zero
     * @param path the path to the context broker
     */
    Querier(String host, int port, String path) {

        Objects.requireNonNull(host, "Hostname is null");

        if (port <= 0) {

            throw new IllegalArgumentException("port must be positive: port=" + port);
        }

        if (path.indexOf("/") != 0) {
            path = "/" + path;
        };

        this.url = "http://" + host + ":" + port + "" + path;
        logger.info("orion broker base url: " + this.url);
        this.factory = new ObjectFactory();
    }

    /**
     * Creates a client request to the host with the given path and data as POST
     * content.
     *
     * @param path the path to use for request
     * @param data the POST content
     * @param clazz the class type to cast the result
     * @return a {@linkplain ContextResponse} containing server's reply
     * @throws Exception if some errors occur
     */
    private <E, T> T response(String path, E data, Class<T> clazz) throws Exception {

        Client client = new Client();
        logger.info("connecting to orion  broker: " + this.url+ path);
        WebResource webResource = client.resource(this.url + path);
        T response = webResource.type(MediaType.APPLICATION_XML).post(clazz, data);
        logger.debug("responsce recieved: \n"+response);
        
        return response;
    }

    @Override
    public EntityId newEntityId(String type, String id, boolean isPattern) {

        EntityId entityID = factory.createEntityId();
        entityID.setType(type);
        entityID.setId(id);
        entityID.setIsPattern(isPattern);

        return entityID;
    }

    @Override
    public QueryContextResponse queryContext(EntityId entityId) throws Exception {

        EntityIdList list = factory.createEntityIdList();
        list.getEntityId().add(entityId);

        QueryContextRequest request = new QueryContextRequest();
        request.setEntityIdList(list);

        return response(QUERY, request, QueryContextResponse.class);
    }

    @Override
    public RegisterContextResponse registerContext(ContextRegistration cxtReg, Duration duration) throws Exception {

        logger.debug("RegisterContextResponse");
        ContextRegistrationList list = factory.createContextRegistrationList();
        list.getContextRegistration().add(cxtReg);

        RegisterContextRequest request = factory.createRegisterContextRequest();
        request.setContextRegistrationList(list);
        request.setDuration(duration);

        return response(REGISTER, request, RegisterContextResponse.class);
    }

    @Override
    public UpdateContextResponse updateContext(ContextElement cxtElem, UpdateActionType action) throws Exception {

        logger.debug("updateContext");
        ContextElementList list = factory.createContextElementList();
        list.getContextElement().add(cxtElem);

        UpdateContextRequest request = factory.createUpdateContextRequest();
        request.setContextElementList(list);
        request.setUpdateAction(action);

        return response(UPDATE, request, UpdateContextResponse.class);
    }

    @Override
    public SubscribeResponse subscribe(List<EntityId> idList, List<String> condList,
            String reference, Duration duration,
            NotifyConditionType type) throws Exception {
        
        logger.debug("subscribe");
        SubscribeContextRequest request = factory.createSubscribeContextRequest();
        request.setReference(reference);

        setSubscriberField(request, idList, condList, duration, type);

        return response(SUBSCRIBE, request, SubscribeContextResponse.class).getSubscribeResponse();
    }

    @Override
    public SubscribeResponse subscribeUpdate(String subscriptionID, List<EntityId> idList,
            List<String> attrList, Duration duration,
            NotifyConditionType type) throws Exception {

        logger.debug("subscribeUpdate");
        UpdateContextSubscriptionRequest request = factory.createUpdateContextSubscriptionRequest();
        request.setSubscriptionId(Objects.requireNonNull(subscriptionID, "SubscriptionID is null"));

        setSubscriberField(request, idList, attrList, duration, type);
        return response(UPDATE_SUBSCRIBE, request, UpdateContextSubscriptionResponse.class)
                .getSubscribeResponse();
    }

    private void setSubscriberField(BaseContextRequest request, List<EntityId> idList,
            List<String> condList, Duration duration,
            NotifyConditionType type) {

        EntityIdList entityList = factory.createEntityIdList();

        for (EntityId entityId : idList) {

            entityList.getEntityId().add(entityId);
        }

        CondValueList cList = factory.createCondValueList();

        for (String attribute : condList) {

            cList.getCondValue().add(attribute);
        }

        NotifyCondition notifyCond = factory.createNotifyCondition();
        notifyCond.setType(Objects.requireNonNull(type, "type is null"));
        notifyCond.setCondValueList(cList);
        NotifyConditionList notList = factory.createNotifyConditionList();
        notList.getNotifyCondition().add(notifyCond);

        request.setEntityIdList(entityList);
        request.setAttributeList(factory.createAttributeList());
        request.setNotifyConditions(notList);
        request.setDuration(duration);
    }
}
