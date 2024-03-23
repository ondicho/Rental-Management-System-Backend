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

    @Column
    private String accountNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private PaymentAccountType accountType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApartmentBillType billType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "register_url_id", referencedColumnName = "id")
    private RegisterUrl registerUrl;
}
