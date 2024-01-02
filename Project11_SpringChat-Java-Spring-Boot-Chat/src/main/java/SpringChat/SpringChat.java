package SpringChat;

import java.io.File;

import SpringChat.storages.UserStorage;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import SpringChat.configurations.HttpInterceptor;
import SpringChat.models.UserModel;
import SpringChat.utils.Utils1;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration(exclude = ErrorMvcAutoConfiguration.class)
public class SpringChat implements ApplicationContextAware {

    public static final String PAGE_RESOURCE_PATH = "res/html";
    public static final String STYLE_RESOURCE_PATH = "res/css";
    public static final String SCRIPT_RESOURCE_PATH = "res/js";
    public static final String IMAGE_RESOURCE_PATH = "res/img";
    public static final String MISC_RESOURCE_PATH = "res/misc";
    public static final String ADMIN_USERNAME = "admin";
    public static final String BROADCAST_USERNAME = "broadcast";
    public static final String IMG_THUMBNAIL_FORMAT = "jpg";
    public static final String UPLOAD_PATH = "uploads";

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private Utils1 byteUtils;

    private static ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(SpringChat.class, args);
    }

    @PostConstruct
    public void starter() {
        new File(UPLOAD_PATH).mkdir();
        if (userStorage.findByUsername(BROADCAST_USERNAME) == null) {

            UserModel bcUSer = new UserModel();
            bcUSer.setUsername(BROADCAST_USERNAME);
            bcUSer.setPassword(byteUtils.hash("123456"));
            bcUSer.setFirstname("Broadcast");
            bcUSer.setLastname("");
            userStorage.save(bcUSer);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        context = ac;
    }

    public static <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }

    @Configuration
    public class InterceptorConfig extends WebMvcConfigurerAdapter {

        @Autowired
        HttpInterceptor serviceInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(serviceInterceptor);
        }
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(10000000000l);
        return multipartResolver;
    }
}
