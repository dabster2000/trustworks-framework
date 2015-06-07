package dk.trustworks.framework.service;

import dk.trustworks.framework.persistence.GenericRepository;

import java.util.List;
import java.util.Map;

/**
 * Created by hans on 17/03/15.
 */
public abstract class DefaultLocalService implements DefaultService {

    public abstract GenericRepository getGenericRepository();

    protected DefaultLocalService() {
    }

    @Override
    public List<Map<String, Object>> getAllEntities(String entityName) {
        return getGenericRepository().getAllEntities(entityName);
    }

    @Override
    public Map<String, Object> getOneEntity(String entityName, String uuid) {
        return getGenericRepository().getOneEntity(entityName, uuid);
    }

}
