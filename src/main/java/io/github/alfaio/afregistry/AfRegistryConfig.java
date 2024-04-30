package io.github.alfaio.afregistry;

import io.github.alfaio.afregistry.cluster.Cluster;
import io.github.alfaio.afregistry.health.AfHealthChecker;
import io.github.alfaio.afregistry.health.HealthChecker;
import io.github.alfaio.afregistry.service.AfRegistryService;
import io.github.alfaio.afregistry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author LimMF
 * @since 2024/4/16
 **/
@Configuration
public class AfRegistryConfig {

    @Bean
    public RegistryService registryService(){
        return new AfRegistryService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService){
        return new AfHealthChecker(registryService);
    }

    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired AfRegistryConfigProperties properties){
        return new Cluster(properties);
    }

}
