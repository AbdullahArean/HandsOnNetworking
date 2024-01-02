package SpringChat.storages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import SpringChat.models.FileInfoModel;

import java.util.UUID;

@Repository
public interface FileInfoStorage extends JpaRepository<FileInfoModel, UUID> {
}
