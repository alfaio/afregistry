package cn.yoube.afregistry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * 描述服务实例的元数据
 *
 * @author LimMF
 * @since 2024/3/20
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"scheme", "host", "port", "context"})
public class InstanceMeta {

    private String scheme;
    private String host;
    private Integer port;
    private String context;

    private boolean status;
    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String scheme, String host, Integer port, String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public static InstanceMeta fromUrl(String url) {
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath().substring(1));
    }

    public String toPath() {
        return String.format("%s_%s", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%s/%s", scheme, host, port, context);
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "");
    }

    public String toMetas() {
        return JSON.toJSONString(parameters);
    }
}
