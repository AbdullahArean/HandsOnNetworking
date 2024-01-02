package SpringChat.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@NoArgsConstructor
@Getter
@Setter
public class SessionModel {

    private String id;

    private UserModel userModel;

    private Long lastModified;

    private String redirectedUri;

    private WebSocketSession webSocketSession;

    private String otherSideUsername;

    public SessionModel(String id, UserModel userModel, Long lastModified) {
        this.id = id;
        this.userModel = userModel;
        this.lastModified = lastModified;
    }

    public void logout() {
        setUserModel(null);
    }
}
