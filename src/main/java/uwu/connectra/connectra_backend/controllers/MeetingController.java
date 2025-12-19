package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.meeting.CreateMeetingRequestDTO;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.services.MeetingService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/meeting")
@Tag(name = "Meeting Controller", description = "Endpoints for managing meetings")
public class MeetingController {
    private final MeetingService meetingService;

    // Create Meeting
    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    @Operation(summary = "Create a new meeting")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> createMeeting(@RequestBody @Validated CreateMeetingRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Meeting created successfully.",
                meetingService.createMeeting(request)
        ));
    }
}
