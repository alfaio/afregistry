package cn.yoube.afregistry.cluster;

import cn.yoube.afregistry.http.HttpInvoker;
import cn.yoube.afregistry.service.AfRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * check server health
 *
 * @author LimMF
 * @since 2024/4/23
 **/
@Slf4j
public class ServerHealth {
    Cluster cluster;
    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    long period = 5_000;
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public void checkServerHealth() {
        executor.scheduleAtFixedRate(() -> {
            try {
                // 1、更新服务状态
                updateServers();
                // 2、选主
                Election.electLeader(cluster.getServers());
                // 3、同步快照
                syncSnapshotFromLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, period, TimeUnit.MILLISECONDS);
    }


    private void syncSnapshotFromLeader() {
        Server self = cluster.self();
        Server leader = cluster.leader();
        if (!self.isLeader() && self.getVersion() < leader.getVersion()) {
            log.debug(" ===> sync snapshot from leader: {}, my version: {}", leader, self.getVersion());
            Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
            log.debug(" ===> sync  snapshot: {}", snapshot);
            AfRegistryService.restore(snapshot);
        }

    }

    private void updateServers() {
        cluster.getServers().stream().parallel().forEach(server -> {
            if (server.equals(cluster.MYSELF)) {
                return;
            }
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.debug(" ===> health check success for {}", server);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception e) {
                log.debug(" ===> health check failed for {}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }


}
