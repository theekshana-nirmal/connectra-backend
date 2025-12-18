package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {
    //All other common fields are inherit from User class
    @Column(name = "degree")
    private String degree;

    @Column(name = "batch")
    private int batch;

    @Column(name = "student_id", unique = true)
    private String studentId; // This is the enrollment number of the student (e.g., UWU/ICT/22/082)
}