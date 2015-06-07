package dk.trustworks.framework.network;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hans on 25/04/15.
 */
public class Locator {

    private static Locator instance;

    private ServiceDiscovery<Object> serviceDiscovery;

    private Map<String, ServiceProvider> serviceProviders = new HashMap<>();

    private Locator() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("localhost:2181", new RetryNTimes(5, 1000));
        curatorFramework.start();

        try {
            serviceDiscovery = ServiceDiscoveryBuilder
                    .builder(Object.class)
                    .basePath("trustworks")
                    .client(curatorFramework).build();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Locator getInstance() {
        return instance == null?instance = new Locator():instance;
    }

    public String resolveURL(String resource) {
        System.out.println("Resource: " + resource);
        ServiceProvider serviceProvider;
        String uriSpec = "";
        if(!serviceProviders.containsKey(resource)) {
            System.out.println("New service provider");
            serviceProvider = serviceDiscovery
                    .serviceProviderBuilder()
                    .serviceName(resource)
                    .build();
            serviceProviders.put(resource, serviceProvider);
            try {
                serviceProvider.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Existing service provider");
            serviceProvider = serviceProviders.get(resource);
        }
        try {
            uriSpec = serviceProvider.getInstance().buildUriSpec();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return uriSpec;
    }
}
