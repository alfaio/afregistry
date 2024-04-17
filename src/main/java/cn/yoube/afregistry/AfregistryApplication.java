package cn.yoube.afregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AfRegistryConfigProperties.class)
public class AfregistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfregistryApplication.class, args);
    }

}
