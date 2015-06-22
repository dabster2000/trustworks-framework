package dk.trustworks.framework.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import dk.trustworks.framework.service.DefaultRestInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hans on 17/03/15.
 */
public abstract class GenericRepository implements DefaultRestInterface {

    private static final Logger logger = LogManager.getLogger();

    protected final Sql2o database;

    protected GenericRepository() {
        this.database = Helper.createHelper().getDatabase();
    }

    @Override
    public List<Map<String, Object>> getAllEntities(String entityName) {
        logger.debug("GenericRepository.getAllEntities");
        logger.debug("entityName = [" + entityName + "]");

        try (Connection con = database.open()) {
            return getEntitiesFromMapSet(con.createQuery("SELECT * FROM " + entityName).executeAndFetchTable().asList());
        } catch (Exception e) {
            logger.error("LOG00260:", e);
        }
        return null;
    }

    @Override
    public Map<String, Object> getOneEntity(String entityName, String uuid) {
        logger.debug("GenericRepository.getOneEntity");
        logger.debug("GenericRepository.getOneEntity");
        Map<String, Object> result = new HashMap<>();

        try (Connection con = database.open()) {
            return getEntityFromMap(con.createQuery("SELECT * FROM " + entityName + " WHERE uuid LIKE :uuid").addParameter("uuid", uuid).executeAndFetchTable().asList().get(0));
        } catch (Exception e) {
            logger.error("LOG00270:", e);
        }
        return result;
    }

    protected List<Map<String, Object>> getEntitiesFromMapSet(List<Map<String, Object>> resultSet) throws SQLException {
        logger.entry(resultSet);
        ArrayList<Map<String, Object>> entities = new ArrayList<>();
        for (Map<String, Object> map : resultSet) {
            entities.add(getEntityFromMap(map));
        }
        logger.exit(entities);
        return entities;
    }

    protected Map<String, Object> getEntityFromMap(Map<String, Object> map) throws SQLException {
        logger.entry(map);
        Map<String, Object> resultsMap = new HashMap<>();
        for (String key : map.keySet()) {
            resultsMap.put(key, map.get(key));
        }
        logger.exit(resultsMap);
        return resultsMap;
    }

    protected void testForNull(JsonNode jsonNode, String[] values) {
        for (String value : values) {
            if(jsonNode.get(value).isNull() || jsonNode.get(value).asText().trim() == "") {
                logger.debug("value is null = " + value);
                throw new RuntimeException(value + " cannot be null or empty");
            }
        }
    }
}
