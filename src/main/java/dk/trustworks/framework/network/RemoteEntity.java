package dk.trustworks.framework.network;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.HashMap;
import java.util.Map;

public class RemoteEntity {
    public RemoteEntity() {
    }

    public Map<String, Object> getOneClientEntity(String resource, String resourceUUID) {
        System.out.println("remote getOneClientEntity...");
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(Locator.getInstance().resolveURL("clientservice")+"/api/"+resource+"/"+resourceUUID)
                    .header("accept", "application/json")
                    .asJson();

            Map<String, Object> resultsMap = new HashMap<>();
            for (Object s : jsonResponse.getBody().getObject().keySet()) {
                String columnName = (String) s;
                Object object = jsonResponse.getBody().getObject().get(columnName);
                resultsMap.put(columnName, object);
            }

            return resultsMap;
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> getOneUserEntity(String resource, String resourceUUID) {
        System.out.println("remote getOneUserEntity...");
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(Locator.getInstance().resolveURL("userservice")+"/api/"+resource+"/"+resourceUUID)
                    .header("accept", "application/json")
                    .asJson();

            Map<String, Object> resultsMap = new HashMap<>();
            for (Object s : jsonResponse.getBody().getObject().keySet()) {
                String columnName = (String) s;
                Object object = jsonResponse.getBody().getObject().get(columnName);
                resultsMap.put(columnName, object);
            }

            return resultsMap;
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }
}