package uwu.connectra.connectra_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uwu.connectra.connectra_backend.dtos.StudentAttendanceDTO;
import uwu.connectra.connectra_backend.dtos.StudentAttendanceHistoryResponseDTO;
import uwu.connectra.connectra_backend.entities.*;
import uwu.connectra.connectra_backend.exceptions.UnauthorizedException;
import uwu.connectra.connectra_backend.repositories.AttendanceRepository;
import uwu.connectra.connectra_backend.repositories.MeetingRepository;
import uwu.connectra.connectra_backend.utils.CurrentUserProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final CurrentUserProvider currentUserProvider;
    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;

    /**
     * Record student attendance when joining a meeting.
     * If duplicate error occurs (concurrent requests), just return success since
     * attendance IS recorded by the other request.
     * noRollbackFor prevents Spring from rolling back when duplicate key occurs.
     */
    @Transactional(noRollbackFor = org.springframework.dao.DataIntegrityViolationException.class)
    public void recordStudentAttendanceOnJoin(Meeting meeting) {
        Student currentStudent = currentUserProvider.getCurrentUserAs(Student.class);
        LocalDateTime now = LocalDateTime.now();

        // Check if attendance already exists
        Attendance attendance = attendanceRepository.findByStudentAndMeeting(currentStudent, meeting)
                .orElse(null);

        if (attendance == null) {
            // Create new attendance record
            try {
                attendance = new Attendance();
                attendance.setStudent(currentStudent);
                attendance.setMeeting(meeting);
                attendance.setJoinedAt(now);
                attendance.setLastJoinedAt(now);
                attendanceRepository.save(attendance); // Don't use saveAndFlush - it corrupts session on error
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Duplicate key - another request already created it
                // Just return - attendance is recorded successfully (by other request)
                return;
            }
            return;
        }

        // Update existing record (if user rejoins)
        autoLeaveStudentIfStillInMeeting(now, attendance);
        attendance.setLastJoinedAt(now);
        attendanceRepository.save(attendance);
    }

    // Update student attendance on leave and calculate total duration
    @Transactional
    public void recordStudentAttendanceOnLeave(Meeting meeting) {
        Student currentStudent = currentUserProvider.getCurrentUserAs(Student.class);

        // Update attendance
        Attendance attendance = attendanceRepository.findByStudentAndMeeting(currentStudent, meeting).orElseThrow(
                () -> new UnauthorizedException("Attendance record not found for student in this meeting."));

        // Validate that the student has actually joined the meeting
        if (attendance.getLastJoinedAt() == null) {
            throw new UnauthorizedException("You have not joined the meeting yet.");
        }

        // Check if user has already left after their last join - if so, just return
        // (idempotent)
        if (attendance.getLeftAt() != null &&
                (attendance.getLeftAt().isAfter(attendance.getLastJoinedAt())
                        || attendance.getLeftAt().isEqual(attendance.getLastJoinedAt()))) {
            // Already left - this is OK, just return success (meeting may have been ended
            // by lecturer)
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setLeftAt(now);

        long duration = java.time.Duration.between(attendance.getLastJoinedAt(), now).toMinutes();
        attendance.setTotalDurationInMinutes(attendance.getTotalDurationInMinutes() + duration);

        // Calculate and update attendance percentage and status
        updateAttendancePercentageAndStatus(attendance, meeting);

        attendanceRepository.save(attendance);
    }

    // Update all attendance records when a meeting ends
    public void finalizeAttendanceForMeeting(Meeting meeting) {
        // Get all attendance records for this meeting
        if (meeting.getAttendances() != null && !meeting.getAttendances().isEmpty()) {
            LocalDateTime meetingEndTime = LocalDateTime.now();

            for (Attendance attendance : meeting.getAttendances()) {
                autoLeaveStudentIfStillInMeeting(meetingEndTime, attendance);

                // Calculate final attendance percentage and status
                updateAttendancePercentageAndStatus(attendance, meeting);
                attendanceRepository.save(attendance);
            }
        }
    }

    // Auto-leave student if they are still marked as in the meeting
    private void autoLeaveStudentIfStillInMeeting(LocalDateTime meetingEndTime, Attendance attendance) {
        if (attendance.getLastJoinedAt() != null &&
                (attendance.getLeftAt() == null ||
                        attendance.getLeftAt().isBefore(attendance.getLastJoinedAt()))) {
            attendance.setLeftAt(meetingEndTime);
            long duration = Duration.between(attendance.getLastJoinedAt(), meetingEndTime)
                    .toMinutes();
            attendance.setTotalDurationInMinutes(attendance.getTotalDurationInMinutes() + duration);
        }
    }

    // Update attendance percentage and status based on total duration vs duration
    private void updateAttendancePercentageAndStatus(Attendance attendance, Meeting meeting) {
        LocalDateTime meetingStart = meeting.getActualStartTime() != null
                ? meeting.getActualStartTime()
                : meeting.getScheduledStartTime();

        LocalDateTime meetingEnd = meeting.getActualEndTime() != null
                ? meeting.getActualEndTime()
                : LocalDateTime.now(); // If meeting is still live, use current time

        // Calculate total meeting duration in minutes
        long totalMeetingDuration = Duration.between(meetingStart, meetingEnd).toMinutes();

        // Avoid division by zero
        if (totalMeetingDuration <= 0) {
            attendance.setAttendancePercentage(0.0);
            attendance.setAttendanceStatus(AttendanceStatus.ABSENT);
            return;
        }

        // Calculate attendance percentage
        double percentage = (attendance.getTotalDurationInMinutes() * 100.0) / totalMeetingDuration;

        // Cap at 100% (in case of calculation edge cases)
        percentage = Math.min(percentage, 100.0);
        percentage = BigDecimal.valueOf(percentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        attendance.setAttendancePercentage(percentage);

        // Determine attendance status based on percentage
        if (percentage >= 80.0) {
            attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
        } else if (percentage > 0.0) {
            attendance.setAttendanceStatus(AttendanceStatus.PARTIALLY_PRESENT);
        } else {
            attendance.setAttendanceStatus(AttendanceStatus.ABSENT);
        }
    }

    // In your service class (e.g., AttendanceService or MeetingService)
    public List<Student> getAttendedStudentsForMeeting(UUID meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));

        List<Attendance> attendances = attendanceRepository.findAllByMeeting(meeting);

        // Extract students from attendance records
        return attendances.stream()
                .map(Attendance::getStudent)
                .collect(Collectors.toList());
    }

    // Get students by attendance status for a specific meeting
    public List<Student> getAttendanceStatusForStudentInMeeting(UUID meetingId, AttendanceStatus status) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));

        List<Attendance> attendances = attendanceRepository.findAllByMeetingAndAttendanceStatus(meeting, status);

        return attendances.stream()
                .map(Attendance::getStudent)
                .collect(Collectors.toList());
    }

    // Map students to StudentAttendanceDTOs for a specific meeting
    public List<StudentAttendanceDTO> mapStudentsToAttendanceDTOs(List<Student> students, Meeting meeting) {
        return students.stream().map(student -> {
            Attendance attendance = attendanceRepository.findByStudentAndMeeting(student, meeting)
                    .orElse(null);

            long durationMinutes = (attendance != null) ? attendance.getTotalDurationInMinutes() : 0;
            String studentName = student.getFirstName() + " " + student.getLastName();

            return new StudentAttendanceDTO(
                    student.getStudentId(),
                    studentName,
                    (attendance != null) ? attendance.getAttendanceStatus() : AttendanceStatus.ABSENT,
                    durationMinutes);
        }).collect(Collectors.toList());
    }

    // Get complete attendance history for the current student
    public List<StudentAttendanceHistoryResponseDTO> getStudentAttendanceHistory(
            AttendanceStatus statusFilter) {

        // 1. Get current authenticated student
        Student currentStudent = currentUserProvider.getCurrentUserAs(Student.class);

        // 2. Find all ENDED/COMPLETED meetings for student's degree and batch
        List<Meeting> completedMeetings = meetingRepository.findAllByTargetDegreeAndTargetBatchAndStatus(
                currentStudent.getDegree(),
                currentStudent.getBatch(),
                MeetingStatus.ENDED);

        // 3. For each meeting, create a DTO with attendance data
        List<StudentAttendanceHistoryResponseDTO> historyList = completedMeetings
                .stream()
                .map(meeting -> {
                    // Look up attendance record
                    Attendance attendance = attendanceRepository
                            .findByStudentAndMeeting(currentStudent, meeting)
                            .orElse(null);

                    // Calculate meeting duration
                    long meetingDuration = 0;
                    if (meeting.getActualStartTime() != null && meeting.getActualEndTime() != null) {
                        meetingDuration = Duration.between(
                                meeting.getActualStartTime(),
                                meeting.getActualEndTime()).toMinutes();
                    }

                    // Create DTO
                    StudentAttendanceHistoryResponseDTO dto = new StudentAttendanceHistoryResponseDTO();

                    dto.setMeetingId(meeting.getMeetingId());
                    dto.setMeetingTitle(meeting.getTitle());
                    dto.setMeetingDate(meeting.getScheduledStartTime());

                    // Set lecturer name
                    Lecturer lecturer = meeting.getCreatedBy();
                    dto.setLecturerName(lecturer.getFirstName() + " " + lecturer.getLastName());

                    dto.setMeetingDuration(meetingDuration);

                    // If attendance record exists, populate with actual data
                    if (attendance != null) {
                        dto.setJoinedAt(attendance.getJoinedAt());
                        dto.setLeftAt(attendance.getLeftAt());
                        dto.setTotalTimeInMinutes(attendance.getTotalDurationInMinutes());
                        dto.setAttendancePercentage(attendance.getAttendancePercentage());
                        dto.setAttendanceStatus(attendance.getAttendanceStatus().name());
                    } else {
                        // Student was absent - set default values
                        dto.setJoinedAt(null);
                        dto.setLeftAt(null);
                        dto.setTotalTimeInMinutes(0L);
                        dto.setAttendancePercentage(0.0);
                        dto.setAttendanceStatus(AttendanceStatus.ABSENT.name());
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // 4. Apply status filter if provided
        if (statusFilter != null) {
            historyList = historyList.stream()
                    .filter(dto -> dto.getAttendanceStatus().equals(statusFilter.name()))
                    .collect(Collectors.toList());
        }

        // 5. Sort by meeting date descending (newest first)
        historyList.sort((dto1, dto2) -> dto2.getMeetingDate().compareTo(dto1.getMeetingDate()));

        return historyList;
    }
}
