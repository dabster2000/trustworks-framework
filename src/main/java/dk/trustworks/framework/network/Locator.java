package dk.trustworks.framework.network;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hans on 25/04/15.
 */
public class Locator {

    private static final Logger logger = LogManager.getLogger();

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
        logger.debug("Resource: " + resource);
        ServiceProvider serviceProvider;
        String uriSpec = "";
        if(!serviceProviders.containsKey(resource)) {
            logger.debug("New service provider");
            serviceProvider = serviceDiscovery
                    .serviceProviderBuilder()
                    .serviceName(resource)
                    .build();
            serviceProviders.put(resource, serviceProvider);
            try {
                serviceProvider.start();
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        } else {
            logger.debug("Existing service provider");
            serviceProvider = serviceProviders.get(resource);
        }
        try {
            uriSpec = serviceProvider.getInstance().buildUriSpec();
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
        logger.debug(uriSpec);
        return uriSpec;
    }
}
