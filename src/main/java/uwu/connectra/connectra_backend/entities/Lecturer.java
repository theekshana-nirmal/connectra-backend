package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "lecturers")
public class Lecturer extends User{
    //All other common fields are inherit from User class

    @PrePersist  // Set default role as LECTURER before saving to database
    protected void onCreate(){
        if (this.getRole() == null){
            this.setRole(Role.LECTURER);
        }
    }
}
