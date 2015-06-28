package dk.trustworks.framework.service;

import dk.trustworks.framework.persistence.GenericRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by hans on 17/03/15.
 */
public abstract class DefaultLocalService implements DefaultService {

    private static final Logger log = LogManager.getLogger(DefaultLocalService.class);

    public abstract GenericRepository getGenericRepository();

    protected DefaultLocalService() {
    }

    @Override
    public List<Map<String, Object>> findByParentUUID(String entityName, String parentUUIDName, String parentUUID) {
        log.debug("DefaultLocalService.findByParentUUID");
        log.debug("entityName = [" + entityName + "], parentUUIDName = [" + parentUUIDName + "], parentUUID = [" + parentUUID + "]");
        return getGenericRepository().findByParentUUID(entityName, parentUUIDName, parentUUID);
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
