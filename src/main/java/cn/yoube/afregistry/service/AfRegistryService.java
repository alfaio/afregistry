package cn.yoube.afregistry.service;

import cn.yoube.afregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * default registry service
 *
 * @author LimMF
 * @since 2024/4/16
 **/
@Slf4j
public class AfRegistryService implements RegistryService {
    public final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    public final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    public final static AtomicLong version = new AtomicLong(0);

    @Override
    public InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (!CollectionUtils.isEmpty(metas) && metas.contains(instance)) {
            log.info(" ===> instance {} already registered", instance.toUrl());
            instance.setStatus(true);
            return instance;
        }
        log.info(" ===> register instance {}", instance.toUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);
        renew(instance, service);
        VERSIONS.put(service, version.incrementAndGet());
        return instance;
    }

    @Override
    public InstanceMeta deregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (CollectionUtils.isEmpty(metas)) {
            return null;
        }
        log.info(" ===> deregister instance {}", instance.toUrl());
        metas.removeIf(meta -> meta.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, version.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        log.info(" ===> get all instances of service {}", service);
        return REGISTRY.get(service);
    }

    @Override
    public long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }
        return now;
    }

    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services).collect(Collectors.toMap(s -> s, VERSIONS::get, (o, n) -> n));
    }
}
