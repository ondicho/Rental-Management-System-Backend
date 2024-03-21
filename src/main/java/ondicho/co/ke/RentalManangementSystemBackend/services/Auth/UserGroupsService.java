package ondicho.co.ke.RentalManangementSystemBackend.services.Auth;


import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.Group;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.Role;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.RoleDTO;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth.GroupRepository;
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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserGroupsService {


    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupsService.class);

    @Autowired
    ResponseHandler responseHandler;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;


    public ResponseEntity<Map<String, Object>> createGroup(Group group) {
        try {
            Group savedGroup = groupRepository.save(group);
            Map<String, Object> response = responseHandler.generateResponse("success", savedGroup, null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            LOGGER.error("Error creating user: {}", e.getMessage());
            Map<String, Object> errorResponse = responseHandler.generateResponse("fail", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public ResponseEntity<Map<String, Object>> createRole(RoleDTO roleDTO) {
        try {
            Optional<Group> group = groupRepository.findbyName(roleDTO.getGroup());
            if (group.isPresent()) {
                Role role = Role.builder()
                        .group(group.get())
                        .name(roleDTO.getName())
                        .description(roleDTO.getDescription())
                        .build();

                roleRepository.save(role);
                Map<String, Object> response = responseHandler.generateResponse("success", role, null);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            Map<String, Object> response = responseHandler.generateResponse("fail", null, "Group :" + roleDTO.getGroup() + " doesnot exist");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            LOGGER.error("Error creating user: {}", e.getMessage());
            Map<String, Object> errorResponse = responseHandler.generateResponse("fail", null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public Map<String, Object> getGroup(int id) {
        try {
            Optional<Group> group = groupRepository.findById(id);
            if (group.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("group", group.get());
                List<Role> roles=roleRepository.findRolesByGroupId(group.get().getId());
                for (Role role : roles) {
                    Map<String, Object> roleDTO = new HashMap<>();
                    roleDTO.put("name",role.getName());
                    roleDTO.put("description",role.getDescription());
                    response.put("roles",roleDTO);
                }
                return responseHandler.generateResponse("success", response, null);
            }
        } catch (Exception e) {
            LOGGER.error("get user stacktrace : " + e);
        }
        return responseHandler.generateResponse("fail", null, "User does not exist");
    }

    public Map<String, Object> fetchAllGroups(int pageNo, int pageSize, String sortBy) {
        try {

            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
            Page<Group> groups = groupRepository.findAll(pageable);

            return responseHandler.generateResponse("success", groups, null);
        } catch (Exception e) {
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }

    public Map<String, Object> fetchGroupUsers(int groupId,int pageNo, int pageSize, String sortBy) {
        try {

            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
            Page<User> groupUsers = userRepository.findAllGroupUsers(groupId,pageable);

            return responseHandler.generateResponse("success", groupUsers, null);
        } catch (Exception e) {
            return responseHandler.generateResponse("fail", null, e.getMessage());
        }
    }
}
