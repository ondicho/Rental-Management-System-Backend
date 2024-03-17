package ondicho.co.ke.RentalManangementSystemBackend.models.Property;
import jakarta.persistence.*;
import lombok.*;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.MPesaTransaction;

@Entity(name = "payment_account_transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PaymentAccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_account_id")
    private PaymentAccount paymentAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private MPesaTransaction transaction;

}
