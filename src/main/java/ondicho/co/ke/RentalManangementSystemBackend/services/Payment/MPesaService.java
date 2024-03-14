package ondicho.co.ke.RentalManangementSystemBackend.services.Payment;


import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.DarajaAccessToken;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.MPesaTransaction;
import ondicho.co.ke.RentalManangementSystemBackend.models.Payment.RegisterUrlRequest;
import ondicho.co.ke.RentalManangementSystemBackend.util.HttpUtil;
import ondicho.co.ke.RentalManangementSystemBackend.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MPesaService {

    private  static String registerUrlEndpoint = null;;

    public MPesaService(
            @Value("${daraja.sandbox.registerUrl}") String registerUrlEndpoint
    ){
        this.registerUrlEndpoint = registerUrlEndpoint;
    }

    @Autowired
    ResponseHandler responseHandler;
    private static final Logger LOGGER = LoggerFactory.getLogger(MPesaService.class);
    public Map<String,Object> receivePaymentCallBack(MPesaTransaction mPesaTransaction){
        try{
            DarajaAccessToken darajaAccessToken= HttpUtil.authenticate();
            return responseHandler.generateResponse("success",darajaAccessToken,null);
        }catch(Exception e){
            LOGGER.error("Receive M-Pesa callback error : {}, {}",e.getMessage(),e);
            return responseHandler.generateResponse("fail",null,e.getMessage());
        }
    }

    public Map<String, Object> registerUrl(RegisterUrlRequest registerUrlRequest) {
        try{
            Map<String, Object> darajaResponse= HttpUtil.darajaRequest(registerUrlEndpoint,registerUrlRequest);
            if(darajaResponse.get("ResponseDescription").toString().equalsIgnoreCase("success")){
                return responseHandler.generateResponse("success",null,darajaResponse.toString());
            }
            return responseHandler.generateResponse("fail",null,darajaResponse.get("ResponseDescription").toString());
        }catch(Exception e){
            LOGGER.error("Register Url error : {}, {}",e.getMessage(),e);
            return responseHandler.generateResponse("fail",null,e.getMessage());
        }
    }
}
