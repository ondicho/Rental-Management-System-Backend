package ondicho.co.ke.RentalManangementSystemBackend.models.Property;

import jakarta.persistence.*;
import lombok.*;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;

import java.util.Set;

@Entity(name = "landlords")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Landlord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User userAccount;

    @OneToMany(mappedBy = "landlord", fetch = FetchType.LAZY)
    private Set<Property> properties;
}
