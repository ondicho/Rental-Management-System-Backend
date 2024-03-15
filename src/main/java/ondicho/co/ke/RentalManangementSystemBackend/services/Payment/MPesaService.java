package ondicho.co.ke.RentalManangementSystemBackend.services.Payment;


import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.MPesaTransaction;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.MpesaValidationResponse;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.RegisterUrl;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.RegisterUrlDTO;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Payment.MPesaTransactionRepository;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Payment.RegisterUrlRepository;
import ondicho.co.ke.RentalManangementSystemBackend.util.HttpUtil;
import ondicho.co.ke.RentalManangementSystemBackend.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class MPesaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MPesaService.class);
    private static String registerUrlEndpoint = null;

    public MPesaService(
            @Value("${daraja.sandbox.registerUrl}") String registerUrlEndpoint
    ) {
        this.registerUrlEndpoint = registerUrlEndpoint;
    }

    @Autowired
    ResponseHandler responseHandler;

    @Autowired
    RegisterUrlRepository registerUrlRepository;

    @Autowired
    MPesaTransactionRepository mPesaTransactionRepository;

    public MpesaValidationResponse receivePaymentCallBackValidation(String paymentAccount, MPesaTransaction mPesaTransaction) {
        try {
            LOGGER.info("Received Validation callback for payment account : {}", paymentAccount);
            Optional<RegisterUrl> registerUrl = registerUrlRepository.findByShortCode(paymentAccount);
            if (registerUrl.isPresent()) {
                LOGGER.info("Validation: Valid payment account");
                return MpesaValidationResponse.builder()
                        .ResultCode("0")
                        .ResultDesc("Accepted")
                        .build();
            }
            LOGGER.info("Validation: Invalid payment account ");

        } catch (Exception e) {
            LOGGER.error("Receive M-Pesa callback error : {}, {}", e.getMessage(), e);
        }
        return MpesaValidationResponse.builder()
                .ResultCode("0")
                .ResultDesc("Accepted")
                .build();
    }

    public void receivePaymentCallBackConfirmation(String paymentAccount, MPesaTransaction mPesaTransaction) {
        try {
            LOGGER.info("Received Confirmation callback for payment account : {}", paymentAccount);
            Optional<RegisterUrl> registerUrl = registerUrlRepository.findByShortCode(paymentAccount);
            if (registerUrl.isPresent()) {
                LOGGER.info("Confirmation: Valid payment account");
                mPesaTransactionRepository.save(mPesaTransaction);
                LOGGER.info("Saved M-PesaTransaction : {}", mPesaTransaction);
            }
            LOGGER.info("Confirmation: Invalid payment account ");

        } catch (Exception e) {
            LOGGER.error("Receive M-Pesa callback error : {}, {}", e.getMessage(), e);
        }
    }

    public Map<String, Object> registerUrl(RegisterUrlDTO registerUrlDTO) {
        try {
            LOGGER.info("DTO: "+registerUrlDTO.getResponseType());
            Map<String, Object> darajaResponse = HttpUtil.darajaRequest(registerUrlEndpoint, registerUrlDTO);
            if (darajaResponse!=null&&darajaResponse.get("ResponseDescription").toString().equalsIgnoreCase("success")) {
                RegisterUrl registerUrl= RegisterUrl.builder()
                        .shortCode(registerUrlDTO.getShortCode())
                        .validationUrl(registerUrlDTO.getValidationURL())
                        .confirmationUrl(registerUrlDTO.getConfirmationURL())
                        .responseType(registerUrlDTO.getResponseType())
                        .build();
                registerUrlRepository.save(registerUrl);
                LOGGER.info("Successful registration");
                return responseHandler.generateResponse("success", registerUrl, null);
            }
            return responseHandler.generateResponse("fail", null, "Internal Server Error");
        } catch (Exception e) {
            LOGGER.error("Register Url error : {}, {}", e.getMessage(), e);
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }

    public Map<String, Object> fetchAllAccountTransactions(String paymentAccount, int pageNo, int pageSize, String sortBy) {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
            Page<MPesaTransaction> transactions = mPesaTransactionRepository.findByBusinessShortCode(paymentAccount, pageable);

            return responseHandler.generateResponse("success", transactions, null);
        } catch (Exception e) {
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }

    public Map<String, Object> fetchAccountTransaction(String paymentAccount, String transactionId) {
        try {
            Optional<MPesaTransaction> transactionOptional = mPesaTransactionRepository.findByTransID(transactionId);
            if (transactionOptional.isPresent()) {
                if(transactionOptional.get().getBusinessShortCode()==paymentAccount){
                    return responseHandler.generateResponse("success", transactionOptional.get(), null);
                }
                return responseHandler.generateResponse("fail", null, "Transaction id : "+transactionId+" not found for account : "+paymentAccount);
            }
            return responseHandler.generateResponse("fail", null, "Transaction id : "+transactionId+" not found");

        } catch (Exception e) {
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }

}
