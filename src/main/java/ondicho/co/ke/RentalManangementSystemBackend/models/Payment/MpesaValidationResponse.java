package ondicho.co.ke.RentalManangementSystemBackend.models.Payment;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MpesaValidationResponse {

    private String ResultCode;
    private String ResultDesc;
}
