package ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth;

import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findRoleByName(String name);

    List<Role> findRolesByGroupId(int id);

}
