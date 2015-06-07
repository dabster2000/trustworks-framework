package dk.trustworks.framework.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hans on 26/04/15.
 */
public class ServiceRegistry {

    private static ServiceRegistry instance;

    private Map<String, DefaultService> services = new HashMap<>();

    private ServiceRegistry() {
    }

    public static ServiceRegistry getInstance() {return instance == null?instance = new ServiceRegistry():instance;}

    public void registerService(String projection, DefaultService service) {
        services.put(projection, service);
    }

    public Map<String, DefaultService> getServices() {
        return services;
    }
}
