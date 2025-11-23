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
    @PrePersist
    protected void onCreate(){
        if(this.getRole() == null){
            this.setRole(Role.ADMIN);
        }
    }

}
