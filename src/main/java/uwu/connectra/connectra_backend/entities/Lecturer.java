package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "lecturers")
@DiscriminatorValue("LECTURER")
public class Lecturer extends User{
    @Column(name = "department")
    private String department;
}
