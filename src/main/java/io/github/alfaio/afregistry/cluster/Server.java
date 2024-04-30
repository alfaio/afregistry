package io.github.alfaio.afregistry.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * registry server instance
 *
 * @author LimMF
 * @since 2024/4/17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "url")
public class Server {

    private String url;
    private boolean status;
    private boolean leader;
    private long version;

}
