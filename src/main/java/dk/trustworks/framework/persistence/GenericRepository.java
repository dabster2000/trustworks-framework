package dk.trustworks.framework.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import dk.trustworks.framework.service.DefaultRestInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
            /*
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM "+entityName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = stmt.executeQuery();
            result = getEntitiesFromResultSet(resultSet);
            resultSet.close();
            stmt.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public Map<String, Object> getOneEntity(String entityName, String uuid) {
        logger.debug("GenericRepository.getOneEntity");
        logger.debug("GenericRepository.getOneEntity");
        Map<String, Object> result = new HashMap<>();

        try (Connection con = database.open()) {
            return getEntityFromMap(con.createQuery("SELECT * FROM "+entityName+" WHERE uuid LIKE :uuid").addParameter("uuid", uuid).executeAndFetchTable().asList().get(0));
        } catch (Exception e) {
            logger.error("LOG00270:", e);
        }
        return result;
/*
        try {
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM "+entityName+" WHERE uuid LIKE ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, uuid);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            result = getEntityFromResultSet(resultSet);
            resultSet.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;*/
    }

    protected List<Map<String, Object>> getEntitiesFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<Map<String, Object>> entities = new ArrayList<>();
        while (resultSet.next()) {
            entities.add(getEntityFromResultSet(resultSet));
        }
        return entities;
    }

    protected List<Map<String, Object>> getEntitiesFromMapSet(List<Map<String, Object>> resultSet) throws SQLException {
        ArrayList<Map<String, Object>> entities = new ArrayList<>();
        for (Map<String, Object> map : resultSet) {
            entities.add(getEntityFromMap(map));
        }
        return entities;
    }

    protected Map<String, Object> getEntityFromResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Object> resultsMap = new HashMap<>();
        for (int i = 1; i <= columnCount; ++i) {
            String columnName = metaData.getColumnName(i).toLowerCase();
            Object object = resultSet.getObject(i);
            resultsMap.put(columnName, object);
        }
        return resultsMap;
    }

    protected Map<String, Object> getEntityFromMap(Map<String, Object> map) throws SQLException {
        Map<String, Object> resultsMap = new HashMap<>();
        for (String key : map.keySet()) {
            resultsMap.put(key, map.get(key));
        }
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
