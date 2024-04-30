package io.github.alfaio.afregistry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author LimMF
 * @since 2024/4/17
 **/
@Data
@ConfigurationProperties(prefix = "afregistry")
public class AfRegistryConfigProperties {

    private List<String> serverList;
}
