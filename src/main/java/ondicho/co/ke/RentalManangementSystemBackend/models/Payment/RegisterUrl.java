package ondicho.co.ke.RentalManangementSystemBackend.models.Payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import ondicho.co.ke.RentalManangementSystemBackend.models.Property.PaymentAccount;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name="register_urls")
public class RegisterUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(unique = true,nullable = false)
    private String shortCode;

    @Column(unique = true,nullable = false)
    private String responseType;

    @Column(unique = true,nullable = false)
    private String confirmationUrl;

    @Column(unique = true,nullable = false)
    private String validationUrl;

    @OneToOne(mappedBy = "registerUrl", cascade = CascadeType.ALL)
    private PaymentAccount paymentAccount;
}
