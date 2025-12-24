package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Entity
@Getter
@Setter
@DiscriminatorValue("LECTURER")
public class Lecturer extends User {
    // Relationships
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Meeting> meetings = new ArrayList<>();
}
