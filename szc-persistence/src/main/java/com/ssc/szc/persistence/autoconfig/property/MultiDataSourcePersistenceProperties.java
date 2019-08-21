package com.ssc.szc.persistence.autoconfig.property;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lebonheur
 */
public class MultiDataSourcePersistenceProperties {

    private Map<String, PersistenceProperties> propertiesMap = new HashMap<>();

    public Map<String, PersistenceProperties> getPropertiesMap() {
        return propertiesMap;
    }

}
