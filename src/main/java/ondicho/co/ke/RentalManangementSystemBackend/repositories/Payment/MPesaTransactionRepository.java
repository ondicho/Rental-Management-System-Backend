package ondicho.co.ke.RentalManangementSystemBackend.repositories.Payment;

import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.MPesaTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MPesaTransactionRepository extends JpaRepository<MPesaTransaction,Integer> {
    Page<MPesaTransaction> findByBusinessShortCode(String paymentAccount, Pageable pageable);

    Optional<MPesaTransaction> findByTransID(String transactionId);
}
