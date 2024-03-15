package ondicho.co.ke.RentalManangementSystemBackend.models.Payment;


import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RegisterUrlDTO {
    private String ShortCode;
    private String ResponseType;
    private String ConfirmationURL;
    private String ValidationURL;
}
