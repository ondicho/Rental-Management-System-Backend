package ondicho.co.ke.RentalManangementSystemBackend.controllers.Auth;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.*;
import ondicho.co.ke.RentalManangementSystemBackend.services.Auth.UserGroupsService;
import ondicho.co.ke.RentalManangementSystemBackend.services.Auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/auth-admin")
@Tag(name = "Authentication Admin API", description = "Operations related User Administration")
public class AuthAdminController {

    @Autowired
    UserService userService;

    @Autowired
    UserGroupsService userGroupsService;

    @PostMapping(value = "/user")
    @Operation(summary = "Create User")
    @ApiResponse(responseCode = "201", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)
            ))
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserDTO user) {
        return userService.createUser(user);
    }


    @GetMapping(value = "/user/{id}")
    @Operation(summary = "Fetch user by ID")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)
            ))
    public Map<String, Object> fetchTransaction(@PathVariable int id) {
        return userService.getUser(id);
    }


    @GetMapping(value = "/users")
    @Operation(summary = "Fetch all users")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)
            ))
    public Map<String, Object> fetchAll(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortBy) {
        return userService.fetchAllUsers(pageNo, pageSize, sortBy);
    }

    @PostMapping(value = "/role")
    @Operation(summary = "Create Role")
    @ApiResponse(responseCode = "201", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Role.class)
            ))
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody RoleDTO role) {
        return userGroupsService.createRole(role);
    }

    @PostMapping(value = "/group")
    @Operation(summary = "Create Group")
    @ApiResponse(responseCode = "201", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Group.class)
            ))
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody Group group) {
        return userGroupsService.createGroup(group);
    }

    @GetMapping(value = "group/{id}")
    @Operation(summary = "Fetch Group by ID")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Group.class)
            ))
    public Map<String, Object> fetchGroup(@PathVariable int id) {
        return userGroupsService.getGroup(id);
    }

    @GetMapping(value = "/group/{id}/users")
    @Operation(summary = "Fetch all groups")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)
            ))
    public Map<String, Object> fetchAllGroupUsers(@PathVariable int id,@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortBy) {
        return userGroupsService.fetchGroupUsers(id,pageNo, pageSize, sortBy);
    }

    @GetMapping(value = "/groups")
    @Operation(summary = "Fetch all group users")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)
            ))
    public Map<String, Object> fetchAllGroup(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortBy) {
        return userGroupsService.fetchAllGroups(pageNo, pageSize, sortBy);
    }

}
