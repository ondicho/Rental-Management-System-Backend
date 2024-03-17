package ondicho.co.ke.RentalManangementSystemBackend.models.Property;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "apartment_bills")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ApartmentBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApartmentBillType billType;

    @Column(nullable = false)
    private long billAmount;


    @Column(nullable = false)
    private LocalDate billDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "apartment_id", referencedColumnName = "id")
    private Apartment apartment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApartmentBillStatus billStatus;

    @Column
    private LocalDate paidOn;

    @Column
    private long paidAmount;

    @Column
    private Boolean fixed;


}
