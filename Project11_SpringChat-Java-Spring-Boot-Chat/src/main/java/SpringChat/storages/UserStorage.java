package SpringChat.storages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import SpringChat.models.UserModel;

import java.util.UUID;
@Repository
public interface UserStorage extends JpaRepository<UserModel, UUID> {

    UserModel findByUsername(String username);
}
