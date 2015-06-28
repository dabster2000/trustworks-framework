package dk.trustworks.framework.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by hans on 24/04/15.
 */
public interface DefaultRestInterface {

    List<Map<String, Object>> findByParentUUID(String entityName, String parentUUIDName, String parentUUID);

    List<Map<String, Object>> getAllEntities(String entityName);

    Map<String, Object> getOneEntity(String entityName, String uuid);

    void create(JsonNode clientJsonNode) throws SQLException;

    void update(JsonNode clientJsonNode, String uuid) throws SQLException;

}
