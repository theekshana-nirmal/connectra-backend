package uwu.connectra.connectra_backend.services;

import io.agora.media.RtcTokenBuilder2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uwu.connectra.connectra_backend.config.AgoraConfig;
import uwu.connectra.connectra_backend.dtos.AgoraTokenRequestDTO;
import uwu.connectra.connectra_backend.dtos.AgoraTokenResponseDTO;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.entities.User;
import uwu.connectra.connectra_backend.repositories.MeetingRepository;
import uwu.connectra.connectra_backend.utils.CurrentUserProvider;

@RequiredArgsConstructor
@Service
@Slf4j
public class AgoraTokenService {
    private final AgoraConfig agoraConfig;
    private final MeetingRepository meetingRepository;
    private final CurrentUserProvider currentUserProvider;

    RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();

    // Generates tokens for specific channel names and user IDs
    public ApiResponse<AgoraTokenResponseDTO> generateAgoraRtcToken(AgoraTokenRequestDTO request) {
        // Get currently authenticated user
        User user = currentUserProvider.getCurrentUser();
        log.info("Authenticated user found: {}", user.getEmail());

        // Mapping authenticated user's ID to an integer UID for Agora
        int uid = (int) user.getId();

        // Calculate the token expiration time
        int tokenExpireTimeInSeconds = agoraConfig.getTokenExpireTime(); // 1 hour
        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        int expirationTimestamp = currentTimestamp + tokenExpireTimeInSeconds;

        // Generate the token using Agora's RtcTokenBuilder2
        String token;
        try {
            token = tokenBuilder.buildTokenWithUid(
                    agoraConfig.getAppId(),
                    agoraConfig.getAppCertificate(),
                    request.getChannelName(),
                    uid,
                    RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                    expirationTimestamp, // Token expire time
                    expirationTimestamp // privilege expire time
            );
            log.info("Generated Agora RTC token for user {} in channel {}", uid, request.getChannelName());
        } catch (Exception e) {
            log.error("Failed to generate Agora token for user {} in channel {}", uid, request.getChannelName(), e);
            throw new RuntimeException("Could not generate meeting token. Please contact support.");
        }

        // Return the generated token in the response DTO
        AgoraTokenResponseDTO responseDTO = new AgoraTokenResponseDTO();
        responseDTO.setAgoraToken(token);
        responseDTO.setAppId(agoraConfig.getAppId());
        responseDTO.setUId(uid);
        responseDTO.setChannelName(request.getChannelName());

        return new ApiResponse<>(
                true,
                "Agora RTC token generated successfully",
                responseDTO);
    }
}
