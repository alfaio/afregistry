package cn.yoube.afregistry;

import cn.yoube.afregistry.health.AfHealthChecker;
import cn.yoube.afregistry.health.HealthChecker;
import cn.yoube.afregistry.service.AfRegistryService;
import cn.yoube.afregistry.service.RegistryService;
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

}
