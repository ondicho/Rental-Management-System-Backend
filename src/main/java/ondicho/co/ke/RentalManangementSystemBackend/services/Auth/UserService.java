package ondicho.co.ke.RentalManangementSystemBackend.services.Auth;

import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.CreateUserDTO;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.Role;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth.RoleRepository;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth.UserRepository;
import ondicho.co.ke.RentalManangementSystemBackend.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ResponseHandler responseHandler;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    public ResponseEntity<Map<String, Object>> createUser(CreateUserDTO userDTO) {

        try {
            if (userDTO.getPassword1().equalsIgnoreCase(userDTO.getPassword2())) {
                Set<Role> roleSet = new HashSet<>();
                Optional<Role> role = roleRepository.findRoleByName(userDTO.getRole());
                role.ifPresent(roleSet::add);
                var user = User.builder()
                        .firstName(userDTO.getFirstName())
                        .lastName(userDTO.getLastName())
                        .email(userDTO.getEmail())
                        .password(passwordEncoder.encode(userDTO.getPassword1().toLowerCase()))
                        .isEnabled(true)
                        .roles(roleSet)
                        .build();

                User savedUser = userRepository.save(user);
                Map<String, Object> response = responseHandler.generateResponse("success", savedUser, null);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
            Map<String, Object> response = responseHandler.generateResponse("fail", userDTO, "Password 1 does not match password 2");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            LOGGER.error("Error creating user: {}", e.getMessage());
            Map<String, Object> errorResponse = responseHandler.generateResponse("fail", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public Map<String, Object> getUser(int id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                return responseHandler.generateResponse("success", user, null);
            }
        } catch (Exception e) {
            LOGGER.error("get user stacktrace : " + e);
        }
        return responseHandler.generateResponse("fail", null, "User does not exist");
    }

    public Map<String, Object> fetchAllUsers(int pageNo, int pageSize, String sortBy) {
        try {

            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
            Page<User> transactions = userRepository.findAll(pageable);

            return responseHandler.generateResponse("success", transactions, null);
        } catch (Exception e) {
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }
}
