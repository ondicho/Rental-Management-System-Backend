package ondicho.co.ke.RentalManangementSystemBackend.models.Property;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity(name = "blocks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(nullable = false,unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @OneToMany(mappedBy = "block", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Floor> floors;
}
