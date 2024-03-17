package ondicho.co.ke.RentalManangementSystemBackend.models.Property;

import jakarta.persistence.*;
import lombok.*;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.RegisterUrl;

@Entity(name = "payment_accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PaymentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(nullable = false)
    private String shortCode;

    @Column(nullable = false)
    private PaymentAccountType accountType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "register_url_id", referencedColumnName = "id")
    private RegisterUrl registerUrl;
}