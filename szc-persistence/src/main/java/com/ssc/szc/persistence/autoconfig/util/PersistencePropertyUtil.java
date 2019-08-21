package com.ssc.szc.persistence.autoconfig.util;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lebonheur
 */
public class PersistencePropertyUtil {

    public static final String CONFIG_PREGIS = "szc.persistence";

    private static Map<String, Map<String, Object>> persistenceProperties;

    private PersistencePropertyUtil() {}

    public static Map<String, Map<String, Object>> get(Environment env) {
        if(persistenceProperties == null) {
            synchronized (PersistencePropertyUtil.class) {
                if(persistenceProperties == null) {
                    persistenceProperties = new HashMap<>();
                    String persistencePrefix = String.format("%s", CONFIG_PREGIS);

                    //TODO
                    Binder binder = Binder.get(env);
                    binder.bind(persistencePrefix, Bindable.ofInstance(persistenceProperties));

//                    PropertySourcesBinder propertySourcesBinder = new PropertySourcesBinder(
//                            ((ConfigurableEnvironment)env).getPropertySources());
//                    propertySourcesBinder.bindTo(persistencePrefix, persistenceProperties);

                }
            }
        }
        return persistenceProperties;
    }

}
