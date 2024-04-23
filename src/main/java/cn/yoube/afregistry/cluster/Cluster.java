package cn.yoube.afregistry.cluster;

import cn.yoube.afregistry.AfRegistryConfigProperties;
import cn.yoube.afregistry.service.AfRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * registry cluster
 *
 * @author LimMF
 * @since 2024/4/17
 **/
@Slf4j
public class Cluster {

    @Value("${server.port}")
    String port;
    String host;
    Server MYSELF;
    AfRegistryConfigProperties registryConfigProperties;
    @Getter
    private List<Server> servers;

    public Cluster(AfRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    public void init() {
        try {
            host = new InetUtils(new InetUtilsProperties())
                    .findFirstNonLoopbackHostInfo().getIpAddress();
            log.info(" === > firstNonLoopbackHost: " + host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }
        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.info(" === > myself: {}", MYSELF);

        initServers();
        new ServerHealth(this).checkServerHealth();
    }

    private void initServers() {
        servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }
            if (url.equals(MYSELF.getUrl())) {
                servers.add(MYSELF);
            }else {
                Server server = new Server();
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1L);
                servers.add(server);
            }
        }
    }

    public Server self() {
        MYSELF.setVersion(AfRegistryService.VERSION.get());
        return MYSELF;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }

}
