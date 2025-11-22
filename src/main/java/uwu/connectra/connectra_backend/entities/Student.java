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
    @Column(name = "degree")
    private String degree;

    @Column(name = "batch")
    private int batch;

    @Column(name = "student_email")
    private String student_email;

    @PrePersist // Set default role as STUDENT before saving to database
    protected void onCreate() {
        if (this.getRole() == null) {
            this.setRole(Role.STUDENT);
        }
    }
}