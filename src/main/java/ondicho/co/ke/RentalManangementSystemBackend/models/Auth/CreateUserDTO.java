package ondicho.co.ke.RentalManangementSystemBackend.models.Auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDTO {

    private String firstName;

    private String lastName;

    private String email;

    private String password1;

    private String password2;

    private String role;

    private String group;

    private Boolean isEnabled;

}
