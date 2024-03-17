package ondicho.co.ke.RentalManangementSystemBackend.models.Property;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity(name = "tenants")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String idNumber;

    @Column(nullable = false,unique = true)
    private String phoneNumber;


    @ElementCollection
    @CollectionTable(name = "tenant_phone_numbers", joinColumns = @JoinColumn(name = "tenant_id"))
    @Column(name = "phone_number")
    private List<String> otherPhoneNumbers;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "apartment_id", referencedColumnName = "id")
    private Apartment apartment;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

}
