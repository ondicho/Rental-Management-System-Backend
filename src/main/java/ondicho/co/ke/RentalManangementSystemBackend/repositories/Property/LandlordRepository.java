package ondicho.co.ke.RentalManangementSystemBackend.repositories.Property;

import ondicho.co.ke.RentalManangementSystemBackend.models.Property.Landlord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandlordRepository extends JpaRepository<Landlord,Integer> {
}
