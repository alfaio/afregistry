package cn.yoube.afregistry.service;

import cn.yoube.afregistry.cluster.Snapshot;
import cn.yoube.afregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

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
    public final static LinkedMultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    public final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    public final static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
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
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (CollectionUtils.isEmpty(metas)) {
            return null;
        }
        log.info(" ===> unregister instance {}", instance.toUrl());
        metas.removeIf(meta -> meta.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        log.info(" ===> get all instances of service {}", service);
        return REGISTRY.get(service);
    }

    @Override
    public synchronized long renew(InstanceMeta instance, String... services) {
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

    public static synchronized Snapshot snapshot() {
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new ConcurrentHashMap<>(VERSIONS);
        Map<String, Long> timestamps = new ConcurrentHashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    public static synchronized long restore(Snapshot snapshot) {
        REGISTRY.clear();
        REGISTRY.addAll(snapshot.getRegistry());
        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVersions());
        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapshot.getTimestamps());
        VERSION.set(snapshot.getVersion());
        return snapshot.getVersion();
    }
}
