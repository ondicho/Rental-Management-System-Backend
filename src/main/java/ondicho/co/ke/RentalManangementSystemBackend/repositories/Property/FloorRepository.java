package ondicho.co.ke.RentalManangementSystemBackend.repositories.Property;

import ondicho.co.ke.RentalManangementSystemBackend.models.Property.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FloorRepository  extends JpaRepository<Floor,Integer> {
}
