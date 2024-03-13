package ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth;

import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<BlackList,Integer> {

    Optional<BlackList> findByToken(String token);
}
