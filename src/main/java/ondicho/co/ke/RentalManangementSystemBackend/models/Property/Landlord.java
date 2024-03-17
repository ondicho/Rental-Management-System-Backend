package ondicho.co.ke.RentalManangementSystemBackend.models.Property;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;

import java.util.Set;

@Entity(name = "landlords")
public class Landlord extends User {

    @OneToMany(mappedBy = "landlord", fetch = FetchType.LAZY)
    private Set<Property> properties;
}
