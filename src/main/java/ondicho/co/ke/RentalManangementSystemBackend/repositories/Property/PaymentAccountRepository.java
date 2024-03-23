package ondicho.co.ke.RentalManangementSystemBackend.repositories.Property;

import ondicho.co.ke.RentalManangementSystemBackend.models.Property.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount,Integer> {

    @Query(value = "SELECT CASE WHEN COUNT(pa) > 0 THEN TRUE ELSE FALSE END FROM payment_accounts pa WHERE pa.shortCode = :shortCode AND (pa.accountNumber = :accountNumber OR :accountNumber IS NULL)", nativeQuery = true)
    Boolean existsByShortCodeAndAccountNumber(@Param("shortCode") String shortCode, @Param("accountNumber") String accountNumber);

    @Query(value = "SELECT CASE WHEN COUNT(pa) > 0 THEN TRUE ELSE FALSE END FROM payment_accounts pa WHERE pa.shortCode = :shortCode AND pa.accountType = :accountType", nativeQuery = true)
    Boolean existsByShortCodeAndAsTill(@Param("shortCode") String shortCode, @Param("accountType") Integer accountType);




}
