package ondicho.co.ke.RentalManangementSystemBackend.services.Auth;

import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.AuthenticationRequest;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.AuthenticationResponse;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.BlackList;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth.BlacklistRepository;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth.UserRepository;
import ondicho.co.ke.RentalManangementSystemBackend.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    ResponseHandler responseHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BlacklistRepository blacklistRepository;

    @Autowired
    JwtService jwtService;


    public AuthService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(), authenticationRequest.getPassword()
                    )
            );
            Optional<User> userOptional = userRepository.findByEmail(authenticationRequest.getEmail());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String jwtToken = jwtService.generateToken(user);
                AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                        .access_token(jwtToken)
                        .email(user.getEmail())
                        .name(user.getFirstName() + " " + user.getLastName())
                        .role(user.getUserRoles())
                        .group(user.getGroups())
                        .build();
                return ResponseEntity.status(HttpStatus.OK).body(authenticationResponse);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthenticationResponse());

        } catch (Exception e) {
            LOGGER.error("get user stacktrace : " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthenticationResponse());
        }
    }

    public ResponseEntity<?> logout(String Authorization) {

        try {
            if (Authorization.startsWith("Bearer ")) {
                String token = Authorization.substring(7);
                Optional<User> user = userRepository.findByEmail(jwtService.extractUsername(token));
                if (user.isPresent()) {
                    if (jwtService.isTokenValid(token, user.get())) {
                        BlackList blackListEntry = BlackList.builder()
                                .token(Authorization)
                                .build();
                        blacklistRepository.save(blackListEntry);
                    }
                    BlackList blackListEntry = BlackList.builder()
                            .token(token)
                            .build();
                    blacklistRepository.save(blackListEntry);

                    return ResponseEntity.status(HttpStatus.OK).body("Logged Out");
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalidtoken");
        } catch (Exception e) {
            LOGGER.error("get user stacktrace : " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
