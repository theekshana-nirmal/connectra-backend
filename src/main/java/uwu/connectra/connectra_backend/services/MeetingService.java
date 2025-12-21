package uwu.connectra.connectra_backend.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uwu.connectra.connectra_backend.dtos.meeting.CreateMeetingRequestDTO;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.dtos.meeting.UpdateMeetingRequestDTO;
import uwu.connectra.connectra_backend.entities.Lecturer;
import uwu.connectra.connectra_backend.entities.Meeting;
import uwu.connectra.connectra_backend.entities.MeetingStatus;
import uwu.connectra.connectra_backend.exceptions.InvalidMeetingTimeException;
import uwu.connectra.connectra_backend.exceptions.MeetingAlreadyEndedException;
import uwu.connectra.connectra_backend.exceptions.UnauthorizedException;
import uwu.connectra.connectra_backend.exceptions.UserNotFoundException;
import uwu.connectra.connectra_backend.repositories.MeetingRepository;
import uwu.connectra.connectra_backend.repositories.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    // CREATE A MEETING
    @Transactional
    public MeetingResponseDTO createMeeting(CreateMeetingRequestDTO request) {
        // Get currently authenticated user (the lecturer creating the meeting)
        Lecturer currentLecturer = getCurrentLecturer();

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

    // GET ALL MEETINGS CREATED BY THE AUTHENTICATED LECTURER
    public List<MeetingResponseDTO> getAllMeetings() {
        // Get currently authenticated user (the lecturer creating the meeting)
        Lecturer currentLecturer = getCurrentLecturer();

        List<Meeting> meetings = meetingRepository.findAllByCreatedByEmailOrderByCreatedAtDesc(currentLecturer.getEmail());

        return meetings.stream()
                .map(meeting -> getMeetingResponseDTO(meeting, currentLecturer))
                .toList();
    }

    // GET MEETING BY ITS ID
    public MeetingResponseDTO getMeetingById(String meetingId) {
        Meeting meeting = meetingRepository.findById(UUID.fromString(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        Lecturer creator = meeting.getCreatedBy(); // Get the lecturer who created the meeting

        return getMeetingResponseDTO(meeting, creator);
    }

    // UPDATE MEETING BY ITS ID
    public MeetingResponseDTO updateMeetingById(String meetingId, UpdateMeetingRequestDTO request) {
        // Get currently authenticated user (the lecturer creating the meeting)
        Lecturer currentLecturer = getCurrentLecturer();

        Meeting meeting = meetingRepository.findById(UUID.fromString(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found"));


        // Check if the current lecturer is the creator of the meeting
        if (meeting.getCreatedBy().getId() != currentLecturer.getId()) {
            throw new UnauthorizedException("You are not authorized to update this meeting.");
        }

        // If the meeting status is Ended, it cannot be updated
        if (meeting.getStatus() == MeetingStatus.ENDED) {
            throw new MeetingAlreadyEndedException("Cannot update a meeting that has already ended.");
        }

        // Check if the scheduled end time is after the scheduled start time
        if (request.getScheduledEndTime().isBefore(request.getScheduledStartTime()) ||
                request.getScheduledEndTime().isEqual(request.getScheduledStartTime())) {
            throw new InvalidMeetingTimeException("Scheduled end time must be after scheduled start time.");
        }

        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setScheduledStartTime(request.getScheduledStartTime());
        meeting.setScheduledEndTime(request.getScheduledEndTime());
        meeting.setTargetDegree(request.getTargetDegree());
        meeting.setTargetBatch(request.getTargetBatch());

        Meeting updatedMeeting = meetingRepository.save(meeting);

        Lecturer creator = updatedMeeting.getCreatedBy(); // Get the lecturer who created the meeting

        return getMeetingResponseDTO(updatedMeeting, creator);
    }

    // CANCEL MEETING BY ITS ID
    public MeetingResponseDTO cancelMeetingById(String meetingId) {
        // Get currently authenticated user (the lecturer creating the meeting)
        Lecturer currentLecturer = getCurrentLecturer();

        Meeting meeting = meetingRepository.findById(UUID.fromString(meetingId))
                .orElseThrow(() -> new RuntimeException("Meeting not found"));


        // Check if the current lecturer is the creator of the meeting
        if (meeting.getCreatedBy().getId() != currentLecturer.getId()) {
            throw new RuntimeException("You are not authorized to update this meeting.");
        }

        // If the meeting status is Ended, it cannot be canceled
        if (meeting.getStatus() == MeetingStatus.ENDED) {
            throw new RuntimeException("Cannot cancel a meeting that has already ended.");
        }

        meeting.setStatus(MeetingStatus.CANCELLED);

        Meeting canceledMeeting = meetingRepository.save(meeting);

        Lecturer creator = canceledMeeting.getCreatedBy(); // Get the lecturer who created the meeting

        return getMeetingResponseDTO(canceledMeeting, creator);
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

    // Helper method to get the currently authenticated lecturer
    private Lecturer getCurrentLecturer() {
        // Get currently authenticated user (the lecturer)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String lecturerEmail = authentication.getName(); // Gets the email from UserDetails

        return (Lecturer) userRepository.findByEmail(lecturerEmail).orElseThrow(() -> new UserNotFoundException("Lecturer not found"));
    }

    public List<MeetingResponseDTO> getStudentMeetings(String degree, Integer batch) {
        // 1. Fetch meetings based on Degree and Batch
        List<Meeting> allGroupMeetings = meetingRepository.findAllByTargetDegreeAndTargetBatch(degree, batch);

        return allGroupMeetings.stream()
                // 2. Filter: Only keep SCHEDULED or LIVE meetings
                .filter(meeting ->
                        meeting.getStatus() == MeetingStatus.SCHEDULED ||
                                meeting.getStatus() == MeetingStatus.LIVE
                )
                // 3. Sort: By scheduled start time (Ascending)
                .sorted(Comparator.comparing(Meeting::getScheduledStartTime))
                // 4. Map: Convert to DTO using existing helper, passing the meeting's creator
                .map(meeting -> getMeetingResponseDTO(meeting, meeting.getCreatedBy()))
                .collect(Collectors.toList());
    }
}
