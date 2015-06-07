package dk.trustworks.framework.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.trustworks.framework.service.DefaultLocalService;
import dk.trustworks.framework.service.DefaultService;
import dk.trustworks.framework.service.ServiceRegistry;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by hans on 18/03/15.
 */
public abstract class DefaultHandler implements HttpHandler {

    protected final ObjectMapper mapper;

    private final String entity;

    private final List<String> commands = new ArrayList<>();

    public DefaultHandler(String entity) {
        this.mapper = new ObjectMapper();
        this.entity = entity;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        System.out.println("handleRequest: "+entity);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        String[] relativePath = exchange.getRelativePath().split("/");
        if (relativePath.length == 0 || relativePath[relativePath.length - 1].equals("")) {
            switch(exchange.getRequestMethod().toString()) {
                case "GET":
                    getAllEntities(exchange);
                    break;
                case "POST":
                    createEntity(exchange);
                    break;
            }
        } else if (relativePath.length > 1 && relativePath[1].equals("search")) {
            System.out.println("DefaultHandler.handleRequest: SEARCH");
            System.out.println("relativePath[2] = " + relativePath[2]);
            handleSearch(exchange, relativePath[2]);
        } else if (relativePath.length > 1 && commands.contains(relativePath[1])) {
            System.out.println("DefaultHandler.handleRequest: " + relativePath[1]);
            this.getClass().getDeclaredMethod(relativePath[1], HttpServerExchange.class, String[].class).invoke(this, exchange, relativePath);
        } else if (relativePath.length > 1) {
            switch(exchange.getRequestMethod().toString()) {
                case "GET":
                    System.out.println("DefaultHandler.handleRequest: GET");
                    System.out.println("relativePath = " + relativePath[1]);
                    findByUUID(exchange, relativePath[1]);
                    break;
                case "POST":
                    System.out.println("DefaultHandler.handleRequest: POST/UPDATE");
                    updateEntity(exchange, relativePath[1]);
                    break;
            }

        }
    }

    protected void getAllEntities(HttpServerExchange exchange) throws JsonProcessingException {
        List<Map<String, Object>> allEntities = getService().getAllEntities(entity);
        if(exchange.getQueryParameters().get("projection") != null) {
            for (Map<String, Object> map : allEntities) {
                for (String projectionTree : exchange.getQueryParameters().get("projection")) {
                    map.putAll(loadChildEntities(map, new LinkedList<>(Arrays.asList(projectionTree.split("/")))));
                }
            }
        }
        exchange.getResponseSender().send(mapper.writeValueAsString(allEntities));
    }

    protected void findByUUID(HttpServerExchange exchange, String uuid) throws JsonProcessingException {
        Map<String, Object> entity = getService().getOneEntity(this.entity, uuid);
        if(exchange.getQueryParameters().get("projection") != null) {
            for (String projectionTree : exchange.getQueryParameters().get("projection")) {
                entity.putAll(loadChildEntities(entity, new LinkedList<>(Arrays.asList(projectionTree.split("/")))));
            }
        }
        exchange.getResponseSender().send(mapper.writeValueAsString(entity));
    }

    protected void createEntity(HttpServerExchange exchange) throws IOException, SQLException {
        exchange.startBlocking( );
        JsonNode jsonNode = mapper.readTree(exchange.getInputStream());
        getService().create(jsonNode);
    }

    protected void updateEntity(HttpServerExchange exchange, String uuid) throws IOException, SQLException {
        exchange.startBlocking();
        JsonNode jsonNode = mapper.readTree(exchange.getInputStream());
        getService().update(jsonNode, uuid);
    }

    protected void handleSearch(HttpServerExchange exchange, String searchMethodName) throws Exception {
        System.out.println("DefaultHandler.handleSearch: "+this.getClass().toString());
        System.out.println("DefaultHandler.handleSearch: " + searchMethodName);
        Object result2 = getService().getClass().getDeclaredMethod(searchMethodName, Map.class).invoke(getService(), exchange.getQueryParameters());
        if(result2.getClass().equals(HashMap.class)) {
            exchange.getResponseSender().send(mapper.writeValueAsString(result2));
        } else {
            List<Map<String, Object>> result = (List<Map<String, Object>>) result2;
            if (exchange.getQueryParameters().get("projection") != null) {
                for (Map<String, Object> map : result) {
                    for (String projectionTree : exchange.getQueryParameters().get("projection")) {
                        map.putAll(loadChildEntities(map, new LinkedList<>(Arrays.asList(projectionTree.split("/")))));
                    }
                }
            }
            exchange.getResponseSender().send(mapper.writeValueAsString(result));
        }
    }

    private Map<String, Object> loadChildEntities(Map<String, Object> map, List<String> projectionTree) {
        String key = projectionTree.remove(0);
        Map<String, Object> childEntities = new HashMap<>();
        Map<String, DefaultService> services = ServiceRegistry.getInstance().getServices();
        if(services.containsKey(key)) {
            if(map.size() == 0) return childEntities;
            Map<String, Object> projection = services.get(key).getOneEntity(services.get(key).getResourcePath(),  map.get(key).toString());
            System.out.println("projection = " + projection);
            if(projectionTree.size() > 0) projection.putAll(loadChildEntities(projection, projectionTree));
            childEntities.put(key.substring(0, key.length() - 4), projection);
        }
        System.out.println("projectionTree = " + projectionTree.size());

        return childEntities;
    }

    protected abstract DefaultLocalService getService();

    protected void addCommand(String command) {
        commands.add(command);
    }
}
