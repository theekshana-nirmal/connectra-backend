package uwu.connectra.connectra_backend.services;

import io.agora.media.RtcTokenBuilder2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uwu.connectra.connectra_backend.config.AgoraConfig;
import uwu.connectra.connectra_backend.dtos.AgoraTokenResponseDTO;
import uwu.connectra.connectra_backend.dtos.meeting.CreateMeetingRequestDTO;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.dtos.meeting.UpdateMeetingRequestDTO;
import uwu.connectra.connectra_backend.entities.*;
import uwu.connectra.connectra_backend.exceptions.InvalidMeetingTimeException;
import uwu.connectra.connectra_backend.exceptions.MeetingAlreadyEndedException;
import uwu.connectra.connectra_backend.exceptions.MeetingCancelledException;
import uwu.connectra.connectra_backend.exceptions.MeetingNotFoundException;
import uwu.connectra.connectra_backend.exceptions.UnauthorizedException;
import uwu.connectra.connectra_backend.repositories.MeetingRepository;
import uwu.connectra.connectra_backend.utils.AgoraTokenGenerator;
import uwu.connectra.connectra_backend.utils.CurrentUserProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AgoraTokenGenerator agoraTokenGenerator;
    private final AgoraConfig agoraConfig;

    // CREATE A NEW MEETING
    @Transactional
    public MeetingResponseDTO createMeeting(CreateMeetingRequestDTO request) {
        Lecturer currentLecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);

        validateMeetingTimes(request.getScheduledStartTime(), request.getScheduledEndTime());

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
        log.info("Meeting created: {} by lecturer: {}", savedMeeting.getMeetingId(), currentLecturer.getEmail());

        return mapToResponseDTO(savedMeeting);
    }

    // GET ALL MEETINGS CREATED BY THE CURRENT LECTURER
    @Transactional(readOnly = true)
    public List<MeetingResponseDTO> getAllMeetings() {
        Lecturer currentLecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);

        List<Meeting> meetings = meetingRepository
                .findAllByCreatedByEmailOrderByCreatedAtDesc(currentLecturer.getEmail());

        return meetings.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // GET MEETING BY ID
    @Transactional(readOnly = true)
    public MeetingResponseDTO getMeetingById(String meetingId) {
        Meeting meeting = findMeetingById(meetingId);
        return mapToResponseDTO(meeting);
    }

    // UPDATE MEETING BY ID
    @Transactional
    public MeetingResponseDTO updateMeetingById(String meetingId, UpdateMeetingRequestDTO request) {
        Lecturer currentLecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        Meeting meeting = findMeetingById(meetingId);

        validateLecturerOwnership(meeting, currentLecturer);
        validateMeetingNotEnded(meeting);
        validateMeetingTimes(request.getScheduledStartTime(), request.getScheduledEndTime());

        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setScheduledStartTime(request.getScheduledStartTime());
        meeting.setScheduledEndTime(request.getScheduledEndTime());
        meeting.setTargetDegree(request.getTargetDegree());
        meeting.setTargetBatch(request.getTargetBatch());

        Meeting updatedMeeting = meetingRepository.save(meeting);
        log.info("Meeting updated: {} by lecturer: {}", updatedMeeting.getMeetingId(), currentLecturer.getEmail());

        return mapToResponseDTO(updatedMeeting);
    }

    // CANCEL MEETING BY ID
    @Transactional
    public MeetingResponseDTO cancelMeetingById(String meetingId) {
        Lecturer currentLecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        Meeting meeting = findMeetingById(meetingId);

        validateLecturerOwnership(meeting, currentLecturer);
        validateMeetingNotEnded(meeting);

        meeting.setStatus(MeetingStatus.CANCELLED);
        Meeting canceledMeeting = meetingRepository.save(meeting);
        log.info("Meeting cancelled: {} by lecturer: {}", canceledMeeting.getMeetingId(), currentLecturer.getEmail());

        return mapToResponseDTO(canceledMeeting);
    }

    // JOIN MEETING BY ID
    @Transactional
    public AgoraTokenResponseDTO joinMeeting(String meetingId) {
        Meeting meeting = findMeetingById(meetingId);
        Role currentUserRole = currentUserProvider.getCurrentUserRole();

        if (currentUserRole == Role.STUDENT) {
            validateStudentMeetingAccess(meeting);
        } else if (currentUserRole == Role.LECTURER) {
            validateLecturerMeetingAccess(meeting);

            if (meeting.getStatus() == MeetingStatus.SCHEDULED) {
                startMeeting(meeting);
            }
        } else {
            throw new UnauthorizedException("You are not authorized to join this meeting.");
        }

        String agoraToken = agoraTokenGenerator.generateToken(
                meeting.getAgoraChannelName(),
                RtcTokenBuilder2.Role.ROLE_PUBLISHER);

        log.info("User joined meeting: {} with role: {}", meetingId, currentUserRole);

        return mapToAgoraTokenResponse(meeting, agoraToken);
    }

    // ==================== Private Helper Methods ====================

    // Automatically start meeting (set status to LIVE and set actual start time)
    private void startMeeting(Meeting meeting) {
        meeting.setStatus(MeetingStatus.LIVE);
        meeting.setActualStartTime(LocalDateTime.now());
        meetingRepository.save(meeting);
        log.info("Meeting automatically started (LIVE): {}", meeting.getMeetingId());
    }

    // Validate that the scheduled end time is after the scheduled start time
    private void validateMeetingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new InvalidMeetingTimeException(
                    "Scheduled end time must be after scheduled start time.");
        }
    }

    // Find meeting by ID or throw MeetingNotFoundException
    private Meeting findMeetingById(String meetingId) {
        return meetingRepository.findById(UUID.fromString(meetingId))
                .orElseThrow(() -> new MeetingNotFoundException(
                        "Meeting not found with ID: " + meetingId));
    }

    // Validate that the current lecturer is the creator of the meeting
    private void validateLecturerOwnership(Meeting meeting, Lecturer lecturer) {
        if (!Objects.equals(meeting.getCreatedBy().getId(), lecturer.getId())) {
            throw new UnauthorizedException(
                    "You are not authorized to modify this meeting.");
        }
    }

    // Validate that the meeting has not already ended
    private void validateMeetingNotEnded(Meeting meeting) {
        if (meeting.getStatus() == MeetingStatus.ENDED) {
            throw new MeetingAlreadyEndedException(
                    "Cannot modify a meeting that has already ended.");
        }
    }

    // Validate student access to the meeting
    private void validateStudentMeetingAccess(Meeting meeting) {
        Student currentStudent = currentUserProvider.getCurrentUserAs(Student.class);

        // Validate degree and batch eligibility
        if (meeting.getTargetDegree() != null &&
                !meeting.getTargetDegree().equalsIgnoreCase(currentStudent.getDegree())) {
            throw new UnauthorizedException("This meeting is not intended for your degree.");
        }

        if (meeting.getTargetBatch() != null &&
                !meeting.getTargetBatch().equals(currentStudent.getBatch())) {
            throw new UnauthorizedException("This meeting is not intended for your batch.");
        }

        // Validate meeting status
        validateMeetingStatusForJoining(meeting);
    }

    // Validate lecturer access to the meeting
    private void validateLecturerMeetingAccess(Meeting meeting) {
        Lecturer currentLecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);

        // Validate ownership
        if (!Objects.equals(meeting.getCreatedBy().getId(), currentLecturer.getId())) {
            throw new UnauthorizedException("You can only join meetings you created.");
        }

        // Validate meeting status - lecturers can join scheduled or live meetings
        if (meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new MeetingCancelledException("This meeting has been cancelled.");
        }

        if (meeting.getStatus() == MeetingStatus.ENDED) {
            throw new MeetingAlreadyEndedException("This meeting has already ended.");
        }
    }

    // Validate meeting status for joining (for students)
    private void validateMeetingStatusForJoining(Meeting meeting) {
        switch (meeting.getStatus()) {
            case CANCELLED -> throw new MeetingCancelledException("This meeting has been cancelled.");
            case SCHEDULED -> throw new UnauthorizedException("This meeting has not started yet.");
            case ENDED -> throw new MeetingAlreadyEndedException("This meeting has already ended.");
        }
    }

    // ==================== Private Mapping Methods ====================

    // Map Meeting entity to MeetingResponseDTO
    private MeetingResponseDTO mapToResponseDTO(Meeting meeting) {
        Lecturer creator = meeting.getCreatedBy();

        MeetingResponseDTO responseDTO = new MeetingResponseDTO();
        responseDTO.setMeetingId(meeting.getMeetingId());
        responseDTO.setTitle(meeting.getTitle());
        responseDTO.setDescription(meeting.getDescription());
        responseDTO.setScheduledStartTime(meeting.getScheduledStartTime());
        responseDTO.setScheduledEndTime(meeting.getScheduledEndTime());
        responseDTO.setActualStartTime(meeting.getActualStartTime());
        responseDTO.setActualEndTime(meeting.getActualEndTime());
        responseDTO.setCreatedAt(meeting.getCreatedAt());
        responseDTO.setUpdatedAt(meeting.getUpdatedAt());
        responseDTO.setTargetDegree(meeting.getTargetDegree());
        responseDTO.setTargetBatch(meeting.getTargetBatch());
        responseDTO.setStatus(meeting.getStatus().name());
        responseDTO.setCreatedById(creator.getId());
        responseDTO.setCreatedByName(creator.getFirstName() + " " + creator.getLastName());

        return responseDTO;
    }

    // Generated Agora token to AgoraTokenResponseDTO
    private AgoraTokenResponseDTO mapToAgoraTokenResponse(Meeting meeting, String agoraToken) {
        AgoraTokenResponseDTO responseDTO = new AgoraTokenResponseDTO();
        responseDTO.setMeetingId(meeting.getMeetingId().toString());
        responseDTO.setAgoraToken(agoraToken);
        responseDTO.setAppId(agoraConfig.getAppId());
        responseDTO.setUId(agoraTokenGenerator.getCurrentUserUid());
        responseDTO.setChannelName(meeting.getAgoraChannelName());

        return responseDTO;
    }
}
