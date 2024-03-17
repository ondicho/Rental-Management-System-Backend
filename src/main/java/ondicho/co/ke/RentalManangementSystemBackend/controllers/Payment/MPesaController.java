package ondicho.co.ke.RentalManangementSystemBackend.controllers.Payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.*;
import ondicho.co.ke.RentalManangementSystemBackend.services.Payment.MPesaService;
import ondicho.co.ke.RentalManangementSystemBackend.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/payment")
@Tag(name = "M-Pesa Payments API", description = "Operations related M-Pesa Payments")
public class MPesaController {

    @Autowired
    MPesaService mPesaService;


    @GetMapping(value = "/get-token")
    @Operation(summary = "Get Token")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DarajaAccessToken.class)
            ))
    public DarajaAccessToken getToken() {
        return HttpUtil.authenticate();
    }

    @PostMapping(value = "/register-url")
    @Operation(summary = "Register payment callback urls")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = RegisterUrl.class)
            ))
    public Map<String, Object> registerUrl(@RequestBody Map<String,Object> registerUrlDTO) {
        return mPesaService.registerUrl(registerUrlDTO);
    }

    @PostMapping(value = "/{paymentAccount}/validate")
    @Operation(summary = "M-Pesa Validation Call back")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = MPesaTransaction.class)
            ))
    public MpesaValidationResponse receiveCallBackValidation(@PathVariable String paymentAccount, @RequestBody MPesaTransaction mPesaTransaction) {
        return mPesaService.receivePaymentCallBackValidation(paymentAccount, mPesaTransaction);
    }

    @PostMapping(value = "/{paymentAccount}/confirm")
    @Operation(summary = "M-Pesa Confirmation Call back")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = MPesaTransaction.class)
            ))
    public void receiveCallBackConfirmation(@PathVariable String paymentAccount, @RequestBody MPesaTransaction mPesaTransaction) {
        mPesaService.receivePaymentCallBackConfirmation(paymentAccount, mPesaTransaction);
    }

    @GetMapping(value = "/{paymentAccount}/transactions")
    @Operation(summary = "All Payment Account(Paybill/TillNumber) transactions")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = MPesaTransaction.class)
            ))
    public Map<String, Object> allPaymentAccountTransactions(@PathVariable String paymentAccount,@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortBy) {
        return mPesaService.fetchAllAccountTransactions(paymentAccount,pageNo, pageSize, sortBy);
    }

    @GetMapping(value = "/{paymentAccount}")
    @Operation(summary = "M-Pesa Transaction")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = MPesaTransaction.class)
            ))
    public Map<String, Object> paymentAccountTransaction(@PathVariable String paymentAccount, @RequestParam String transactionId) {
        return mPesaService.fetchAccountTransaction(paymentAccount, transactionId);
    }

}
