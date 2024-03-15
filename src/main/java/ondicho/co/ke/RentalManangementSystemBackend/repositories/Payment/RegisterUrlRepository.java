package ondicho.co.ke.RentalManangementSystemBackend.repositories.Payment;

import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.RegisterUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisterUrlRepository extends JpaRepository<RegisterUrl,Integer> {
//   @Query(value="",nativeQuery = true)
    Optional<RegisterUrl> findByShortCode(String paymentAccount);
}
