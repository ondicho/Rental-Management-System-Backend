package ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth;

import jakarta.transaction.Transactional;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository  extends JpaRepository<User,Integer> {

    @Transactional
    @Query(value = "select u.* from users u join user_roles ur on u.id =ur.user_id " +
            "join roles r on ur.role_id =r.id join groups g on r.group_id=g.id " +
            "where g.id= :groupId",nativeQuery = true)
    Page<User> findAllGroupUsers(@Param("groupId")int groupId, Pageable pageable);

    @Transactional
    @Query(
            value="SELECT u.* from users u where lower(u.email) like %:email%",
            nativeQuery = true)
    Optional<User> findByEmail(String email);
}
