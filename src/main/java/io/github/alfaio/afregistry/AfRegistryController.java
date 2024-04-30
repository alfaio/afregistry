package io.github.alfaio.afregistry;

import io.github.alfaio.afregistry.cluster.Cluster;
import io.github.alfaio.afregistry.cluster.Server;
import io.github.alfaio.afregistry.cluster.Snapshot;
import io.github.alfaio.afregistry.model.InstanceMeta;
import io.github.alfaio.afregistry.service.AfRegistryService;
import io.github.alfaio.afregistry.service.RegistryService;
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
    @Autowired
    private Cluster cluster;

    @RequestMapping("/reg")
    public void register(@RequestParam String service, @RequestBody InstanceMeta instance) {
        checkLeader();
        log.info(" ===> register: {} @ {}", service, instance.toUrl());
        registryService.register(service, instance);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
        checkLeader();
        log.info(" ===> unregister: {} @ {}", service, instance.toUrl());
        return registryService.unregister(service, instance);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> get(@RequestParam String service) {
        log.info(" ===> getAllInstances: {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("/renew")
    public Long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        checkLeader();
        log.info(" ===> renew: {} @ {}", service, instance.toUrl());
        return registryService.renew(instance, service);
    }

    @RequestMapping("/renews")
    public Long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
        checkLeader();
        log.info(" ===> renews: {} @ {}", services, instance.toUrl());
        return registryService.renew(instance, services.split(","));
    }

    @RequestMapping("/version")
    public Long version(@RequestParam String service) {
        log.info(" ===> version: {}", service);
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String service) {
        log.info(" ===> versions: {}", service);
        return registryService.versions(service.split(","));
    }

    @RequestMapping("/info")
    public Server info() {
        log.info(" ===> info: {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ===> cluster: {}", cluster.getServers());
        return cluster.getServers();
    }

    @RequestMapping("/leader")
    public Server leader() {
        log.info(" ===> leader: {}", cluster.leader());
        return cluster.leader();
    }

    @RequestMapping("/setLeader")
    public Server setLeader() {
        cluster.self().setLeader(true);
        log.info(" ===> setLeader: {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/snapshot")
    public Snapshot snapshot() {
        return AfRegistryService.snapshot();
    }

    private void checkLeader() {
        if (!cluster.self().isLeader()) {
            throw new RuntimeException("target server not leader, the leader is " + cluster.leader().getUrl());
        }
    }

}
