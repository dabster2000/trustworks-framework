package dk.trustworks.framework.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by hans on 24/04/15.
 */
public interface DefaultService extends DefaultRestInterface {
    String getResourcePath();
}
