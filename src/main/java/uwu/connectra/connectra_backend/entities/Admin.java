package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "admins")
public class Admin extends User{
    // NOTE: This sets the default role to ADMIN before saving,
    // But assigning roles in the service layer is the recommended best practice.
    // In this specific case, it is acceptable because the role for Admin is fixed and will never change dynamically.
    // Same thing applied to Lecturers and Students
    @PrePersist
    protected void onCreate(){
        if(this.getRole() == null){
            this.setRole(Role.ADMIN);
        }
    }

}
