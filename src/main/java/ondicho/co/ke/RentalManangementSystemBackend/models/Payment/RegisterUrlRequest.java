package ondicho.co.ke.RentalManangementSystemBackend.models.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUrlRequest {

    private String ShortCode;
    private String ResponseType;
    private String ConfirmationURL;
    private String ValidationURL;
}
