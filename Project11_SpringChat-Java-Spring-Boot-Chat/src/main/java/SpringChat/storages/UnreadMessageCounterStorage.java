package SpringChat.storages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import SpringChat.models.UnreadMessageCounterModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface UnreadMessageCounterStorage extends JpaRepository<UnreadMessageCounterModel, UUID> {

    public UnreadMessageCounterModel findByCurrentSideUsernameAndOtherSideUsername(String cs, String os);

    public List<UnreadMessageCounterModel> findAllByCurrentSideUsername(String s);

    public List<UnreadMessageCounterModel> findAllByOtherSideUsername(String s);
}
