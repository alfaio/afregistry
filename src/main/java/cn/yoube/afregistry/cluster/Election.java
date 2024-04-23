package cn.yoube.afregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/** elect leader for servers
 * 
 * @author LimMF
 * @since 2024/4/23
 **/
@Slf4j
public class Election {

    public static void electLeader(List<Server> servers) {
        List<Server> leaders = servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).toList();
        if (leaders.isEmpty()) {
            log.warn(" ===> no leader, electing...: {}", servers);
            elect(servers);
        } else if (leaders.size() > 1) {
            log.warn(" ===> more than one leader, electing...: {}", leaders);
            elect(servers);
        }else {
            log.debug(" ===> not need elect, leader: {}", leaders.get(0));
        }

    }

    private static void elect(List<Server> servers) {
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
}
