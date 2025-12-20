package uwu.connectra.connectra_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agora")
@Getter
@Setter
public class AgoraConfig {
    private String appId;
    private String appCertificate;
    private int tokenExpireTime;
}
