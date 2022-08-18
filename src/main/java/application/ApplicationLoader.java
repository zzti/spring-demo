package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author xiaoxi666
 * @date 2022-08-18 20:50
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApplicationLoader {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApplicationLoader.class);
        application.run(args);
    }
}