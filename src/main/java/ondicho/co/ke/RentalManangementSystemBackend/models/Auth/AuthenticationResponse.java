package ondicho.co.ke.RentalManangementSystemBackend.models.Auth;

import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String access_token;

    private String email;

    private String name;

    private List<String> role;

    private List<String> group;
}
