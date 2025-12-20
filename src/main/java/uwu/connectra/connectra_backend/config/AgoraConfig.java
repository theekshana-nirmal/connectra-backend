package uwu.connectra.connectra_backend.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agora")
@Getter
@Setter
public class AgoraConfig {
    @NotBlank(message = "Agora App ID must not be blank")
    private String appId;

    @NotBlank(message = "Agora App Certificate must not be blank")
    private String appCertificate;

    @Min(value = 60, message = "Token expire time must be at least 60 second")
    private int tokenExpireTime;
}