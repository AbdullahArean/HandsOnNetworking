package SpringChat.storages;

import java.io.File;

import SpringChat.models.MessageModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import SpringChat.SpringChat;
import SpringChat.models.FileInfoModel;

@Repository
public interface MessageStorage extends JpaRepository<MessageModel, UUID> {

    public List<MessageModel> findAllByReceiverUsername(String receiver);

    public List<MessageModel> findAllBySenderUsername(String receiver);

    @Query(value = "select * from message m where(m.date < :date) and "
            + "(m.receiver_username=:receiver) "
            + "order by m.date desc limit :limit", nativeQuery = true)
    public List<MessageModel> fetchMessages(@Param("limit") int limit, @Param("receiver") String receiver, @Param("date") long date);

    @Query(value = "select * from message m where (m.date < :date) and "
            + "((m.sender_username=:sender and m.receiver_username=:receiver) or "
            + "(m.sender_username=:receiver and m.receiver_username=:sender)) "
            + "order by m.date desc limit :limit", nativeQuery = true)
    public List<MessageModel> fetchMessages(@Param("limit") int limit, @Param("sender") String sender, @Param("receiver") String receiver, @Param("date") long date);

    @Override
    public default void delete(MessageModel msg) {
        FileInfoStorage fileInfoStorage = SpringChat.getBean(FileInfoStorage.class);
        if (!msg.isTextMessage()) {
            FileInfoModel fileInfoModel = fileInfoStorage.findById(msg.getFileInfoId()).get();
            new File(SpringChat.UPLOAD_PATH + "/" + fileInfoModel.getFileDataId()).delete();
            if (fileInfoModel.getImgPrevFileDataId() != null) {
                new File(SpringChat.UPLOAD_PATH + "/" + fileInfoModel.getImgPrevFileDataId()).delete();
            }
            fileInfoStorage.deleteById(fileInfoModel.getId());
        }
        deleteById(msg.getId());
    }
}
