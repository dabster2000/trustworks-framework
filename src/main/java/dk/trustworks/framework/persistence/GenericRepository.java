package dk.trustworks.framework.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import dk.trustworks.framework.service.DefaultRestInterface;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hans on 17/03/15.
 */
public abstract class GenericRepository implements DefaultRestInterface {

    protected final DataSource database;

    protected GenericRepository() {
        this.database = Helper.createHelper().getDatabase();
    }

    @Override
    public List<Map<String, Object>> getAllEntities(String entityName) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM "+entityName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = stmt.executeQuery();
            result = getEntitiesFromResultSet(resultSet);
            resultSet.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map<String, Object> getOneEntity(String entityName, String uuid) {
        System.out.println("GenericRepository.getOneEntity");
        System.out.println("entityName = " + entityName);
        System.out.println("uuid = " + uuid);
        Map<String, Object> result = new HashMap<>();
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
        return result;
    }

    protected List<Map<String, Object>> getEntitiesFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<Map<String, Object>> entities = new ArrayList<>();
        while (resultSet.next()) {
            entities.add(getEntityFromResultSet(resultSet));
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

    protected void testForNull(JsonNode jsonNode, String[] values) {
        for (String value : values) {
            if(jsonNode.get(value).isNull() || jsonNode.get(value).asText().trim() == "") {
                System.out.println("value is null = " + value);
                throw new RuntimeException(value + " cannot be null or empty");
            }
        }
    }
}
