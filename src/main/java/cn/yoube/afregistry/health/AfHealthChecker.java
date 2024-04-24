package cn.yoube.afregistry.health;

import cn.yoube.afregistry.model.InstanceMeta;
import cn.yoube.afregistry.service.AfRegistryService;
import cn.yoube.afregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * default health checker
 *
 * @author LimMF
 * @since 2024/4/16
 **/
@Slf4j
public class AfHealthChecker implements HealthChecker {

    RegistryService registryService;

    public AfHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    long timeout = 20_000;
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(() -> {
            log.info(" ===> health checker running...");
            long now = System.currentTimeMillis();
            AfRegistryService.TIMESTAMPS.forEach((serviceAndInstance, timestamp) -> {
                if (now - timestamp > timeout) {
                    log.info(" ===> health checker: {} is down", serviceAndInstance);
                    int index = serviceAndInstance.indexOf("@");
                    String service = serviceAndInstance.substring(0, index);
                    String url = serviceAndInstance.substring(index + 1);
                    InstanceMeta instance = InstanceMeta.fromUrl(url);
                    registryService.unregister(service, instance);
                    AfRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                }
            });
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}
