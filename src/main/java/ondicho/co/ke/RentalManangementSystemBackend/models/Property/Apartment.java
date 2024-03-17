package ondicho.co.ke.RentalManangementSystemBackend.models.Property;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Column(nullable = false)
    private ApartmentOccupancy occupancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @OneToOne(mappedBy = "apartment", cascade = CascadeType.ALL)
    private Tenant tenant;

    @OneToMany(mappedBy = "apartment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ApartmentBill> apartmentBills;

    public long calculateTotalMonthlyPayment(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        long totalBillsAmount = apartmentBills.stream()
                .filter(bill -> bill.getBillDate().isAfter(startOfMonth.minusDays(1)) &&
                        bill.getBillDate().isBefore(endOfMonth.plusDays(1)))
                .mapToLong(ApartmentBill::getBillAmount)
                .sum();

        // Assuming rent is a long and is fixed for the month
        // If rent varies, you might need to adjust this part accordingly
        return totalBillsAmount;
    }


    public long calculatePaidAmountPerMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        return apartmentBills.stream()
                .filter(bill -> bill.getBillStatus() == ApartmentBillStatus.paid &&
                        bill.getPaidOn().isAfter(startOfMonth.minusDays(1)) &&
                        bill.getPaidOn().isBefore(endOfMonth.plusDays(1)))
                .mapToLong(ApartmentBill::getPaidAmount)
                .sum();
    }


}
