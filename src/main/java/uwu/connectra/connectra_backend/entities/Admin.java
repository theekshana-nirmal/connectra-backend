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
@DiscriminatorValue("ADMIN")
public class Admin extends User{

}