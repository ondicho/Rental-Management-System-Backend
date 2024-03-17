package ondicho.co.ke.RentalManangementSystemBackend.repositories.Property;

import ondicho.co.ke.RentalManangementSystemBackend.models.Property.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant,Integer> {
}
