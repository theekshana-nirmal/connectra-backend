package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Table(name = "students")
@Getter
@Setter
@Entity
public class Student extends User {

    @Column(name = "first_name")
    private String first_name;

    @Column(name = "last_name")
    private String last_name;

    @Column(name = "degree")
    private String degree;

    @Column(name = "batch")
    private int batch;

    @Column(name = "student_email")
    private String student_email;

    @Column(name = "hashed_password")
    private String hashed_password;

    @Column(name = "profile_photo_url")
    private String profile_photo_url = "https://placehold.co/100x100"; // Default URL

    @PrePersist // Set default role as STUDENT before saving to database
    protected void onCreate() {
        if (this.getRole() == null) {
            this.setRole(Role.STUDENT);
        }
    }
}