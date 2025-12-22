package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uwu.connectra.connectra_backend.dtos.AgoraTokenResponseDTO;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.meeting.CreateMeetingRequestDTO;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.dtos.meeting.UpdateMeetingRequestDTO;
import uwu.connectra.connectra_backend.services.MeetingService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/meeting")
@Tag(name = "Meeting Controller", description = "Endpoints for managing meetings")
public class MeetingController {
    private final MeetingService meetingService;

    // Create Meeting
    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER')")
    @Operation(summary = "Create a new meeting")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> createMeeting(@RequestBody @Validated CreateMeetingRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Meeting created successfully.",
                meetingService.createMeeting(request)
        ));
    }

    // Get all Meetings (created by the authenticated lecturer)
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    @GetMapping("/lecturer")
    @Operation(summary = "Get all meetings created by the authenticated lecturer")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getAllMeetings() {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                        true,
                        "Meetings retrieved successfully.",
                        meetingService.getAllMeetings()
                )
                )
        );
    }

    // Get Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @GetMapping("/{meetingId}")
    @Operation(summary = "Get meeting details by its ID")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> getMeetingById(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Meeting retrieved successfully.",
                meetingService.getMeetingById(meetingId)
        )
        ));
    }

    // Update Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @PutMapping("/{meetingId}")
    @Operation(summary = "Update meeting details by its ID")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> updateMeetingById(@PathVariable String meetingId, @RequestBody @Validated UpdateMeetingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Meeting updated successfully.",
                meetingService.updateMeetingById(meetingId, request)
        )
        ));
    }

    // Cancel Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @PutMapping("/{meetingId}/cancel")
    @Operation(summary = "Cancel meeting by its ID")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> cancelMeetingById(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Meeting canceled successfully.",
                meetingService.cancelMeetingById(meetingId)
        )
        ));
    }

    // Join Meeting by its ID (Generate Agora Token)
    @PreAuthorize("hasAnyRole('LECTURER', 'STUDENT')")
    @PostMapping("/{meetingId}/join")
    @Operation(summary = "Generate Agora token to join meeting by its ID")
    public ResponseEntity<ApiResponse<AgoraTokenResponseDTO>> joinMeetingById(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                        true,
                        "Joined meeting successfully.",
                        meetingService.joinMeeting(meetingId)
                )
                )
        );
    }

    // Stop Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @PutMapping("/{meetingId}/stop")
    @Operation(summary = "Stop meeting by its ID")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> stopMeeting(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Meeting stopped successfully.",
                meetingService.stopMeeting(meetingId)
        )
        ));
    }
}
