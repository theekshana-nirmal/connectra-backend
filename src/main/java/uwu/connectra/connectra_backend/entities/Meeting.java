package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID meetingId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "scheduled_start_time", nullable = false)
    private LocalDateTime scheduledStartTime;

    @Column(name = "scheduled_end_time", nullable = false)
    private LocalDateTime scheduledEndTime;

    @Column(name = "acctual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "acctual_end_time")
    private LocalDateTime actualEndTime;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "target_degree")
    private String targetDegree;

    @Column(name = "target_batch")
    private Integer targetBatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MeetingStatus status;

    @Column(name = "agora_channel_name", nullable = false, unique = true)
    private String agoraChannelName;

    // TODO: Add fields for meeting recordings related features (When implementing that feature)
//    @Column(name = "recording_resource_id", unique = true)
//    private String recordingResourceId; // This is use to identify the recording resource
//
//    @Column(name = "recording_sid", unique = true)
//    private String recordingSid; // This is the actual recording ID provided by Agora

//    @Column(name = "recording_storage_path")
//    private String recordingStoragePath;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer createdBy;
}
