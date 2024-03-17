package ondicho.co.ke.RentalManangementSystemBackend.models.Property;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Entity(name = "apartments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @OneToOne(mappedBy = "apartment", cascade = CascadeType.ALL)
    private Tenant tenant;

    @OneToMany(mappedBy = "apartment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ApartmentBill> apartmentBills;

    public long calculateTotalMonthlyPayment() {
        long totalBillsAmount=0;
        for(ApartmentBill apartmentBill:apartmentBills){
            totalBillsAmount=totalBillsAmount+ apartmentBill.getBillAmount();
        }
        return totalBillsAmount;
    }

}
