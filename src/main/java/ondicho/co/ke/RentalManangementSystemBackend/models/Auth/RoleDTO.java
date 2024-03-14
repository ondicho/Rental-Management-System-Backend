package ondicho.co.ke.RentalManangementSystemBackend.models.Auth;

import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {

    private String name;
    private String description;
    private String group;
}
