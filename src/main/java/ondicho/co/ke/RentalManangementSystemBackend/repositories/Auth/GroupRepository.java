package ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth;

import jakarta.transaction.Transactional;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository  extends JpaRepository<Group,Integer> {

    @Query(
            value="SELECT g.* from groups g where g.name like %:name%",
            nativeQuery = true)
    Optional<Group> findbyName(@Param(("name")) String name);
}
