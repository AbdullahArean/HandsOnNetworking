package SpringChat.configurations;

import SpringChat.models.SessionModel;
import SpringChat.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import SpringChat.SpringChat;
import SpringChat.models.FileInfoModel;
import SpringChat.storages.FileInfoStorage;
import SpringChat.storages.SessionStorage;
import SpringChat.storages.UserStorage;
import SpringChat.utils.Utils1;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class HttpInterceptor implements HandlerInterceptor {

    @Autowired
    private SessionStorage sessionStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private Utils1 byteUtils;

    @Autowired
    private FileInfoStorage fileInfoStorage;

    private Set<String> allowedUrls = new HashSet<>();
    private Set<String> allowedFiles = new HashSet<>();
    private Set<String> htmlFiles = new HashSet<>();

    public HttpInterceptor() {
        allowedUrls.add("/");
        allowedUrls.add("/home");
        allowedUrls.add("/index");
        allowedUrls.add("/login-helper");
        allowedUrls.add("/signup");
        allowedUrls.add("/signup-helper");

        for (String filename : Objects.requireNonNull(new File(SpringChat.IMAGE_RESOURCE_PATH).list())) {
            allowedFiles.add("/" + filename);
        }
        for (String filename : Objects.requireNonNull(new File(SpringChat.STYLE_RESOURCE_PATH).list())) {
            allowedFiles.add("/" + filename);
        }
        for (String filename : Objects.requireNonNull(new File(SpringChat.SCRIPT_RESOURCE_PATH).list())) {
            allowedFiles.add("/" + filename);
        }
        for (String filename : Objects.requireNonNull(new File(SpringChat.MISC_RESOURCE_PATH).list())) {
            allowedFiles.add("/" + filename);
        }
        for (String filename : Objects.requireNonNull(new File(SpringChat.PAGE_RESOURCE_PATH).list())) {
            htmlFiles.add("/" + filename);
        }
    }

    private void putHeaderParams(Map<String, String> params, UserModel userModel) {
        if (userModel != null) {
            params.put("headerAccountText", userModel.getFirstname() + " " + userModel.getLastname());
            params.put("headerAccountAction", "");
            params.put("headerAccountClasses", "Dropdown");
            params.put("HeaderUserCaretSvgDisplay", "inline-block");
        } else {
            params.put("headerAccountText", "Sign In / Sign Up");
            params.put("headerAccountAction", "onclick='location.href=\"/userModel\"'");
            params.put("headerAccountClasses", "");
            params.put("HeaderUserCaretSvgDisplay", "none");
        }
    }

    private Properties getPropertiesFromCookies(Cookie cookie[]) {
        if (cookie == null) {
            return null;
        }
        Properties cookies = new Properties();
        for (Cookie c : cookie) {
            cookies.put(c.getName(), c.getValue());
        }
        return cookies;
    }

    public void loginWithCookies(Properties cookies, SessionModel sessionModel) {
        if (cookies != null) {
            if (cookies.containsKey("username") && cookies.containsKey("password")) {
                UserModel dbUserModel = userStorage.findByUsername("" + cookies.get("username"));
                if ((dbUserModel != null) && dbUserModel.getPassword().equals(cookies.get("password"))) {
                    sessionModel.setUserModel(dbUserModel);
                }
            }
        }
    }

    private void loginWithBasicAuth(HttpServletRequest request, HttpSession httpSession, SessionModel sessionModel) throws UnsupportedEncodingException {
        String auth = request.getHeader("Authorization");
        if (auth != null) {
            String[] split = auth.split(" ");
            if ("basic".equals(split[0].toLowerCase())) {
                String base64 = split[1];
                String cred = new String(Base64.getDecoder().decode(base64), "UTF-8");
                String username = cred.substring(0, cred.indexOf(":"));
                String password = cred.substring(cred.indexOf(":") + 1, cred.length());
                UserModel dbUserModel = userStorage.findByUsername(username);
                if ((dbUserModel != null) && dbUserModel.getPassword().equals(byteUtils.hash(password))) {
                    sessionModel.setUserModel(dbUserModel);
                }
            }
        }
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 3600000)
    public void clearExpiredSessions() {
        List<SessionModel> expired = sessionStorage.findExpired(System.currentTimeMillis() - 3600000);
        sessionStorage.deleteAll(expired);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession httpSession = request.getSession();

        SessionModel sessionModel = sessionStorage.findById(httpSession.getId()).orElse(null);
        if (sessionModel == null) {
            sessionModel = new SessionModel(httpSession.getId(), null, System.currentTimeMillis());
        }
        sessionModel.setLastModified(System.currentTimeMillis());
        sessionStorage.save(sessionModel);

        loginWithBasicAuth(request, httpSession, sessionModel);
        loginWithCookies(getPropertiesFromCookies(request.getCookies()), sessionModel);

        if (allowedFiles.contains(uri)) {
            String uriLC = uri.toLowerCase();
            if (uriLC.endsWith(".svg")) {
                response.setContentType("image/svg+xml");
                response.getOutputStream().write(byteUtils.readBytes(new File((SpringChat.IMAGE_RESOURCE_PATH + "/" + uri)), false));
            } else if (uriLC.endsWith(".css")) {
                response.setContentType("text/css");
                response.getOutputStream().write(byteUtils.readBytes(new File((SpringChat.STYLE_RESOURCE_PATH + "/" + uri)), false));
            } else if (uriLC.endsWith(".json")) {
                response.setContentType("application/json");
                response.getOutputStream().write(byteUtils.readBytes(new File((SpringChat.MISC_RESOURCE_PATH + "/" + uri)), false));
            } else if (uriLC.endsWith(".js")) {
                response.setContentType("text/javascript");
                response.getOutputStream().write(byteUtils.readBytes(new File((SpringChat.SCRIPT_RESOURCE_PATH + "/" + uri)), false));
            } else if (uriLC.endsWith(".png")) {
                response.setContentType("image/png");
                response.getOutputStream().write(byteUtils.readBytes(new File((SpringChat.IMAGE_RESOURCE_PATH + "/" + uri)), false));
            } else if (uriLC.endsWith(".ico")) {
                response.setContentType("image/x-icon");
                response.getOutputStream().write(byteUtils.readBytes(new File((SpringChat.IMAGE_RESOURCE_PATH + "/" + uri)), false));
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return false;
        }

        if (uri.startsWith("/image-file-preview/")) {
            String fileInfoId = uri.substring(uri.indexOf('/', 1) + 1);
            FileInfoModel info = fileInfoStorage.findById(UUID.fromString(fileInfoId)).get();
            MediaType mediaType;
            byte fileData[];
            if (info.getImgPrevFileDataId() == null) {
                mediaType = byteUtils.getMediaType(info.getName());
                fileData = byteUtils.readBytes(new File(SpringChat.UPLOAD_PATH + "/" + info.getFileDataId()), true);
            } else {
                mediaType = byteUtils.getMediaType("a." + SpringChat.IMG_THUMBNAIL_FORMAT);
                fileData = byteUtils.readBytes(new File(SpringChat.UPLOAD_PATH + "/" + info.getImgPrevFileDataId()), true);
            }
            response.setContentType("" + mediaType);
            response.getOutputStream().write(fileData);
            return false;
        }

        if (!allowedUrls.contains(uri)) {
            if ("/login".equals(uri)) {
                if (sessionModel.getUserModel() != null) {
                    response.sendRedirect("/user");
                    return false;
                }
            } else {
                if (sessionModel.getUserModel() == null) {
                    sessionModel.setRedirectedUri(uri);
                    response.sendRedirect("/login");
                    return false;
                }
            }
        }

        if (uri.equals("/")) {
            uri = "/home";
        }
        if (htmlFiles.contains(uri + ".html")) {
            Map<String, String> params = new HashMap<>();
            putHeaderParams(params, sessionModel.getUserModel());
            params.put("pageTitle", uri.substring(1));
            if (uri.equals("/user")) {
                params.put("optionsSvg",
                        "<img src=\"options.svg\" "
                        + "id=\"OptionsSvg\" "
                        + "class=\"Button ButtonTransPrimary\""
                        + "onclick='toggleSidebarDisplay()'/>");
            } else {
                params.put("optionsSvg", "");
            }
            String html = byteUtils.readPage(uri + ".html", params);
            response.setContentType("text/html");
            response.getOutputStream().write(html.getBytes("UTF-8"));
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception {
    }
}
