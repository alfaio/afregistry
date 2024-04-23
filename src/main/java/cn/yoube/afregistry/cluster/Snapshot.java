package cn.yoube.afregistry.cluster;

import cn.yoube.afregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * @author LimMF
 * @since 2024/4/22
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {
    LinkedMultiValueMap<String, InstanceMeta> registry;
    Map<String, Long> versions;
    Map<String, Long> timestamps;
    long version;
}
