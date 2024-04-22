package cn.yoube.afregistry.cluster;

import cn.yoube.afregistry.AfRegistryConfigProperties;
import cn.yoube.afregistry.http.HttpInvoker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    long period = 5_000;
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

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
        executor.scheduleAtFixedRate(() -> {
            try {
                updateServers();
                electLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    private void electLeader() {
        List<Server> leaders = this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).toList();
        if (leaders.isEmpty()) {
            log.info(" ===> no leader, electing...: {}", servers);
            elect();
        } else if (leaders.size() > 1) {
            log.info(" ===> more than one leader, electing...: {}", leaders);
            elect();
        }else {
            log.info(" ===> not need elect, leader: {}", leaders.get(0));
        }

    }

    private void elect() {
        // 1.各节点自己选，算法保证大家选的是同一个
        // 2.外部有一个分布式锁，谁拿到谁是主
        // 3.分布式一致算法，比如paxos, raft， 复杂
        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    // hashCode最小为leader
                    if (candidate.hashCode() > server.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }
        if (candidate != null) {
            candidate.setLeader(true);
            log.info(" ===> elect leader success: {}", candidate);
        } else {
            log.info(" ===> elect leader failed: {}", servers);
        }
    }

    private void updateServers() {
        servers.stream().parallel().forEach(server -> {
            if (server.equals(MYSELF)) {
                return;
            }
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.info(" ===> health check success for {}", server);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception e) {
                log.info(" ===> health check failed for {}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public Server self() {
        return MYSELF;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }

}
