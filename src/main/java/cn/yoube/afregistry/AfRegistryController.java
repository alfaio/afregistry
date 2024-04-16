package cn.yoube.afregistry;

import cn.yoube.afregistry.model.InstanceMeta;
import cn.yoube.afregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * rest controller for registry
 *
 * @author LimMF
 * @since 2024/4/16
 **/
@Slf4j
@RestController
public class AfRegistryController {

    @Autowired
    private RegistryService registryService;

    @RequestMapping("/reg")
    public void register(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> register: {} @ {}", service, instance.toUrl());
        registryService.register(service, instance);
    }

    @RequestMapping("/dereg")
    public InstanceMeta deregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> deregister: {} @ {}", service, instance.toUrl());
        return registryService.deregister(service, instance);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> get(@RequestParam String service) {
        log.info(" ===> getAllInstances: {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("renew")
    public void renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew: {} @ {}", service, instance.toUrl());
        registryService.renew(instance, service);
    }

    @RequestMapping("renews")
    public void renews(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> renews: {} @ {}", service, instance.toUrl());
        registryService.renew(instance, service.split(","));
    }

    @RequestMapping("version")
    public Long version(@RequestParam String service) {
        log.info(" ===> version: {}", service);
        return registryService.version(service);
    }

    @RequestMapping("versions")
    public Map<String, Long> versions(@RequestParam String service) {
        log.info(" ===> versions: {}", service);
        return registryService.versions(service.split(","));
    }
}
