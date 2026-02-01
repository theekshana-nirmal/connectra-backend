package uwu.connectra.connectra_backend.services;

import io.agora.media.RtcTokenBuilder2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uwu.connectra.connectra_backend.config.AgoraConfig;
import uwu.connectra.connectra_backend.dtos.AgoraTokenResponseDTO;
import uwu.connectra.connectra_backend.dtos.AttendanceReportResponseDTO;
import uwu.connectra.connectra_backend.dtos.ParticipantDTO;
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
import uwu.connectra.connectra_backend.repositories.StudentRepository;
import uwu.connectra.connectra_backend.utils.AgoraTokenGenerator;
import uwu.connectra.connectra_backend.utils.CurrentUserProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingService {
    private final AttendanceService attendanceService;
    private final MeetingRepository meetingRepository;
    private final StudentRepository studentRepository;
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

        // Get UID first (needed for attendance tracking)
        int agoraUid = agoraTokenGenerator.getCurrentUserUid();

        if (currentUserRole == Role.STUDENT) {
            validateStudentMeetingAccess(meeting);
            try {
                attendanceService.recordStudentAttendanceOnJoin(meeting, agoraUid);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Ignore duplicate attendance record (race condition)
                log.debug("Concurrent attendance recording detected for student in meeting {}", meetingId);
            }
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

    // LEAVE MEETING BY ID
    @Transactional
    public String leaveMeeting(String meetingId) {
        Meeting meeting = findMeetingById(meetingId);
        Role currentUserRole = currentUserProvider.getCurrentUserRole();

        // If the user is a student, update their attendance record
        if (currentUserRole == Role.STUDENT) {
            attendanceService.recordStudentAttendanceOnLeave(meeting);
        }

        log.info("User left meeting: {} with role: {}", meetingId, currentUserRole);
        return String.format("Left meeting %s successfully.", meetingId);
    }

    // STOP MEETING BY ID
    @Transactional
    public MeetingResponseDTO stopMeeting(String meetingId) {
        Meeting meeting = findMeetingById(meetingId);
        Role currentUserRole = currentUserProvider.getCurrentUserRole();

        if (currentUserRole != Role.LECTURER) {
            throw new UnauthorizedException("Only lecturers can stop the meeting.");
        }

        Lecturer currentLecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);

        validateLecturerOwnership(meeting, currentLecturer);

        validateMeetingIsLive(meeting);

        meeting.setStatus(MeetingStatus.ENDED);
        meeting.setActualEndTime(LocalDateTime.now());

        Meeting stoppedMeeting = meetingRepository.save(meeting);

        // Finalize all attendance records for the meeting
        attendanceService.finalizeAttendanceForMeeting(stoppedMeeting);

        log.info("Meeting stopped: {} by lecturer: {}", stoppedMeeting.getMeetingId(), currentLecturer.getEmail());

        return mapToResponseDTO(stoppedMeeting);
    }

    // GET ATTENDANCE REPORT DATA FOR A MEETING
    @Transactional(readOnly = true)
    public AttendanceReportResponseDTO generateAttendanceReport(String meetingId) {
        Meeting meeting = findMeetingById(meetingId);
        validateLecturerCanAccessReport(meeting);
        validateMeetingHasEnded(meeting);

        AttendanceData attendanceData = collectAttendanceData(meeting);
        return mapToAttendanceReportDTO(meeting, attendanceData);
    }

    // ==================== Attendance Report Helper Methods ====================
    // Record to hold attendance data
    private record AttendanceData(
            List<Student> targetStudents,
            List<Student> presentStudents,
            List<Student> partiallyPresentStudents,
            List<Student> absentStudents) {
    }

    // Validate that the current lecturer can access the attendance report
    private void validateLecturerCanAccessReport(Meeting meeting) {
        Role currentUserRole = currentUserProvider.getCurrentUserRole();

        if (currentUserRole != Role.LECTURER) {
            throw new UnauthorizedException("Only lecturers can generate attendance reports.");
        }

        Lecturer currentLecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        validateLecturerOwnership(meeting, currentLecturer);
    }

    // Collect attendance data for the meeting
    private AttendanceData collectAttendanceData(Meeting meeting) {
        String targetDegree = meeting.getTargetDegree();
        int targetBatch = meeting.getTargetBatch();

        List<Student> targetStudents = studentRepository.findAllByDegreeAndBatch(targetDegree, targetBatch);
        List<Student> presentStudents = attendanceService.getAttendanceStatusForStudentInMeeting(
                meeting.getMeetingId(), AttendanceStatus.PRESENT);
        List<Student> partiallyPresentStudents = attendanceService.getAttendanceStatusForStudentInMeeting(
                meeting.getMeetingId(), AttendanceStatus.PARTIALLY_PRESENT);

        List<Student> totalParticipatedStudents = attendanceService
                .getAttendedStudentsForMeeting(meeting.getMeetingId());

        List<String> participatedStudentIds = totalParticipatedStudents.stream()
                .map(Student::getEmail)
                .toList();

        // Filter absent students by comparing IDs
        List<Student> absentStudents = targetStudents.stream()
                .filter(student -> !participatedStudentIds.contains(student.getEmail()))
                .toList();

        return new AttendanceData(targetStudents, presentStudents, partiallyPresentStudents, absentStudents);
    }

    // Calculate meeting duration in minutes
    private long calculateMeetingDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }

    // Map attendance data to AttendanceReportResponseDTO
    private AttendanceReportResponseDTO mapToAttendanceReportDTO(Meeting meeting, AttendanceData attendanceData) {
        Lecturer lecturer = meeting.getCreatedBy();
        String lecturerName = lecturer.getFirstName() + " " + lecturer.getLastName();
        long durationMinutes = calculateMeetingDuration(meeting.getActualStartTime(), meeting.getActualEndTime());

        AttendanceReportResponseDTO reportDTO = new AttendanceReportResponseDTO();
        reportDTO.setMeetingId(meeting.getMeetingId().toString());
        reportDTO.setTopic(meeting.getTitle());
        reportDTO.setLecturerName(lecturerName);
        reportDTO.setDegree(meeting.getTargetDegree());
        reportDTO.setBatch(meeting.getTargetBatch());
        reportDTO.setStartedAt(meeting.getActualStartTime());
        reportDTO.setEndedAt(meeting.getActualEndTime());
        reportDTO.setDurationMinutes((int) durationMinutes);

        reportDTO.setTotalStudents(attendanceData.targetStudents().size());
        reportDTO.setTotalParticipated(
                attendanceData.presentStudents().size() + attendanceData.partiallyPresentStudents().size());
        reportDTO.setPresentCount(attendanceData.presentStudents().size());
        reportDTO.setPartialCount(attendanceData.partiallyPresentStudents().size());
        reportDTO.setAbsentCount(attendanceData.absentStudents().size());

        reportDTO.setPresentStudents(
                attendanceService.mapStudentsToAttendanceDTOs(attendanceData.presentStudents(), meeting));
        reportDTO.setPartiallyAttendedStudents(
                attendanceService.mapStudentsToAttendanceDTOs(attendanceData.partiallyPresentStudents(), meeting));
        reportDTO.setAbsentStudents(
                attendanceService.mapStudentsToAttendanceDTOs(attendanceData.absentStudents(), meeting));

        return reportDTO;
    }

    // GET ALL SCHEDULED/LIVE MEETINGS FOR CURRENT STUDENT'S DEGREE AND BATCH
    @Transactional(readOnly = true)
    public List<MeetingResponseDTO> getStudentMeetings() {
        // 1. Get current student with business logic validation
        Student currentStudent = currentUserProvider.getCurrentUserAs(Student.class);

        // 2. Fetch meetings based on student's Degree and Batch
        List<Meeting> allGroupMeetings = meetingRepository.findAllByTargetDegreeAndTargetBatch(
                currentStudent.getDegree(),
                currentStudent.getBatch());

        return allGroupMeetings.stream()
                // 3. Filter: Only keep SCHEDULED or LIVE meetings
                .filter(meeting -> meeting.getStatus() == MeetingStatus.SCHEDULED ||
                        meeting.getStatus() == MeetingStatus.LIVE)
                // 4. Sort: By scheduled start time (Ascending)
                .sorted(Comparator.comparing(Meeting::getScheduledStartTime))
                // 5. Map: Convert to DTO
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
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

    // Validate that the meeting has ended (for attendance reports)
    private void validateMeetingHasEnded(Meeting meeting) {
        if (meeting.getStatus() != MeetingStatus.ENDED) {
            throw new UnauthorizedException(
                    "Attendance report can only be generated for meetings that have ended.");
        }
    }

    // Validate that the meeting is live
    private void validateMeetingIsLive(Meeting meeting) {
        if (meeting.getStatus() != MeetingStatus.LIVE) {
            throw new UnauthorizedException("This meeting is not live.");
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

        // Get current user's name and role
        Role currentUserRole = currentUserProvider.getCurrentUserRole();
        String userName;
        boolean isHost;

        if (currentUserRole == Role.LECTURER) {
            Lecturer lecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
            userName = lecturer.getFirstName() + " " + lecturer.getLastName();
            isHost = true;
        } else {
            Student student = currentUserProvider.getCurrentUserAs(Student.class);
            userName = student.getFirstName() + " " + student.getLastName();
            isHost = false;
        }

        responseDTO.setUserName(userName);
        responseDTO.setHost(isHost);

        return responseDTO;
    }

    // Get active participants for a meeting (for name sync)
    public List<ParticipantDTO> getActiveParticipants(String meetingId) {
        Meeting meeting = findMeetingById(meetingId);
        List<ParticipantDTO> participants = new java.util.ArrayList<>();

        // Add lecturer (host)
        Lecturer lecturer = meeting.getCreatedBy();
        // Note: Lecturer doesn't have agoraUid stored, but we can identify them as host
        // For the lecturer, we'll use their user ID as a fallback
        ParticipantDTO lecturerParticipant = new ParticipantDTO();
        lecturerParticipant.setAgoraUid((int) lecturer.getId());
        lecturerParticipant.setDisplayName(lecturer.getFirstName() + " " + lecturer.getLastName());
        lecturerParticipant.setHost(true);
        participants.add(lecturerParticipant);

        // Add students from attendance records (they have agoraUid)
        if (meeting.getAttendances() != null) {
            for (Attendance attendance : meeting.getAttendances()) {
                // Only include students who have joined (have agoraUid) and haven't left
                if (attendance.getAgoraUid() != null) {
                    Student student = attendance.getStudent();
                    ParticipantDTO studentParticipant = new ParticipantDTO();
                    studentParticipant.setAgoraUid(attendance.getAgoraUid());
                    studentParticipant.setDisplayName(student.getFirstName() + " " + student.getLastName());
                    studentParticipant.setHost(false);
                    participants.add(studentParticipant);
                }
            }
        }

        return participants;
    }
}
