package ondicho.co.ke.RentalManangementSystemBackend.repositories.Property;

import ondicho.co.ke.RentalManangementSystemBackend.models.Property.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount,Integer> {
}
