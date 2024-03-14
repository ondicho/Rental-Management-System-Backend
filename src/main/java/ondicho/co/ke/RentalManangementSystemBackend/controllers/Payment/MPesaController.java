package ondicho.co.ke.RentalManangementSystemBackend.controllers.Payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.Group;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.DarajaAccessToken;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.MPesaTransaction;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.RegisterUrlRequest;
import ondicho.co.ke.RentalManangementSystemBackend.services.Payment.MPesaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/mpesa")
@Tag(name = "M-Pesa Payments API", description = "Operations related M-Pesa Payments")
public class MPesaController {

    @Autowired
    MPesaService mPesaService;



    @GetMapping(value = "/register-url")
    @Operation(summary = "Register payment callback urls")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DarajaAccessToken.class)
            ))
    public Map<String, Object> registerUrl(@RequestBody RegisterUrlRequest registerUrlRequest) {
        return mPesaService.registerUrl(registerUrlRequest);
    }

    @PostMapping(value = "/group")
    @Operation(summary = "Mpesa Call back")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Group.class)
            ))
    public Map<String, Object> createGroup(@RequestBody MPesaTransaction mPesaTransaction) {
        return mPesaService.receivePaymentCallBack(mPesaTransaction);
    }

}
