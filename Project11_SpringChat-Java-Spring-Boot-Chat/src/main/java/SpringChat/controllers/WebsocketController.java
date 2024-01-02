package SpringChat.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import SpringChat.SpringChat;
import SpringChat.configurations.HttpInterceptor;
import SpringChat.models.*;
import SpringChat.storages.*;
import SpringChat.utils.Utils1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class WebsocketController implements WebSocketHandler {

    @Autowired
    private Utils1 byteUtils;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private SessionStorage sessionStorage;

    @Autowired
    private MessageStorage messageStorage;

    @Autowired
    private UnreadMessageCounterStorage unreadMessageCounterStorage;

    @Autowired
    private FileInfoStorage fileInfoStorage;

    @Autowired
    private HttpInterceptor httpInterceptor;

    @Autowired
    @Value("${loadingMessagesChunksize}")
    public int loadingMessagesChunksize;

    private final ReentrantLock lock = new ReentrantLock(true);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, SessionModel> sessionMapFromWSS = new HashMap<>();
    private final Map<String, SessionModel> sessionMapFromUN = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession wss) throws IOException {
        try {
//            if (userRepository.findByUsername("admin") == null) {
//
//                UserModel bcUSer = new UserModel();
//                bcUSer.setUsername("Broadcast");
//                bcUSer.setPassword(byteUtils.hash("123456"));
//                bcUSer.setFirstname("Broadcast");
//                bcUSer.setLastname("");
//                userRepository.save(bcUSer);
//            }
            lock.lock();
            SessionModel sessionModel = getSession(wss);
            if ((sessionModel != null) && (sessionModel.getUserModel() != null)) {
                SessionModel oldSessionModel = sessionMapFromUN.get(sessionModel.getUserModel().getUsername());
                if (oldSessionModel != null) {
                    if (oldSessionModel.getWebSocketSession().isOpen()) {
                        oldSessionModel.getWebSocketSession().sendMessage(new TextMessage("redirect\n/"));
                    }
                    removeWebSocketSession(oldSessionModel.getWebSocketSession());
                }
                sessionModel.setWebSocketSession(wss);
                sessionMapFromWSS.put(wss.getId(), sessionModel);
                sessionMapFromUN.put(sessionModel.getUserModel().getUsername(), sessionModel);
                sessionModel.setOtherSideUsername(SpringChat.BROADCAST_USERNAME);
                changePage(sessionModel);
                sessionModel.getWebSocketSession().sendMessage(new TextMessage(
                        createUsersListUIComponent(sessionModel.getUserModel().getUsername(), sessionModel.getOtherSideUsername())));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void handleMessage(WebSocketSession wss, WebSocketMessage<?> webSocketMessage) throws Exception {
        try {
            lock.lock();
            if (!sessionMapFromWSS.containsKey(wss.getId())) {
                return;
            }
            SessionModel currentSessionModel = sessionMapFromWSS.get(wss.getId());
            UserModel currentUserModel = currentSessionModel.getUserModel();
            String payload = ((TextMessage) webSocketMessage).getPayload();
            String cmd = payload.substring(0, payload.indexOf("\n"));
            String body = payload.substring(payload.indexOf("\n") + 1, payload.length());
            if ("msg".equals(cmd)) {
                MessageModel msg = new MessageModel();
                msg.setTextMessage(true);
                msg.setBody(body);
                msg.setDate(System.currentTimeMillis());
                msg.setSenderPresentation(currentUserModel.getPresentation());
                msg.setSenderUsername(currentUserModel.getUsername());
                msg.setReceiverUsername(currentSessionModel.getOtherSideUsername());

                routeMessage(currentUserModel.getUsername(), msg);
            } else if ("change-page".equals(cmd)) {
                currentSessionModel.setOtherSideUsername(body);
                changePage(currentSessionModel);
            } else if ("delete-msg".equals(cmd)) {
                MessageModel msg = messageStorage.findById(UUID.fromString(body)).get();
                deleteMessage(msg, currentSessionModel);
            } else if ("top".equals(cmd)) {
                String otherSideUsername = currentSessionModel.getOtherSideUsername();
                sendMessages(currentUserModel.getUsername(), otherSideUsername, currentSessionModel, Long.parseLong(body), "load");
            } else if ("ping".equals(cmd)) {
                currentSessionModel.getWebSocketSession().sendMessage(new TextMessage("pong\n"));
            } else {
                logger.error("Unsupported command!");
            }
        } finally {
            lock.unlock();
        }
    }

    private void sendMessages(String currentSideUsername, String otherSideUsername, SessionModel currentSessionModel, long date, String cmd) throws IOException {
        List<MessageModel> messageModels;
        if (SpringChat.BROADCAST_USERNAME.equals(otherSideUsername)) {
            messageModels = messageStorage.fetchMessages(loadingMessagesChunksize, otherSideUsername, date);
        } else {
            messageModels = messageStorage.fetchMessages(loadingMessagesChunksize, currentSideUsername, otherSideUsername, date);
        }
        Collections.reverse(messageModels);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messageModels.size(); i++) {
            MessageModel m = messageModels.get(i);
            if (m.isTextMessage()) {
                sb.append(createTextMessageUIComponent(m, currentSideUsername.equals(m.getSenderUsername())));
            } else {
                sb.append(createFileMessageUIComponent(m, currentSideUsername.equals(m.getSenderUsername())));
            }
        }
        currentSessionModel.getWebSocketSession().sendMessage(new TextMessage(cmd + "\n" + sb.toString()));
        if (messageModels.size() > 0) {
            currentSessionModel.getWebSocketSession().sendMessage(new TextMessage("checkForLoadingMore\n"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wss, CloseStatus closeStatus) throws Exception {
        try {
            lock.lock();
            removeWebSocketSession(wss);
        } finally {
            lock.unlock();
        }
    }

    public void logout(UserModel userModel) {
        try {
            lock.lock();
            if (userModel != null && sessionMapFromUN.containsKey(userModel.getUsername())) {
                removeWebSocketSession(sessionMapFromUN.get(userModel.getUsername()).getWebSocketSession());
            }
        } finally {
            lock.unlock();
        }
    }

    private void removeWebSocketSession(WebSocketSession wss) {
        if (sessionMapFromWSS.containsKey(wss.getId())) {
            UserModel userModel = sessionMapFromWSS.get(wss.getId()).getUserModel();
            if (userModel != null) {
                sessionMapFromUN.remove(userModel.getUsername());
            }
            sessionMapFromWSS.remove(wss.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendFile(UserModel sender, FileInfoModel info) throws IOException {
        MessageModel msg = new MessageModel();
        msg.setTextMessage(false);
        msg.setImageFile(info.getImgPrevFileDataId() != null);
        msg.setBody(info.getName() + " (" + byteUtils.humanReadableSize(info.getLength()) + ")");
        msg.setDate(System.currentTimeMillis());
        msg.setSenderPresentation(sender.getPresentation());
        msg.setFileInfoId(info.getId());
        try {
            lock.lock();
            routeMessage(sender.getUsername(), msg);
        } finally {
            lock.unlock();
        }
    }

    public void updateAllUserLists(String excludeUsername) {
        try {
            lock.lock();
            if (excludeUsername != null) {
                List<MessageModel> messageModels = messageStorage.findAllBySenderUsername(excludeUsername);
                messageModels.addAll(messageStorage.findAllByReceiverUsername(excludeUsername));
                messageModels.forEach(x -> messageStorage.delete(x));
                List<UnreadMessageCounterModel> unreadMessageCounterModels = unreadMessageCounterStorage.findAllByCurrentSideUsername(excludeUsername);
                unreadMessageCounterModels.addAll(unreadMessageCounterStorage.findAllByOtherSideUsername(excludeUsername));
                unreadMessageCounterStorage.deleteAll(unreadMessageCounterModels);
            }
            sessionMapFromWSS.values().forEach(x -> {
                try {
                    if (x.getUserModel() != null) {
                        if (excludeUsername != null && x.getOtherSideUsername().equals(excludeUsername)) {
                            x.setOtherSideUsername(SpringChat.BROADCAST_USERNAME);
                            changePage(x);
                        }
                        x.getWebSocketSession().sendMessage(new TextMessage(
                                createUsersListUIComponent(x.getUserModel().getUsername(), x.getOtherSideUsername())));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } finally {
            lock.unlock();
        }
    }

    private SessionModel getSession(WebSocketSession wss) {
        List<String> cookies = wss.getHandshakeHeaders().get("cookie");
        if ((cookies != null) && !cookies.isEmpty()) {
            String foundCookie = null;
            for (String c : cookies) {
                if (c.toLowerCase().contains("jsessionid=")) {
                    foundCookie = c;
                    break;
                }
            }
            if (foundCookie != null) {
                try {
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(foundCookie.replaceAll(";", "\n").getBytes("UTF-8")));
                    String sid = properties.getProperty("JSESSIONID");
                    Optional<SessionModel> sessionOptional = sessionStorage.findById(sid);
                    if (sessionOptional.isPresent()) {
                        return sessionOptional.get();
                    } else {
                        SessionModel sessionModel = new SessionModel(sid, null, System.currentTimeMillis());
                        httpInterceptor.loginWithCookies(properties, sessionModel);
                        if (sessionModel.getUserModel() != null) {
                            sessionStorage.save(sessionModel);
                            return sessionModel;
                        } else {
                            wss.sendMessage(new TextMessage("redirect\n/"));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String createFileMessageUIComponent(MessageModel msg, boolean self) throws IOException {
        String text = "";
        Map<String, String> params = new HashMap<>();
        params.put("id", "" + msg.getId());
        params.put("date", "date=\"" + msg.getDate() + "\"");
        params.put("onclick", " onclick=\'download(\"" + msg.getFileInfoId() + "\")\' ");
        params.put("body", msg.getBody());
        params.put("dateStr", byteUtils.formatTime(msg.getDate()));
        if (msg.isImageFile()) {
            params.put("image", "<img src=\"image-file-preview/" + msg.getFileInfoId() + "\" class=\"ChatImgFileMsgAttachment\"/>");
        } else {
            params.put("image", "<img src=\"attachment.svg\" class=\"ChatFileMsgAttachmentSvg\"/>");
        }
        if (self) {
            text += byteUtils.readPage("/chat-msg-right.html", params);
        } else {
            params.put("title", msg.getSenderPresentation());
            text += byteUtils.readPage("/chat-msg-left.html", params);
        }
        return text;
    }

    private String createTextMessageUIComponent(MessageModel msg, boolean self) throws IOException {
        String text = "";
        Map<String, String> params = new HashMap<>();
        params.put("id", "" + msg.getId());
        params.put("date", "date=\"" + msg.getDate() + "\"");
        params.put("image", "");
        params.put("onclick", "");
        params.put("body", msg.getBody());
        params.put("dateStr", byteUtils.formatTime(msg.getDate()));
        if (self) {
            text += byteUtils.readPage("/chat-msg-right.html", params);
        } else {
            params.put("title", msg.getSenderPresentation());
            text += byteUtils.readPage("/chat-msg-left.html", params);
        }
        return text;
    }

    private String createUsersListUIComponent(String username, String activeUsername) throws IOException {
        List<UserModel> userModels = userStorage.findAll();
        UserModel broadcastUserModel = userModels.stream().filter(x -> x.getUsername().equals(SpringChat.BROADCAST_USERNAME))
                .collect(Collectors.toList()).get(0);
        userModels.remove(broadcastUserModel);
        Collections.sort(userModels);
        userModels.add(0, broadcastUserModel);
        String text = "userModels\n";
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < userModels.size(); i++) {
            UserModel userModel = userModels.get(i);
            params.clear();
            if (userModel.getUsername().equals(activeUsername)) {
                params.put("name", userModel.getPresentation());
                text += byteUtils.readPage("/sidebar-entry-active.html", params);
            } else {
                params.put("name", userModel.getPresentation());
                params.put("onclick", " onclick=\'changePage(\"" + userModel.getUsername() + "\")\' ");
                UnreadMessageCounterModel unreadMessageCounterModel = unreadMessageCounterStorage.findByCurrentSideUsernameAndOtherSideUsername(username, userModel.getUsername());
                Integer count = (unreadMessageCounterModel == null) ? 0 : unreadMessageCounterModel.getCount();
                params.put("count", (count == 0) ? ""
                        : "<div class=\"SidebarEntryUnreadCount SimpleText SimpleFont\">" + count + "</div>");
                text += byteUtils.readPage("/sidebar-entry-passive.html", params);
            }
        }
        return text;
    }

    private void changePage(SessionModel sessionModel) throws IOException {
        String otherSideUsername = sessionModel.getOtherSideUsername();
        sessionModel.getWebSocketSession().sendMessage(new TextMessage(
                createUsersListUIComponent(sessionModel.getUserModel().getUsername(), otherSideUsername)));
        sendMessages(sessionModel.getUserModel().getUsername(), otherSideUsername, sessionModel, System.currentTimeMillis(), "page");
        UnreadMessageCounterModel unreadMessageCounterModel = unreadMessageCounterStorage.findByCurrentSideUsernameAndOtherSideUsername(sessionModel.getUserModel().getUsername(), otherSideUsername);
        if (unreadMessageCounterModel != null) {
            unreadMessageCounterStorage.delete(unreadMessageCounterModel);
        }
    }

    private void routeMessage(String senderUsername, MessageModel msg) throws IOException {
        SessionModel currentSessionModel = sessionMapFromUN.get(senderUsername);
        String otherSideUsername = currentSessionModel.getOtherSideUsername();
        msg.setSenderUsername(senderUsername);
        msg.setReceiverUsername(otherSideUsername);
        messageStorage.save(msg);
        String selfPack = "msg\n" + (msg.isTextMessage() ? createTextMessageUIComponent(msg, true) : createFileMessageUIComponent(msg, true));
        String otherPack = "msg\n" + (msg.isTextMessage() ? createTextMessageUIComponent(msg, false) : createFileMessageUIComponent(msg, false));
        routePacket(selfPack, otherPack, currentSessionModel);
    }

    private void routePacket(String selfPack, String otherPack, SessionModel currentSessionModel) throws IOException {
        String senderUsername = currentSessionModel.getUserModel().getUsername();
        String otherSideUsername = currentSessionModel.getOtherSideUsername();
        currentSessionModel.getWebSocketSession().sendMessage(new TextMessage(selfPack));
        if (otherSideUsername.equals(SpringChat.BROADCAST_USERNAME)) {
            for (UserModel userModel : userStorage.findAll()) {
                String username = userModel.getUsername();
                if (username.equals(senderUsername) || username.equals(SpringChat.BROADCAST_USERNAME)) {
                    continue;
                }
                SessionModel userSessionModel = sessionMapFromUN.get(username);
                sendOtherSideMessage(otherPack, otherSideUsername, username, userSessionModel);
            }
        } else {
            if (!otherSideUsername.equals(senderUsername)) {
                SessionModel otherSideSessionModel = sessionMapFromUN.get(otherSideUsername);
                sendOtherSideMessage(otherPack, senderUsername, otherSideUsername, otherSideSessionModel);
            }
        }
    }

    private void sendOtherSideMessage(String msg, String otherSideUsername, String senderUsername, SessionModel sessionModel) throws IOException {
        if ((sessionModel != null) && sessionModel.getOtherSideUsername().equals(otherSideUsername)) {
            sessionModel.getWebSocketSession().sendMessage(new TextMessage(msg));
        } else {
            UnreadMessageCounterModel unreadMessageCounterModel = unreadMessageCounterStorage.findByCurrentSideUsernameAndOtherSideUsername(senderUsername, otherSideUsername);
            int cnt = (unreadMessageCounterModel == null) ? 1 : (unreadMessageCounterModel.getCount() + 1);
            if (unreadMessageCounterModel == null) {
                unreadMessageCounterModel = new UnreadMessageCounterModel();
                unreadMessageCounterModel.setCurrentSideUsername(senderUsername);
                unreadMessageCounterModel.setOtherSideUsername(otherSideUsername);
            }
            unreadMessageCounterModel.setCount(cnt);
            unreadMessageCounterStorage.save(unreadMessageCounterModel);
            if (sessionModel != null) {
                sessionModel.getWebSocketSession().sendMessage(new TextMessage(
                        createUsersListUIComponent(sessionModel.getUserModel().getUsername(), sessionModel.getOtherSideUsername())));
            }
        }
    }

    public void deleteMessage(MessageModel msg, SessionModel currentSessionModel) throws IOException {
        String pack = "delete-msg\n" + msg.getId();
        messageStorage.delete(msg);
        routePacket(pack, pack, currentSessionModel);
    }
}
