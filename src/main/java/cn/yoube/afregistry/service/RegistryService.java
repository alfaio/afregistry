package cn.yoube.afregistry.service;

import cn.yoube.afregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * interface for registry service
 *
 * @author LimMF
 * @since 2024/4/16
 **/
public interface RegistryService {

    InstanceMeta register(String service, InstanceMeta instance);

    InstanceMeta deregister(String service, InstanceMeta instance);

    List<InstanceMeta> getAllInstances(String service);

    long renew(InstanceMeta instance, String... services);

    Long version(String service);

    Map<String, Long> versions(String... services);
}
