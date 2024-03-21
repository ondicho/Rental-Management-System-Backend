package ondicho.co.ke.RentalManangementSystemBackend.controllers.Property;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.CreateUserDTO;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.services.Property.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/property")
@Tag(name = "Property  API", description = "Operations related Property Administration")
public class PropertyController {

    @Autowired
    PropertyService propertyService;

    @PostMapping
    @Operation(summary = "Create property")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)
            ))
    public Map<String, Object> createUser(@RequestBody MultipartFile file, HttpServletRequest request) {
        return propertyService.processExcel(file,request);
    }
}

