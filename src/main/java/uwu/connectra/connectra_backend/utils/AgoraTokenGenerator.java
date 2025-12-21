package uwu.connectra.connectra_backend.utils;

import io.agora.media.RtcTokenBuilder2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uwu.connectra.connectra_backend.config.AgoraConfig;
import uwu.connectra.connectra_backend.entities.User;

/**
 * Utility component responsible for generating Agora RTC tokens.
 *
 * <p>This class uses the Agora Server SDK to create secure, time-bound
 * RTC tokens for authenticated users, enabling them to join Agora
 * channels with appropriate roles and permissions.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AgoraTokenGenerator {
    private final AgoraConfig agoraConfig;
    private final CurrentUserProvider currentUserProvider;
    private final RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();


    // Generate Agora RTC token for the currently authenticated user
    public String generateToken(String channelName, RtcTokenBuilder2.Role role) {
        // Get currently authenticated user's ID
        int uid = getCurrentUserUid();
        return generateTokenForUser(channelName, uid, role);
    }


    // Generate Agora RTC token for a specific user ID
    private String generateTokenForUser(String channelName, int uid, RtcTokenBuilder2.Role role) {
        // Calculate the token expiration time
        int tokenExpireTimeInSeconds = agoraConfig.getTokenExpireTime();
        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        int expirationTimestamp = currentTimestamp + tokenExpireTimeInSeconds;

        // Generate the token using Agora's RtcTokenBuilder2
        String token;
        try {
            token = tokenBuilder.buildTokenWithUid(
                    agoraConfig.getAppId(),
                    agoraConfig.getAppCertificate(),
                    channelName,
                    uid,
                    role,
                    expirationTimestamp, // Token expire time
                    expirationTimestamp // privilege expire time
            );
            log.info("Generated Agora RTC token for user {} in channel {} with role {}",
                    uid, channelName, role);
        } catch (Exception e) {
            log.error("Failed to generate Agora token for user {} in channel {}",
                    uid, channelName, e);
            throw new RuntimeException("Could not generate meeting token. Please contact support.");
        }

        return token;
    }

    // Get the current user's UID as an integer
    public int getCurrentUserUid() {
        User user = currentUserProvider.getCurrentUser();
        return (int) user.getId();
    }
}
