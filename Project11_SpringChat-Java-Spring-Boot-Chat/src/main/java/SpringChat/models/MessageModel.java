package SpringChat.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class MessageModel extends BaseModel implements Comparable<MessageModel> {

    @Column(length = 100)
    private String senderUsername;

    @Column(length = 100)
    private String receiverUsername;

    @Column
    private boolean textMessage;

    @Column
    private boolean imageFile;

    @Column(length = 100)
    private String senderPresentation;

    @Column
    private String body;

    @Column
    private Long date;

    @Column
    private UUID fileInfoId;

    public MessageModel clone() {
        MessageModel m = new MessageModel();
        m.setSenderUsername(senderUsername);
        m.setReceiverUsername(receiverUsername);
        m.setTextMessage(textMessage);
        m.setImageFile(imageFile);
        m.setSenderPresentation(senderPresentation);
        m.setBody(body);
        m.setDate(date);
        m.setFileInfoId(fileInfoId);
        return m;
    }

    @Override
    public int compareTo(MessageModel messageModel) {
        return date.compareTo(messageModel.getDate());
    }
}
