package uwu.connectra.connectra_backend.services;

import jakarta.security.auth.message.AuthException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uwu.connectra.connectra_backend.dtos.meeting.CreateMeetingRequestDTO;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.entities.Lecturer;
import uwu.connectra.connectra_backend.entities.Meeting;
import uwu.connectra.connectra_backend.entities.MeetingStatus;
import uwu.connectra.connectra_backend.exceptions.InvalidMeetingTimeException;
import uwu.connectra.connectra_backend.exceptions.UserNotFoundException;
import uwu.connectra.connectra_backend.repositories.MeetingRepository;
import uwu.connectra.connectra_backend.repositories.UserRepository;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    // CREATE A MEETING
    @Transactional
    public MeetingResponseDTO createMeeting(CreateMeetingRequestDTO request) {
        // Get currently authenticated user (the lecturer creating the meeting)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String lecturerEmail = authentication.getName(); // Gets the email from UserDetails

        Lecturer currentLecturer = (Lecturer) userRepository.findByEmail(lecturerEmail).orElseThrow(() -> new UserNotFoundException("Lecturer not found"));

        // Check if the scheduled end time is after the scheduled start time
        if (request.getScheduledEndTime().isBefore(request.getScheduledStartTime()) ||
                request.getScheduledEndTime().isEqual(request.getScheduledStartTime())) {
            throw new InvalidMeetingTimeException("Scheduled end time must be after scheduled start time.");
        }

        // TODO: Check if the lecturer has conflicting meetings (overlapping times)


        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setScheduledStartTime(request.getScheduledStartTime());
        meeting.setScheduledEndTime(request.getScheduledEndTime());
        meeting.setTargetDegree(request.getTargetDegree());
        meeting.setTargetBatch(request.getTargetBatch());
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setAgoraChannelName("meeting-" + UUID.randomUUID());
        meeting.setCreatedBy(currentLecturer);

        Meeting savedMeeting = meetingRepository.save(meeting);

        return getMeetingResponseDTO(savedMeeting, currentLecturer);
    }

    // Helper method to map Meeting entity to MeetingResponseDTO
    private static MeetingResponseDTO getMeetingResponseDTO(Meeting savedMeeting, Lecturer currentLecturer) {
        MeetingResponseDTO responseDTO = new MeetingResponseDTO();
        responseDTO.setMeetingId(savedMeeting.getMeetingId());
        responseDTO.setTitle(savedMeeting.getTitle());
        responseDTO.setDescription(savedMeeting.getDescription());
        responseDTO.setScheduledStartTime(savedMeeting.getScheduledStartTime());
        responseDTO.setScheduledEndTime(savedMeeting.getScheduledEndTime());
        responseDTO.setActualStartTime(savedMeeting.getActualStartTime());
        responseDTO.setActualEndTime(savedMeeting.getActualEndTime());
        responseDTO.setCreatedAt(savedMeeting.getCreatedAt());
        responseDTO.setUpdatedAt(savedMeeting.getUpdatedAt());
        responseDTO.setTargetDegree(savedMeeting.getTargetDegree());
        responseDTO.setTargetBatch(savedMeeting.getTargetBatch());
        responseDTO.setStatus(savedMeeting.getStatus().name());
        responseDTO.setCreatedById(currentLecturer.getId());

        String lecturerFullName = currentLecturer.getFirstName() + " " + currentLecturer.getLastName();
        responseDTO.setCreatedByName(lecturerFullName);
        return responseDTO;
    }
}
