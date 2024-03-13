package ondicho.co.ke.RentalManangementSystemBackend.util;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResponseHandler {

    public Map<String,Object> generateResponse(String message, Object data, String errorMessage){
        Map<String,Object> response=new HashMap<>();
        response.put("data",data);
        response.put("message",message);
        if(message.equalsIgnoreCase("success")){
            response.put("code",000);
        }else {
            response.put("code", 001);
            response.put("error",message);
            response.put("message",errorMessage);
        }
        return response;
    }
}
