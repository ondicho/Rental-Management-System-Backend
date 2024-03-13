package ondicho.co.ke.RentalManangementSystemBackend.controllers.Auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.AuthenticationRequest;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.AuthenticationResponse;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.CreateUserDTO;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.services.Auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/auth")
@Tag(name = "Authorization API", description = "Operations related User Authorization")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping(value = "/login")
    @Operation(summary = "Login")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthenticationResponse.class)
            ))
    public  ResponseEntity<AuthenticationResponse> createUser(@RequestBody AuthenticationRequest authenticationRequest) {
        return authService.authenticate(authenticationRequest);
    }


    @GetMapping(value = "/logout")
    @Operation(summary = "Logout")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Map.class)
            ))
    public ResponseEntity<?>  fetchTransaction(@RequestHeader String Authorization) {
        return authService.logout(Authorization);
    }
}
