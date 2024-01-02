package SpringChat.controllers;

import SpringChat.models.SessionModel;
import SpringChat.models.UserModel;
import SpringChat.storages.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import SpringChat.SpringChat;
import SpringChat.models.FileInfoModel;
import SpringChat.storages.FileInfoStorage;
import SpringChat.storages.SessionStorage;
import SpringChat.utils.Utils1;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class MainController {

    @Autowired
    private SessionStorage sessionStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private Utils1 byteUtils;

    @Autowired
    private WebsocketController websocketController;

    @Autowired
    private FileInfoStorage fileInfoStorage;

    private UserModel getUser(HttpSession session) {
        return sessionStorage.findById(session.getId()).get().getUserModel();
    }

    @PostMapping("/change-password")
    public String changePassword(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> params) {
        String currentPassword = params.get("currentPassword");
        String newPassword = params.get("newPassword");
        Boolean updateCookies = Boolean.parseBoolean(params.get("updateCookies"));
        HttpSession httpSession = request.getSession();
        UserModel userModel = getUser(httpSession);
        if (userModel.getPassword().equals(byteUtils.hash(currentPassword))) {
            String problem = UserModel.validatePassword(newPassword);
            if (problem == null) {
                userModel.setPassword(byteUtils.hash(newPassword));
                userStorage.save(userModel);
                if (updateCookies) {
                    Cookie usernameCookie = new Cookie("username", userModel.getUsername());
                    usernameCookie.setMaxAge(60 * 60 * 24 * 30 * 12);
                    response.addCookie(usernameCookie);
                    Cookie passwordCookie = new Cookie("password", userModel.getPassword());
                    passwordCookie.setMaxAge(60 * 60 * 24 * 30 * 12);
                    response.addCookie(passwordCookie);
                }
                return "Yes";
            } else {
                return "No\n" + problem;
            }
        } else {
            return "No\nIncorrect current password";
        }
    }

    @PostMapping("/login-helper")
    public String loginHelper(HttpServletRequest request, HttpServletResponse response, @RequestBody UserModel userModel) throws IOException {
        HttpSession httpSession = request.getSession();
        UserModel dbUserModel = userStorage.findByUsername(userModel.getUsername());
        if ((dbUserModel != null) && dbUserModel.getPassword().equals(byteUtils.hash(userModel.getPassword()))) {
            SessionModel sessionModel = sessionStorage.findById(httpSession.getId()).get();
            sessionModel.setUserModel(dbUserModel);
            if (userModel.getRememberMe()) {
                Cookie usernameCookie = new Cookie("username", userModel.getUsername());
                usernameCookie.setMaxAge(60 * 60 * 24 * 30 * 12);
                response.addCookie(usernameCookie);
                Cookie passwordCookie = new Cookie("password", byteUtils.hash(userModel.getPassword()));
                passwordCookie.setMaxAge(60 * 60 * 24 * 30 * 12);
                response.addCookie(passwordCookie);
            }
            String redirectedUri = sessionModel.getRedirectedUri();
            redirectedUri = (redirectedUri != null) ? redirectedUri : "/userModel";
            return "Yes\n" + redirectedUri;
        } else {
            return "No\nIncorrect username and/or password";
        }
    }

    @PostMapping("/signup-helper")
    public String signupHelper(HttpServletRequest request, HttpServletResponse response, @RequestBody UserModel userModel) throws IOException {
        HttpSession httpSession = request.getSession();
        UserModel dbUserModel = userStorage.findByUsername(userModel.getUsername());
        if (dbUserModel == null) {
            String problem = UserModel.validateAll(userModel);
            if (problem == null) {
                dbUserModel = new UserModel();
                dbUserModel.setFirstname(userModel.getFirstname());
                dbUserModel.setLastname(userModel.getLastname());
                dbUserModel.setUsername(userModel.getUsername());
                dbUserModel.setPassword(byteUtils.hash(userModel.getPassword()));
                userStorage.saveAndFlush(dbUserModel);
                String result = loginHelper(request, response, userModel);
                websocketController.updateAllUserLists(null);
                return result;
            } else {
                return "No\n" + problem;
            }
        } else {
            return "No\nDuplicate username";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession httpSession = request.getSession();
        UserModel userModel = getUser(httpSession);
        try {
            Cookie usernameCookie = new Cookie("username", "");
            usernameCookie.setMaxAge(0);
            response.addCookie(usernameCookie);
            Cookie passwordCookie = new Cookie("password", "");
            passwordCookie.setMaxAge(0);
            response.addCookie(passwordCookie);
            SessionModel sessionModel = sessionStorage.findById(httpSession.getId()).get();
            websocketController.logout(userModel);
            sessionModel.logout();
            response.sendRedirect("/home");
            return "Redirecting";
        } catch (IOException e) {
            e.printStackTrace();
            String exStr = byteUtils.serializeException(e);
            return "<code>" + exStr + "</code>";
        }
    }

    @GetMapping("/deleteAccount")
    public String deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        HttpSession httpSession = request.getSession();
        SessionModel sessionModel = sessionStorage.findById(httpSession.getId()).get();
        UserModel userModel = sessionModel.getUserModel();
        String logout = logout(request, response);
        if ("Redirecting".equals(logout) && !SpringChat.ADMIN_USERNAME.equals(userModel.getUsername())) {
            sessionStorage.findByUsername(httpSession.getId())
                    .forEach(x -> x.setUserModel(null));
            userStorage.deleteById(userModel.getId());
        }
        websocketController.updateAllUserLists(userModel.getUsername());
        return logout;
    }

    @GetMapping(value = "/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam String fileId, HttpServletResponse response) {
        try {
            Optional<FileInfoModel> infoOptional = fileInfoStorage.findById(UUID.fromString(fileId));
            if (!infoOptional.isPresent()) {
                throw new RuntimeException("FileNotFound: " + fileId);
            }
            FileInfoModel info = infoOptional.get();
            MediaType mediaType = byteUtils.getMediaType(info.getName());
            InputStreamResource resource = new InputStreamResource(
                    new FileInputStream(new File(SpringChat.UPLOAD_PATH + "/" + info.getFileDataId())));
            ResponseEntity<InputStreamResource> body = ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; "
                            + "filename=\"" + info.getName() + "\"; "
                            + "filename*=UTF-8''" + URLEncoder.encode(info.getName(), "UTF-8").replace("+", "%20"))
                    .contentLength(info.getLength())
                    .body(resource);
            return body;
        } catch (Exception ioex) {
            throw new RuntimeException("Exception while reading file: " + fileId);
        }
    }

    @PostMapping(value = "/uploadFile")
    @ResponseBody
    public String uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        HttpSession httpSession = request.getSession();
        Map<String, String> params = new HashMap<>();
        params.put("pageTitle", "title");
        boolean success = true;
        try {
            FileInfoModel info = new FileInfoModel();
            info.setName(file.getOriginalFilename());
            File orgFile = new File(SpringChat.UPLOAD_PATH + "/" + UUID.randomUUID());
            FileOutputStream os = new FileOutputStream(orgFile);
            byteUtils.copy(file.getInputStream(), os, false, true);
            info.setFileDataId(UUID.fromString(orgFile.getName()));
            info.setLength(orgFile.length());

            MediaType mediaType = byteUtils.getMediaType(info.getName());
            boolean isImg = (mediaType != null && mediaType.toString().contains("image"));
            if (isImg) {
                File imgPrevFile = new File(SpringChat.UPLOAD_PATH + "/" + UUID.randomUUID());
                byteUtils.makeThumbnailImage(SpringChat.IMG_THUMBNAIL_FORMAT, 500, new FileInputStream(orgFile), new FileOutputStream(imgPrevFile), true, true);
                info.setImgPrevFileDataId(UUID.fromString(imgPrevFile.getName()));
            }
            fileInfoStorage.save(info);
            websocketController.sendFile(getUser(httpSession), info);
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        params.put("body", "<div style=\"line-height: 25px;background-color: #eeeeee;\"> &nbsp;"
                + (success ? "✓" : "✗")
                + "</div>");
        try {
            return byteUtils.readPage("/base.html", params);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
